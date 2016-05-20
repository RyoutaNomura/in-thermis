package logic.indexer.impl

import java.net.URI
import java.nio.file.Paths
import java.util.Date

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup

import logic.IndexerResource
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }
import utils.CharsetUtils

object HtmlIndexer extends FileIndexer {

  override def getResourceTypeName: String = "HTML"

  override def getKeyTitles: Tuple3[String, String, String] = ("Line", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case s if s.endsWith(".html") => true
    case s if s.endsWith(".htm")  => true
    case _                        => false
  }

  override def generateIndex(resource: IndexerResource): IndexerResult = {
    val is = resource.getInputStream

    try {
      val charset = CharsetUtils.detectEncoding(is)
      val document = Jsoup.parse(is, charset, Paths.get(resource.uri).toAbsolutePath().toString)

      val contents = document.text().lines.zipWithIndex
        .map {
          case (line, lineNo) =>
            val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
            Content(lineNo + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices)
        }.toList
      fillSibilingContent(contents)

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
