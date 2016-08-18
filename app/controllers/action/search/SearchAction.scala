package controllers.action.search

import scala.annotation.migration
import scala.collection.{ Seq, mutable }
import org.apache.commons.lang3.StringUtils
import com.datastax.driver.core.Session
import daos.WordIndexDAO
import dtos.WordIndexDTO
import enums.SearchResultOrder
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexerFactory
import play.Logger
import utils.CassandraHelper
import scala.collection.mutable.ArrayBuffer

object SearchAction {

  val logger = Logger.of(this.getClass)

  //  def execute(word: String, order: SearchResultOrder, fetchSize: Int, currentPage: Int): SearchResponse = {
  def execute(criteria: SearchCriteria) = {
    val starttime = System.currentTimeMillis()
    val session = CassandraHelper.getSession

    try {
      // 単語リストを取得して、指定オーダー順に並べる
      var wordIndices = loadIndices(session, criteria).sortWith(createSortFunction(criteria.order))
      // 結果の組み立て
      val searchResults = wordIndices.map { x =>
        // インデクサクラス名
        val indexer = FileIndexerFactory.create(x.resourceIndexerName)
        // キー情報
        val keyStr = Seq(
          getKeyString(indexer.getKeyTitles._1, x.contentKey1),
          getKeyString(indexer.getKeyTitles._2, x.contentKey2),
          getKeyString(indexer.getKeyTitles._3, x.contentKey3))
          .filter { x => !x.isEmpty }
          .mkString(" / ")

        // 結果オブジェクトを生成
        SearchResult(
          criteria.text,
          x.resourceUri,
          x.resourceName,
          x.resourceSize,
          indexer.getResourceTypeName,
          x.resourceLastModified,
          x.resourceLastModified,
          keyStr,
          x.content,
          x.prevContent,
          x.nextContent,
          x.indices.toSet,
          x.resourceIndexerName)
      }.toSeq

      SearchResponse(
        searchResults,
        wordIndices.size,
        System.currentTimeMillis - starttime,
        wordIndices.size < (criteria.currentPage + 1) * criteria.fetchSize)

    } finally {
      session.closeAsync()
    }
  }

  private def loadIndices(session: Session, criteria: SearchCriteria): Seq[WordIndexDTO] = {

    // 検索ワードを形態素解析
    val words = StringAnalyzer.analyze(criteria.text)

    // 形態素解析の結果単語でインデックスを取得する
    val resourceIndexMap = words.flatMap { ar =>

      var wordIndices = WordIndexDAO.select(session, ar.word)

      // 条件がある場合は、単語+条件で条件数分検索して和集合をとる
      criteria.resourceIndices.map { x =>
        wordIndices = wordIndices.intersect(WordIndexDAO.selectByResourceIndexerName(session, ar.word, x))
      }
      criteria.resourceWalkers.map { x =>
        wordIndices = wordIndices.intersect(WordIndexDAO.selectByResourceWalkerName(session, ar.word, x))
      }
      wordIndices
    }.groupBy { x => x.resourceLocationId }

    // リソースごとに、代表コンテンツを取得
    resourceIndexMap
      .foldLeft(Seq.newBuilder[WordIndexDTO]) { (builder, resourceIndex) =>
        builder += resourceIndex._2
          .groupBy(_.resourceContentId)
          .values.map { dtos =>
            var newIndices = dtos.foldLeft(mutable.Map.empty[Int, Int]) { (map, dto) =>
              map ++= dto.indices
            }.toMap
            WordIndexDTO(
              dtos.head.word,
              dtos.head.resourceContentId,
              newIndices,
              newIndices.size,
              dtos.head.content,
              dtos.head.prevContent,
              dtos.head.nextContent,
              dtos.head.contentKey1,
              dtos.head.contentKey2,
              dtos.head.contentKey3,
              dtos.head.resourceUri,
              dtos.head.resourceDisplayLocation,
              dtos.head.resourceName,
              dtos.head.resourceSize,
              dtos.head.resourceWalkerName,
              dtos.head.resourceIndexerName,
              dtos.head.resourceLastModified,
              dtos.head.resourceLocationId)
          }
          .toSeq.sortWith((a1, a2) => {
            (a1.indices.size < a2.indices.size) &&
              (a1.contentKey1 > a2.contentKey1) &&
              (a1.contentKey2 > a2.contentKey2) &&
              (a1.contentKey3 > a2.contentKey3)
          })
          .head
      }
      .result
  }

  private def createSortFunction(order: SearchResultOrder): ((WordIndexDTO, WordIndexDTO) => Boolean) = {
    order match {
      case SearchResultOrder.COUNT_ASC             => ((a, b) => a.indicesCount - b.indicesCount > 0)
      case SearchResultOrder.COUNT_DESC            => ((a, b) => a.indicesCount - b.indicesCount < 0)
      case SearchResultOrder.RESOURCE_UPDATED_ASC  => ((a, b) => a.resourceLastModified.before(b.resourceLastModified))
      case SearchResultOrder.RESOURCE_UPDATED_DESC => ((a, b) => a.resourceLastModified.after(b.resourceLastModified))
      case SearchResultOrder.RESOURCE_URI_ASC      => ((a, b) => a.resourceUri.compareTo(b.resourceUri) > 0)
      case SearchResultOrder.RESOURCE_URI_DESC     => ((a, b) => a.resourceUri.compareTo(b.resourceUri) < 0)
      case SearchResultOrder.RESOURCE_NAME_ASC     => ((a, b) => a.resourceName.compareTo(b.resourceName) > 0)
      case SearchResultOrder.RESOURCE_NAME_DESC    => ((a, b) => a.resourceName.compareTo(b.resourceName) < 0)
    }
  }

  private def getKeyString(title: String, value: String) = {
    if (StringUtils.isNotEmpty(value)) { s"$title: $value" } else { StringUtils.EMPTY }
  }
}