package logic.indexer

import java.net.URI

import logic.IndexerResource
import models.{ Content, IndexerResult }
import utils.ReflectionUtils

trait FileIndexer {
  def getResourceTypeName: String
  def getKeyTitles: Tuple3[String, String, String]
  def getPriority: Int
  def isTarget(uri: URI): Boolean
  def generateIndex(resource: IndexerResource): IndexerResult

  def getClassName: String = ReflectionUtils.toType(this.getClass).typeSymbol.fullName

  private val maxLengthOfResult = 60;

  protected def fillSibilingContent(contents: Seq[Content]) = {
    contents.sliding(2).foreach {
      case elm1 :: elm2 :: Nil => {
        elm1.nextContent = elm2.content.size match {
          case s if s > maxLengthOfResult => elm2.content.slice(0, maxLengthOfResult) + "..."
          case _                          => elm2.content.slice(0, maxLengthOfResult)
        }
        elm2.prevContent = elm1.content.size match {
          case s if s > maxLengthOfResult => "..." + elm1.content.slice(s - maxLengthOfResult - 1, s - 1)
          case _                          => elm1.content.slice(0, maxLengthOfResult)
        }
      }
      case _ =>
    }
  }
}
