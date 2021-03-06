package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.odftoolkit.simple.PresentationDocument

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.{ Content, IndexerResource, IndexerResult }

object OdpIndexer extends FileIndexer {

  override def getResourceTypeName: String = "OpenDocument Presentation"

  override def getKeyTitles: Tuple3[String, String, String] = ("Slide", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-powerpoint-o"

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case v if v.endsWith(".odp") => true
    case _                       => false
  }

  override def generateIndex(resource: IndexerResource): IndexerResult = {

    val is = resource.getInputStream

    try {
      val document = PresentationDocument.loadDocument(is)
      val contents = (0 until document.getSlideCount)
        .map { slideNo =>
          val content = document.getSlideByIndex(slideNo).getTextboxIterator
            .foldLeft(StringBuilder.newBuilder)((sb, textbox) => sb.append(textbox.getTextContent)).toString
          (slideNo, content)
        }.map {
          case (no, c) =>
            val indices = StringAnalyzer.analyze(c).map { x => (x.word, x.start, x.length) }
            Content((no + 1).toString, StringUtils.EMPTY, StringUtils.EMPTY, c, StringUtils.EMPTY, StringUtils.EMPTY, indices)
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
