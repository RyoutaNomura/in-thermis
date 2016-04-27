package logic.walker.impl

import java.net.URI
import java.util.Date

import jcifs.smb.SmbFile
import logic.indexer.entity.IndexerResource
import logic.walker.ResourceWalker

object CIFSWalker extends ResourceWalker {
  private val uri: URI = URI.create("")
  private val user: String = ""
  private val pass: String = ""

  override def walk(uri: URI, generateIndex: IndexerResource => Unit): Unit = {
    walkTree(new SmbFile(uri.toString), generateIndex)
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
            new Date(child.getLastModified),
            new Date(child.getLastModified)))
      }
    }
  }
}

case class CIFSResource(
  val uri: URI,
  val displayLocation: String,
  val name: String,
  val size: Long,
  val created: Date,
  val lastModified: Date)
    extends IndexerResource {

  override def getInputStream = {
    new SmbFile(uri.toString).getInputStream
  }
}