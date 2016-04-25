package logic.indexer.impl

import java.io.File
import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.odftoolkit.simple.TextDocument

import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }
import utils.FileTimeUtils

object OdtIndexer extends FileIndexer {

  override def getResourceTypeName: String = "OpenDocument Word Processor Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Paragraph", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case v if v.endsWith(".odt") => true
    case _                       => false
  }

  override def generateIndex(uri: URI): IndexerResult = {
    val document = TextDocument.loadDocument(new File(uri))
    val contents = document.getParagraphIterator.map { _.getTextContent }.zipWithIndex.map {
      case (p, pno) =>
        val indices = StringAnalyzer.analyze(p).map { x => (x.word, x.start, x.length) }
        Content(pno + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, p, StringUtils.EMPTY, StringUtils.EMPTY, indices)
    }.toList

    IndexerResult(
      uri,
      FilenameUtils.getBaseName(Paths.get(uri).toString()),
      Files.size(Paths.get(uri)),
      FileTimeUtils.getCreated(uri),
      FileTimeUtils.getLastModified(uri),
      contents,
      this.getClassName,
      new Date)
  }

}