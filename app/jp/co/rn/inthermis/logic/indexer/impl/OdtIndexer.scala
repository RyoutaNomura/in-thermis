package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.odftoolkit.simple.TextDocument

import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.{ Content, IndexerResult }

object OdtIndexer extends FileIndexer {

  override def getResourceTypeName: String = "OpenDocument Text"

  override def getKeyTitles: Tuple3[String, String, String] = ("Paragraph", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-word-o"

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case v if v.endsWith(".odt") => true
    case _                       => false
  }

  override def generateIndex(resource: IndexerResource): IndexerResult = {

    val is = resource.getInputStream

    try {
      val document = TextDocument.loadDocument(is)
      val contents = document.getParagraphIterator.map { _.getTextContent }.zipWithIndex.map {
        case (p, pno) =>
          val indices = StringAnalyzer.analyze(p).map { x => (x.word, x.start, x.length) }
          Content(pno + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, p, StringUtils.EMPTY, StringUtils.EMPTY, indices)
      }.toList

      IndexerResult(
        resource,
        contents,
        this.getClassName)

    } finally {
      is.close()
    }
  }

}
