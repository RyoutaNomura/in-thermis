package logic.indexer.impl

import java.io.File
import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup

import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }
import utils.{ CharsetUtils, FileTimeUtils }

object HtmlIndexer extends FileIndexer {

  override def getResourceTypeName: String = "HTML"

  override def getKeyTitles: Tuple3[String, String, String] = ("Line: ", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case s if s.endsWith(".html") => true
    case s if s.endsWith(".htm")  => true
    case _                        => false
  }

  override def generateIndex(uri: URI): IndexerResult = {
    val file = new File(uri)
    val charset = CharsetUtils.detectEncoding(uri)
    val document = Jsoup.parse(file, charset)

    val contents = document.text().lines.zipWithIndex
      .map {
        case (line, lineNo) =>
          val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
          Content(lineNo + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices)
      }.toList
    fillSibilingContent(contents)
    //    val contents = document.textNodes.zipWithIndex
    //      .map {
    //        case (node, nodeNo) =>
    //          val indices = StringAnalyzer.analyze(node.text).map { x => (x.word, x.start, x.length) }
    //          Content(nodeNo + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, node.text, StringUtils.EMPTY, StringUtils.EMPTY, indices)
    //      }.toList
    //    fillSibilingContent(contents)

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