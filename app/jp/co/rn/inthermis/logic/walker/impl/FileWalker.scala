package jp.co.rn.inthermis.logic.walker.impl

import java.io.{ BufferedInputStream, InputStream }
import java.net.URI
import java.nio.file.{ Files, Paths }
import java.time.LocalDateTime

import scala.collection.JavaConversions._

import org.apache.commons.io.FilenameUtils

import jp.co.rn.inthermis.logic.walker.{ ResourceWalker, ResourceWalkerConfig }
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.utils.FileTimeUtils

object FileWalker extends ResourceWalker {

  override def walk(config: ResourceWalkerConfig, generateIndex: IndexerResource => Unit): Unit = {
    Files.walk(Paths.get(config.uri))
      .iterator
      .filter(p => !Files.isDirectory(p))
      .map { p =>
        LocalFileResource(
          p.toUri,
          p.toString,
          FilenameUtils.getBaseName(p.toString),
          Files.size(p),
          FileTimeUtils.getCreated(p.toUri),
          FileTimeUtils.getCreated(p.toUri))
      }.foreach { generateIndex }
  }
}

case class LocalFileResource(
  override val uri: URI,
  override val displayLocation: String,
  override val name: String,
  override val size: Long,
  override val created: LocalDateTime,
  override val lastModified: LocalDateTime)
    extends IndexerResource {

  override def getInputStream: InputStream = {
    new BufferedInputStream(Files.newInputStream(Paths.get(uri)))
  }
}
