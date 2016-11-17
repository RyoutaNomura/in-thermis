package jp.co.rn.inthermis.logic.walker

import java.time.ZoneOffset
import java.util.{ Date, UUID }
import com.datastax.driver.core.Session
import jp.co.rn.inthermis.daos.{ ResourceContentDAO, ResourceLocationDAO, WordIndexDAO }
import jp.co.rn.inthermis.logic.indexer.{ FileIndexer, FileIndexerFactory }
import jp.co.rn.inthermis.models.{ IndexerResource, IndexerResult }
import jp.co.rn.inthermis.utils.ReflectionUtils
import play.Logger
import scala.util.Try
import java.nio.charset.MalformedInputException
import java.net.URI
import java.io.InputStream

trait ResourceWalker {

  private val logger = Logger.of(this.getClass)

  val walkerName: String = ReflectionUtils.toType(this.getClass).typeSymbol.fullName

  val specificFileIndexer: Option[FileIndexer] = Option.empty

  def walk(config: ResourceWalkerConfig, generateIndex: IndexerResource => Unit): Unit

  def walk(session: Session, config: ResourceWalkerConfig): Unit = {

    def generateIndex = (resource: IndexerResource) => {
      // インデクサ取得
      val indexer = specificFileIndexer match {
        case Some(s) => s
        case _       => FileIndexerFactory.create(resource.uri)
      }
      // リソースの最終更新日時を取得
      val lastModified = Date.from(resource.lastModified.toInstant(ZoneOffset.UTC))

      // DBのインデックスを取得
      try {
        ResourceLocationDAO.selectByUri(session, resource.uri.toString) match {
          case Some(s) if (s.indexGenerated.before(lastModified)) => {
            // インデクサ作成日時 < リソース更新日時の場合は既存データ削除後に登録
            logger.debug(s"Index will be regenerated: ${resource.uri}")
            deleteLocationById(session, s.resourceLocationId)
            persistIndex(session, indexer.generateIndex(resource))
          }
          case Some(s) => {
            // 何もしない
            logger.debug(s"Index is latest: ${resource.uri}.")
          }
          case None => {
            // 存在しない場合はインデックスを作成して登録
            logger.debug(s"Index not found. Index will be generated: ${resource.uri}")
            persistIndex(session, indexer.generateIndex(resource))
          }
        }
      } catch {
        case e: Exception => logger.error(e.getLocalizedMessage)
      }
    }

    this.walk(config, generateIndex)
  }

  def deleteAll(session: Session, config: ResourceWalkerConfig): Unit = {
    val locations = ResourceLocationDAO.selectByWalkerName(session, config.implClassName)
    locations.foreach { x => deleteLocationById(session, x.resourceLocationId) }
  }

  def deleteLocationById(session: Session, id: UUID) {
    logger.debug(s"$id deleting...")

    WordIndexDAO.selectByResourceLocationId(session, id)
      .foreach { wordIndex =>
        WordIndexDAO.delete(session, wordIndex.word, wordIndex.resourceContentId)
      }

    ResourceContentDAO.selectByResourceLocationId(session, id)
      .map(_.resourceContentId)
      .distinct
      .foreach { contentId =>
        ResourceContentDAO.delete(session, contentId)
      }

    ResourceLocationDAO.delete(session, id)
  }

  private def persistIndex(session: Session, indexerResult: IndexerResult) {
    ResourceLocationDAO.insert(session, indexerResult.generateResourceLocationDTO(walkerName))
    indexerResult.generateResourceContentDTOs.foreach { dto => ResourceContentDAO.insert(session, dto) }
    indexerResult.generateWordIndexDTOs(walkerName).foreach { dto =>
      WordIndexDAO.insert(session, dto)
    }
  }

}
