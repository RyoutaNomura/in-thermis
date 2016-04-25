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
import org.odftoolkit.simple.PresentationDocument

object OdpIndexer extends FileIndexer {

  override def getResourceTypeName: String = "OpenDocument Presentation Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Slide", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case v if v.endsWith(".odp") => true
    case _                       => false
  }

  override def generateIndex(uri: URI): IndexerResult = {
    val document = PresentationDocument.loadDocument(new File(uri))
    val contents = (0 until document.getSlideCount)
      .map { slideNo =>
        val content = document.getSlideByIndex(slideNo).getTextboxIterator
          .foldLeft(StringBuilder.newBuilder)((sb, textbox) => sb.append(textbox.getTextContent)).toString
        (slideNo, content)
      }.map {
        case (no, c) =>
          val indices = StringAnalyzer.analyze(c).map { x => (x.word, x.start, x.length) }
          Content(no + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, c, StringUtils.EMPTY, StringUtils.EMPTY, indices)
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