package logic.indexer.impl

import java.net.URI
import models.IndexerResult
import models.IndexerResult
import logic.indexer.FileIndexer
import org.apache.commons.lang3.StringUtils

object NullIndexer extends FileIndexer {

  override def getPriority: Int = -1
  override def isTarget(uri: URI): Boolean = false
  override def generateIndex(uri: URI): IndexerResult = IndexerResult()
  override def getKeyTitles: Tuple3[String, String, String] = {
    (StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY)
  }
  override def getResourceTypeName: String = "Null"
}