package jp.co.rn.inthermis.logic.walker

import java.time.ZoneOffset
import java.util.Date
import java.util.UUID

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.datastax.driver.core.Session

import jp.co.rn.inthermis.daos._
import jp.co.rn.inthermis.elasticsearch.ElasticSearchRequestHandler
import jp.co.rn.inthermis.logic.indexer._
import jp.co.rn.inthermis.models._
import jp.co.rn.inthermis.utils.ReflectionUtils
import play.Logger
import jp.co.rn.inthermis.logic.ResourceIndexer
import jp.co.rn.inthermis.elasticsearch.ElasticSearchCriteria
import jp.co.rn.inthermis.elasticsearch.Highlight
import jp.co.rn.inthermis.elasticsearch.MatchQuery

trait ResourceWalker {

  private val logger = Logger.of(this.getClass)

  val walkerName: String = ReflectionUtils.toType(this.getClass).typeSymbol.fullName

  val specificFileIndexer: Option[FileIndexer] = Option.empty

  /**
   * 継承クラス側で実装する関数
   */
  def walk(config: ResourceWalkerConfig, generateIndex: IndexerResource => Unit): Unit

  /**
   * Indexerから呼び出されるwalk関数
   * 各クラスで実装されたwalk関数を呼び出す。
   */
  def walk(session: Session, config: ResourceWalkerConfig): Unit = this.walk(config, generateIndex(session)_)

  /**
   * インデックス生成関数
   */
  private def generateIndex(session: Session)(resource: IndexerResource): Unit = {

    specificFileIndexer ++ FileIndexerFactory.create(resource.uri) headOption match {
      case Some(indexer) => {
        logger.debug(s"${indexer} selected for ${resource.uri}")
        findAndPersistIndex(indexer, Date.from(resource.lastModified.toInstant(ZoneOffset.UTC)))
      }
      case None => logger.debug(s"indexer not found for ${resource.uri}")
    }

    def findAndPersistIndex(indexer: FileIndexer, lastModified: Date) = {
      Try {
        ResourceLocationDAO.selectByUri(session, resource.uri.toString) match {
          case Some(s) if s.indexGenerated.before(lastModified) => {
            logger.debug(s"resource already indexed with id: ${s.resourceLocationId}, uri: ${resource.uri}")
            // インデクサ作成日時 < リソース更新日時の場合は既存データ削除後に登録
            deleteLocationById(session, s.resourceLocationId)
            logger.debug(s"old indices deleted")
            indexer.generateContentIndex(resource) match {
              case Some(s) => persistIndex(session, s)
              case None    =>
            }

          }
          case Some(s) => {
            logger.debug(s"resource already indexed with id: ${s.resourceLocationId}, uri: ${resource.uri}")
            // 何もしない
          }
          case None => {
            logger.debug(s"resource not yet indexed. uri: ${resource.uri}")
            // 存在しない場合はインデックスを作成して登録
            indexer.generateContentIndex(resource) match {
              case Some(s) => persistIndex(session, s)
              case None    =>
            }
          }
        }
      } match {
        case Success(v) =>
        case Failure(t) => throw t
      }
    }

    def persistIndex(session: Session, indexerResult: ContentIndexerResult): Unit = {

      val location = indexerResult.generateResourceLocationDTO(walkerName)
      //      val contents = indexerResult.generateResourceContentDTOs
      val indices = indexerResult.generateWordIndexESDTO(location.resourceLocationId, walkerName)

      indices match {
        case c if c.isEmpty => logger.debug(s"register skipped: contents is empty")
        case _ => {
          Try {
            ResourceLocationDAO.insert(session, location)
            //            contents.foreach { dto => ResourceContentDAO.insert(session, dto) }
            ElasticSearchRequestHandler.createIndices(indices)

          } match {
            case Success(v) => logger.debug(s"success to persist indices: ${location.resourceUri}")
            case Failure(f) => {
              ResourceLocationDAO.delete(session, location.resourceLocationId)
              //              contents.foreach { dto => ResourceContentDAO.delete(session, dto.resourceContentId) }

              logger.error(s"failed to persist indices: ${location.resourceUri} due to ${f.getClass}")
              throw f
            }
          }
        }
      }
    }
  }

  def deleteAll(session: Session, config: ResourceWalkerConfig): Unit = {
    val locations = ResourceLocationDAO.selectByWalkerName(session, config.implClassName)
    locations.foreach { x => deleteLocationById(session, x.resourceLocationId) }
  }

  def deleteLocationById(session: Session, id: UUID) {
    logger.debug(s"$id deleting...")

    ElasticSearchRequestHandler.deleteIndices(ElasticSearchCriteria(MatchQuery(Map("resourceLocationId" -> id.toString)), 0, 0, Highlight(), Seq.empty)) match {
      case Success(s) => {
        logger.error(s"$s indices deleted")
        ResourceLocationDAO.delete(session, id)
      }
      case Failure(f) => throw f
    }

  }

}
