package logic.indexer.impl

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument

import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }

object DocIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Microsoft Word Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet", "Row", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString.endsWith(".doc") || uri.toString.endsWith(".docx")

  override def generateIndex(uri: URI): IndexerResult = {

    val stream = uri.toURL.openStream
    val extractor = uri.toString match {
      case s if s.endsWith(".doc")  => new WordExtractor(new HWPFDocument(stream))
      case s if s.endsWith(".docx") => new XWPFWordExtractor(new XWPFDocument(stream))
      case _                        => throw new IllegalArgumentException(s"$uri is not supported.")
    }

    try {
      val contents = extractor.getText.lines.zipWithIndex.map {
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
      extractor.close()
      stream.close()
    }
  }
}