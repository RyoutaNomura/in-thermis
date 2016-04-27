package logic.walker.impl

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.io.FilenameUtils

import logic.indexer.entity.IndexerResource
import logic.walker.ResourceWalker
import utils.FileTimeUtils

object FileWalker extends ResourceWalker {
  def walk(uri: URI, generateIndex: IndexerResource => Unit): Unit = {
    Files.walk(Paths.get(uri)).iterator.map { p =>
      LocalFileResource(
        p.toUri,
        p.toString,
        FilenameUtils.getBaseName(p.toString),
        Files.size(p),
        FileTimeUtils.getCreated(p.toUri),
        FileTimeUtils.getCreated(p.toUri))
    }
  }
}

case class LocalFileResource(
  val uri: URI,
  val displayLocation: String,
  val name: String,
  val size: Long,
  val created: Date,
  val lastModified: Date)
    extends IndexerResource {

  override def getInputStream = Files.newInputStream(Paths.get(uri))
}