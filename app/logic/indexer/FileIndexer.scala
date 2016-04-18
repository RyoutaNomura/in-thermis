package logic.indexer

import java.net.URI
import models.IndexerResult
import models.Content
import java.util.Date
import java.nio.file.Files
import java.nio.file.Paths
import utils.ReflectionUtils

trait FileIndexer {
  def getResourceTypeName: String
  def getKeyTitles: Tuple3[String, String, String]
  def getPriority: Int
  def isTarget(uri: URI): Boolean
  def generateIndex(uri: URI): IndexerResult
  
  def getClassName = ReflectionUtils.toType(this.getClass).typeSymbol.fullName
  
  def getLastModified(uri: URI): Date = {
    new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis)
  }
  
  def fillSibilingContent(contents: Seq[Content]) = {
    contents.sliding(2).foreach {
      case elm1 :: elm2 :: Nil => {
        elm1.nextContent = elm2.content.size match {
          case s if s > 100 => elm2.content.slice(0, 100) + "..."
          case _            => elm2.content.slice(0, 100)
        }
        elm2.prevContent = elm1.content.size match {
          case s if s > 100 => "..." + elm1.content.slice(s - 101, s - 1)
          case _            => elm1.content.slice(0, 100)
        }
      }
      case _ =>
    }
  }

}