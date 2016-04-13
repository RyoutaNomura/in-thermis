package logic.indexer

import collection.JavaConversions._
import java.net.URI
import org.apache.commons.lang3.StringUtils
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files
import java.util.Date
import daos.ResourceLocationDAO
import daos.WordIndicesDAO
import daos.ResourceContentDAO
import utils.CassandraHelper
import dtos.ResourceLocationDTO
import java.util.UUID
import java.time.ZoneOffset
import java.time.ZoneId
import com.datastax.driver.core.Session

object ResourceIndexer {

  def generateIndex(session: Session, uri: URI) {
    uri.getScheme match {
      case "file" => generateFileIndex(session, uri)
      case _      => println("Only file scheme allowed.")
    }
  }

  private def generateFileIndex(session: Session, uri: URI) {
    def generateAndPersistIndex(uri: URI) {
      val indexerResult = FileIndexerFactory.create(uri).generateIndex(uri)
      ResourceLocationDAO.insert(session, indexerResult.locationDTO)
      indexerResult.contentDTOs.foreach { dto => ResourceContentDAO.insert(session, dto) }
      indexerResult.wordIndicesDTOs.foreach { dto => WordIndicesDAO.insert(session, dto) }
    }

    def deleteIndex(session: Session, id: UUID) {
      ResourceLocationDAO.delete(session, id)
      ResourceContentDAO.delete(session, id)
      WordIndicesDAO.delete(session, id)
      println(s"resource_location found. deleted ${id}")
    }

    Files.walk(Paths.get(uri)).iterator
      .filter { p => !Files.isDirectory(p) }
      .foreach { p =>
        val lastModified = new Date(Files.getLastModifiedTime(p).toMillis)

        ResourceLocationDAO.find(session, p.toUri.toString) match {
          case Some(s) if s.indexGenerated.before(lastModified) =>
            println(s"resource_location found. " + p)
            deleteIndex(session, s.id)
            generateAndPersistIndex(p.toUri)
          case Some(s) =>
            println(s"resource_location found. index creaion skipped. " + p)
          case None =>
            println(s"resource_location not found. " + p)
            generateAndPersistIndex(p.toUri)
        }
      }
  }
}