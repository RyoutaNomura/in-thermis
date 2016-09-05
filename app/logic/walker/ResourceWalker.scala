package logic.walker

import java.net.URI
import java.util.UUID
import com.datastax.driver.core.Session
import daos.{ ResourceContentDAO, ResourceLocationDAO, WordIndexDAO }
import logic.IndexerResource
import logic.indexer.FileIndexerFactory
import models.IndexerResult
import play.Logger
import utils.ReflectionUtils
import java.time.ZoneOffset
import java.util.Date
import logic.indexer.FileIndexer

trait ResourceWalker {

  private val logger = Logger.of(this.getClass)

  val walkerName: String = ReflectionUtils.toType(this.getClass).typeSymbol.fullName

  val specificFileIndexer: Option[FileIndexer] = Option.empty

  def walk(config: ResourceWalkerConfig, generateIndex: IndexerResource => Unit): Unit

  def walk(session: Session, config: ResourceWalkerConfig): Unit = {

    def generateIndex = (resource: IndexerResource) => {
      logger.debug(s"target: ${resource.name}")
      // インデクサ取得
      val indexer = specificFileIndexer match {
        case Some(s) => s
        case _       => FileIndexerFactory.create(resource.uri)
      }
      // リソースの最終更新日時を取得
      val lastModified = Date.from(resource.lastModified.toInstant(ZoneOffset.UTC))

      // DBのインデックスを取得
      ResourceLocationDAO.selectByUri(session, resource.uri.toString) match {
        case Some(s) if (s.indexGenerated.before(lastModified)) => {
          // インデクサ作成日時 < リソース更新日時の場合は既存データ削除後に登録
          logger.debug(s"index found at ${resource.uri}. index will be regenerated.")
          deleteLocationById(session, s.resourceLocationId)
          persistIndex(session, indexer.generateIndex(resource))
        }
        case Some(s) => {
          // 何もしない
          logger.debug(s"latest index fount at ${resource.uri}.")
        }
        case None => {
          // 存在しない場合はインデックスを作成して登録
          logger.debug(s"no index fount at ${resource.uri}. index will be generated.")
          persistIndex(session, indexer.generateIndex(resource))
        }
      }
    }

    this.walk(config, generateIndex)
  }

  def deleteAll(session: Session, config: ResourceWalkerConfig): Unit = {
    val locations = ResourceLocationDAO.selectByWalkerName(session, config.implClassName)
    locations.foreach { x => deleteLocationById(session, x.resourceLocationId) }
  }

  private def deleteLocationById(session: Session, id: UUID) {
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
