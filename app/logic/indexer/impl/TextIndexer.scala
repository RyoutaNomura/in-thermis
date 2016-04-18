package logic.indexer.impl

import utils.ReflectionUtils
import org.apache.commons.lang3.StringUtils
import java.net.URI
import scala.io.Source
import logic.analyzer.StringAnalyzer
import models.Content
import logic.indexer.FileIndexer
import org.apache.commons.io.FilenameUtils
import java.nio.file.Paths
import java.nio.file.Files
import java.util.Date
import models.IndexerResult
import java.io.File

object TextIndexer extends FileIndexer {

  override def getPriority: Int = 0
  
  override def getResourceTypeName: String = "Plain Text File"
  
  override def getKeyTitles: Tuple3[String, String, String] = ("Line: ", StringUtils.EMPTY, StringUtils.EMPTY)
  
  override def isTarget(uri: URI): Boolean = uri.toString() match {
    case s if s.endsWith(".txt") => true
    case s if s.endsWith(".md")  => true
    case _                       => false
  }

  override def generateIndex(uri: URI): IndexerResult = {
    val source = Source.fromFile(new File(uri));
    try {
      val contents = source.getLines().zipWithIndex
        .map {
          case (line, lineNo) =>
            val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
            Content(lineNo.toString, StringUtils.EMPTY, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices)
        }.toList
      fillSibilingContent(contents)

      IndexerResult(
        uri,
        FilenameUtils.getBaseName(Paths.get(uri).toString()),
        Files.size(Paths.get(uri)),
        new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis()),
        new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis()),
        contents,
        this.getClassName,
        new Date)
    } finally {
      source.close()
    }
  }
}