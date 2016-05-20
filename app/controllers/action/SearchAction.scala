package controllers.action

import models.SearchResult
import utils.CassandraHelper
import daos.WordIndicesDAO
import daos.ResourceLocationDAO
import daos.ResourceContentDAO
import org.apache.commons.lang3.StringUtils
import logic.indexer.FileIndexerFactory
import play.Logger
import dtos.WordIndicesDTO
import com.datastax.driver.core.Session
import logic.analyzer.StringAnalyzer
import dtos.WordIndicesDTO
import java.util.UUID
import dtos.WordIndicesDTO
import scala.collection.mutable
import dtos.WordIndicesDTO
import scala.collection.mutable.MapBuilder
import dtos.WordIndicesDTO

object SearchAction {

  val logger = Logger.of(this.getClass)

  def execute(word: String, order: SearchResultOrder): Seq[SearchResult] = {

    val session = CassandraHelper.getSession

    try {
      // 単語リストを取得して、指定オーダー順に並べる
      val wordIndices = loadAndSortIndices(session, word, order);

      // 取得した単語リストに含まれる場所の一覧を取得
      val locationIds = wordIndices.map { x => x.resourceLocationId }.toSet
      val locations = ResourceLocationDAO.select(session, locationIds)

      // 取得した単語リストに含まれる内容の一覧を取得
      val contentIds = wordIndices.map { x => x.indices.maxBy(_._2.size)._1 }.toSet
      val contents = ResourceContentDAO.select(session, contentIds)

      // 結果の組み立て
      wordIndices.map { x =>
        // 場所情報
        val location = locations.getOrElse(x.resourceLocationId, throw new RuntimeException(s"no location: ${x.resourceLocationId}"))
        // 内容(出現回数が一番多い内容)
        var resourceContentId = x.indices.maxBy(_._2.size)._1
        val content = contents.getOrElse(resourceContentId, throw new RuntimeException(s"no content: $resourceContentId"))
        // インデックス
        val indices = x.indices.getOrElse(resourceContentId, throw new RuntimeException(s"indices not found by resourceContentId:$resourceContentId"))
        // インデクサクラス名
        val indexer = FileIndexerFactory.create(location.indexerClassName)
        // キー情報
        val keyStr = Seq(
          getKeyString(indexer.getKeyTitles._1, content.key1),
          getKeyString(indexer.getKeyTitles._2, content.key2),
          getKeyString(indexer.getKeyTitles._3, content.key3))
          .filter { x => !x.isEmpty }
          .mkString(" / ")
        // リソース種別
        val resourceTypeName = indexer.getResourceTypeName

        // 結果オブジェクトを生成
        SearchResult(
          word,
          location.uri,
          location.name,
          location.size,
          resourceTypeName,
          location.created,
          location.modified,
          keyStr,
          content.content,
          content.prevContent,
          content.nextContent,
          indices,
          location.indexerClassName,
          location.indexGenerated)
      }.toSeq

    } finally {
      session.closeAsync()
    }
  }

  private def loadAndSortIndices(session: Session, text: String, order: SearchResultOrder): Seq[WordIndicesDTO] = {

    // 形態素解析の結果単語でインデックスを取得する
    val dtos = StringAnalyzer
      .analyze(text)
      .map(x => WordIndicesDAO.select(session, x.word))
      .toSet

    // すべての単語に含まれる場所IDの一覧を取得
    val intersectIds = dtos
      .map(_.map(_.resourceLocationId).toSet)
      .reduceLeftOption { (a, b) => a.intersect(b) }
      .getOrElse(Set.empty)

    // 対象DTOを展開
    val groupedDtos = dtos
      .flatten
      .filter(x => intersectIds.contains(x.resourceLocationId))
      .groupBy(_.resourceLocationId)

    // 結果リストに畳み込む
    val mergedDtos = groupedDtos.values.foldLeft(Seq.newBuilder[WordIndicesDTO]) { (builder, seq) =>
      val indices = seq.flatMap(_.indices).groupBy(_._1).mapValues(f => f.flatMap(_._2))
      val mergedDto = seq.find { x => true } match {
        case Some(s) => WordIndicesDTO(
          s.word,
          s.resourceLocationId,
          indices.values.flatten.size,
          s.resourceUpdated,
          s.resourceUri,
          s.resourceName,
          indices)
        case None => WordIndicesDTO()
      }
      builder += mergedDto
    }.result

    order match {
      case SearchResultOrder.COUNT_ASC             => mergedDtos.sortBy { x => x.count }
      case SearchResultOrder.COUNT_DESC            => mergedDtos.sortBy { x => x.count }.reverse
      case SearchResultOrder.RESOURCE_UPDATED_ASC  => mergedDtos.sortBy { x => x.resourceUpdated }.reverse
      case SearchResultOrder.RESOURCE_UPDATED_DESC => mergedDtos.sortBy { x => x.resourceUpdated }.reverse
      case SearchResultOrder.RESOURCE_URI_ASC      => mergedDtos.sortBy { x => x.resourceUri }
      case SearchResultOrder.RESOURCE_URI_DESC     => mergedDtos.sortBy { x => x.resourceUri }.reverse
      case SearchResultOrder.RESOURCE_NAME_ASC     => mergedDtos.sortBy { x => x.resourceName }
      case SearchResultOrder.RESOURCE_NAME_DESC    => mergedDtos.sortBy { x => x.resourceName }.reverse
    }
  }

  private def getKeyString(title: String, value: String) = {
    if (StringUtils.isNotEmpty(value)) { s"$title: $value" } else { StringUtils.EMPTY }
  }
}
