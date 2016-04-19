package logic.walker

import java.net.URI
import models.IndexerResult
import com.datastax.driver.core.Session
import java.util.UUID
import daos.ResourceLocationDAO
import daos.ResourceContentDAO
import daos.WordIndicesDAO
import logic.indexer.FileIndexerFactory
import logic.indexer.FileIndexer

trait ResourceWalker {

  def walk(uri: URI, foreach: URI => Unit): Unit

  def walk(session: Session, uri: URI): Unit = {
    walk(uri, uri => {
      val indexer = FileIndexerFactory.create(uri)
      // リソースの最終更新日時を取得      
      val lastModified = indexer.getLastModified(uri)

      // DBのインデックスを取得
      ResourceLocationDAO.find(session, uri.toString) match {
        case Some(s) if (s.indexGenerated.before(lastModified)) => {
          // インデクサ作成日時 < リソース更新日時の場合は既存データ削除後に登録
          println(s"old index found at ${uri}. index will be regenerated.")
          deleteIndex(session, s.id)
          persistIndex(session, indexer.generateIndex(uri))
        }
        case Some(s) => {
          // 何もしない
          println(s"latest index fount at ${uri}.")
        }
        case None => {
          // 存在しない場合はインデックスを作成して登録
          println(s"no index fount at ${uri}. index will be generated.")
          persistIndex(session, indexer.generateIndex(uri))
        }
      }
    })
  }

  private def deleteIndex(session: Session, id: UUID) {
    ResourceLocationDAO.delete(session, id)
    ResourceContentDAO.delete(session, id)
    WordIndicesDAO.delete(session, id)
  }

  private def persistIndex(session: Session, indexerResult: IndexerResult) {
    ResourceLocationDAO.insert(session, indexerResult.locationDTO)
    indexerResult.contentDTOs.foreach { dto => ResourceContentDAO.insert(session, dto) }
    indexerResult.wordIndicesDTOs.foreach { dto => WordIndicesDAO.insert(session, dto) }
  }
}