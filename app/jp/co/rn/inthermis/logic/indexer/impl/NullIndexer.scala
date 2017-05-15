package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import org.apache.commons.lang3.StringUtils

import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import jp.co.rn.inthermis.models.ContentIndexerResult

object NullIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Null"

  override def getKeyTitles: Tuple3[String, String, String] = {
    (StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY)
  }
  override def getPriority: Int = -1

  override def getIconCssClassName: String = "fa-file-o"

  override def isTarget(uri: URI): Boolean = false

  override def generateIndex(resource: IndexerResource): LineIndexerResult = LineIndexerResult()

  override def generateContentIndex(resource: IndexerResource): Option[ContentIndexerResult] = Option.empty[ContentIndexerResult]
  
  override val isShowAsCriteria = false
}
