package logic.indexer.impl

import java.net.URI
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.odftoolkit.simple.TextDocument

import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import logic.indexer.entity.IndexerResource
import models.{ Content, IndexerResult }

object OdtIndexer extends FileIndexer {

  override def getResourceTypeName: String = "OpenDocument Word Processor Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Paragraph", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

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
        this.getClassName,
        new Date)

    } finally {
      is.close()
    }
  }

}