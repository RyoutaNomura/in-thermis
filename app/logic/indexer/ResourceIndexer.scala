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

object ResourceIndexer {

  def createIndex(helper: CassandraHelper, uri: URI) {
    uri.getScheme match {
      case "file" => createFileIndex(helper, uri)
      case _      => println("Only file scheme allowed.")
    }
  }

  private def createFileIndex(helper: CassandraHelper, uri: URI) {

    val locationDAO = ResourceLocationDAO(helper)
    val contentDAO = ResourceContentDAO(helper)
    val wordIndiceDAO = WordIndicesDAO(helper)
    
    def createAndPersistIndex(uri: URI) {
      val indexer = FileIndexerFactory.create(uri)
      val indexerResult = indexer.generateIndex(uri)
      locationDAO.insert(indexerResult.locationDTO)
      indexerResult.contentDTOs.foreach { contentDAO.insert }
      indexerResult.wordIndicesDTOs.foreach { wordIndiceDAO.insert }
    }
    
    def deleteIndex(id: UUID) {
      locationDAO.delete(id)
      contentDAO.delete(id)
      wordIndiceDAO.delete(id)
      println(s"resource_location found. deleted ${id}")
    }

    Files.walk(Paths.get(uri)).iterator
      .filter { p => !Files.isDirectory(p) }
      .foreach { p =>
        val lastModified = new Date(Files.getLastModifiedTime(p).toMillis)

        locationDAO.find(p.toUri.toString) match {
          case Some(s) if s.indexGenerated.before(lastModified) =>
            println(s"resource_location found.")
            deleteIndex(s.id)
            createAndPersistIndex(p.toUri)
          case Some(s) =>
            println(s"resource_location found. index creaion skipped.")
          case None =>
            println(s"resource_location not found")
            createAndPersistIndex(p.toUri)
        }
      }
  }
}