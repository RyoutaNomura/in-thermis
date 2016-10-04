package jp.co.rn.inthermis.logic.walker.impl

import java.io.InputStream
import java.net.URI
import java.time.{ Instant, LocalDateTime, ZoneOffset }

import jcifs.smb.{ NtlmPasswordAuthentication, SmbFile }
import jp.co.rn.inthermis.logic.walker.{ ResourceWalker, ResourceWalkerConfig }
import jp.co.rn.inthermis.models.IndexerResource

object CIFSWalker extends ResourceWalker {

  val cifsAuthDomain = "CIFS_AUTH_DOMAIN"
  val cifsAuthUser = "CIFS_AUTH_USER"
  val cifsAuthPass = "CIFS_AUTH_PASS"

  override def walk(config: ResourceWalkerConfig, generateIndex: IndexerResource => Unit): Unit = {

    val auth = new NtlmPasswordAuthentication(
      config.props.get(cifsAuthDomain) match {
        case Some(s) => s
        case None    => throw new IllegalArgumentException(s"$cifsAuthDomain not found in props")
      },
      config.props.get(cifsAuthUser) match {
        case Some(s) => s
        case None    => throw new IllegalArgumentException(s"$cifsAuthUser not found in props")
      },
      config.props.get(cifsAuthPass) match {
        case Some(s) => s
        case None    => throw new IllegalArgumentException(s"$cifsAuthPass not found in props")
      });
    this.walkTree(new SmbFile(config.uri.toString, auth), generateIndex)
  }

  private def walkTree(parent: SmbFile, generateIndex: IndexerResource => Unit): Unit = {
    parent.listFiles.foreach { child =>
      child.isDirectory match {
        case true => walkTree(child, generateIndex)
        case false => generateIndex(
          CIFSResource(
            child.getURL.toURI,
            child.getUncPath,
            child.getName,
            child.getContentLength,
            LocalDateTime.ofInstant(Instant.ofEpochMilli(child.getLastModified), ZoneOffset.UTC),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(child.getLastModified), ZoneOffset.UTC)))
      }
    }
  }
}

case class CIFSResource(
  override val uri: URI,
  override val displayLocation: String,
  override val name: String,
  override val size: Long,
  override val created: LocalDateTime,
  override val lastModified: LocalDateTime)
    extends IndexerResource {

  override def getInputStream: InputStream = {
    new SmbFile(uri.toString).getInputStream
  }
}
