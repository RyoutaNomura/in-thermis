package logic.indexer.impl

import java.net.URI
import java.util.Date

import org.apache.commons.lang3.StringUtils
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument

import logic.IndexerResource
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }

object DocIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Microsoft Word"

  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet", "Row", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case s if s.endsWith(".doc")  => true
    case s if s.endsWith(".docx") => true
    case _                        => false
  }

  override def generateIndex(resource: IndexerResource): IndexerResult = {

    val is = resource.getInputStream
    val extractor = resource.uri.toString match {
      case s if s.endsWith(".doc")  => new WordExtractor(new HWPFDocument(is))
      case s if s.endsWith(".docx") => new XWPFWordExtractor(new XWPFDocument(is))
      case _                        => throw new IllegalArgumentException(s"${resource.uri} is not supported.")
    }

    try {
      val contents = extractor.getText.lines.zipWithIndex.map {
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
      extractor.close()
      is.close()
    }
  }
}
