package controllers.action.search

import org.apache.commons.lang3.StringUtils
import com.datastax.driver.core.Session
import daos.{ ResourceContentDAO, ResourceLocationDAO, WordIndicesDAO }
import dtos.WordIndicesDTO
import enums.SearchResultOrder
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexerFactory
import play.Logger
import utils.CassandraHelper
import daos.WordIndicesByKeyDAO
import daos.WordIndicesByKeyDAO
import daos.WordIndicesByKeyDAO

object SearchAction {

  val logger = Logger.of(this.getClass)

  //  def execute(word: String, order: SearchResultOrder, fetchSize: Int, currentPage: Int): SearchResponse = {
  def execute(criteria: SearchCriteria) = {

    val starttime = System.currentTimeMillis()

    val session = CassandraHelper.getSession

    try {
      // 単語リストを取得して、指定オーダー順に並べる
      val wordIndicesAll = sortIndices(loadIndices(session, criteria), criteria.order)
      val wordIndices = wordIndicesAll.slice(criteria.currentPage * criteria.fetchSize, (criteria.currentPage + 1) * criteria.fetchSize)

      // 取得した単語リストに含まれる場所の一覧を取得
      val locationIds = wordIndices.map { x => x.resourceLocationId }.toSet
      val locations = ResourceLocationDAO.select(session, locationIds)

      // 取得した単語リストに含まれる内容の一覧を取得
      val contentIds = wordIndices.map { x => x.indices.maxBy(_._2.size)._1 }.toSet
      val contents = ResourceContentDAO.select(session, contentIds)

      // 結果の組み立て
      val searchResults = wordIndices.map { x =>
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
          criteria.text,
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

      SearchResponse(
        searchResults,
        wordIndicesAll.size,
        System.currentTimeMillis - starttime,
        wordIndicesAll.size < (criteria.currentPage + 1) * criteria.fetchSize)

    } finally {
      session.closeAsync()
    }
  }

  private def loadIndices(session: Session, criteria: SearchCriteria): Seq[WordIndicesDTO] = {

    // 形態素解析の結果単語でインデックスを取得する
    val words = StringAnalyzer.analyze(criteria.text)
    words.map(x => WordIndicesByKeyDAO.)

    val dtos = StringAnalyzer
      .analyze(criteria.text)
      .map(x => WordIndicesByKeyDAO.select(
        session,
        x.word,
        criteria.dateRangeCriteria,
        criteria.resourceWalkers,
        criteria.resourceIndices))
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
    groupedDtos.values.foldLeft(Seq.newBuilder[WordIndicesDTO]) { (builder, seq) =>
      val indices = seq.flatMap(_.indices).groupBy(_._1).mapValues(f => f.flatMap(_._2))
      val mergedDto = seq.find { x => true } match {
        case Some(s) => WordIndicesDTO(
          s.word,
          s.resourceUpdated,
          s.resourceWalkerName,
          s.resourceIndexerName,
          s.resourceLocationId,
          s.resourceName,
          s.resourceUri,
          indices.values.flatten.size,
          indices)
        case None => WordIndicesDTO()
      }
      builder += mergedDto
    }.result
  }

  private def sortIndices(dtos: Seq[WordIndicesDTO], order: SearchResultOrder): Seq[WordIndicesDTO] = {

    order match {
      case SearchResultOrder.COUNT_ASC             => dtos.sortBy { x => x.count }
      case SearchResultOrder.COUNT_DESC            => dtos.sortBy { x => x.count }.reverse
      case SearchResultOrder.RESOURCE_UPDATED_ASC  => dtos.sortBy { x => x.resourceUpdated }.reverse
      case SearchResultOrder.RESOURCE_UPDATED_DESC => dtos.sortBy { x => x.resourceUpdated }.reverse
      case SearchResultOrder.RESOURCE_URI_ASC      => dtos.sortBy { x => x.resourceUri }
      case SearchResultOrder.RESOURCE_URI_DESC     => dtos.sortBy { x => x.resourceUri }.reverse
      case SearchResultOrder.RESOURCE_NAME_ASC     => dtos.sortBy { x => x.resourceName }
      case SearchResultOrder.RESOURCE_NAME_DESC    => dtos.sortBy { x => x.resourceName }.reverse
    }
  }

  private def getKeyString(title: String, value: String) = {
    if (StringUtils.isNotEmpty(value)) { s"$title: $value" } else { StringUtils.EMPTY }
  }
}
