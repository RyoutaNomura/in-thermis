package logic.indexer.impl

import java.net.URI
import java.util.Date

import scala.io.Source

import org.apache.commons.lang3.StringUtils

import logic.IndexerResource
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }
import utils.CharsetUtils

object TextIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Plain Text"

  override def getKeyTitles: Tuple3[String, String, String] = ("Line", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString() match {
    case s if s.endsWith(".txt") => true
    case s if s.endsWith(".md")  => true
    case _                       => false
  }

  override def generateIndex(resource: IndexerResource): IndexerResult = {
    var is = resource.getInputStream
    val source = Source.fromInputStream(is)(CharsetUtils.getCodec(is))

    try {
      val contents = source.getLines.zipWithIndex
        .map {
          case (line, lineNo) =>
            val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
            Content(lineNo + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices)
        }.toList
      fillSibilingContent(contents)

      IndexerResult(
        resource,
        contents,
        this.getClassName)

    } finally {
      source.close()
      is.close()
    }
  }
}
