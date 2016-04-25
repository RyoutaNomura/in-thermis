package logic.indexer.impl

import java.io.File
import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.apache.pdfbox.io.RandomAccessFile
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.text.PDFTextStripper

import com.google.common.base.Splitter

import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }
import utils.FileTimeUtils

object PdfIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Adobe PDF Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Page", "Line", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString().endsWith(".pdf")

  override def generateIndex(uri: URI): IndexerResult = {

    val file = new RandomAccessFile(new File(uri), "r")
    try {
      val parser = new PDFParser(file)
      parser.parse
      val pd = parser.getPDDocument

      val stripper = new PDFTextStripper
      var contents =
        (0 until pd.getNumberOfPages)
          .flatMap { pageIndex =>
            stripper.setStartPage(pageIndex)
            stripper.setEndPage(pageIndex)

            val pageContents = Splitter
              .on(System.lineSeparator())
              .split(stripper.getText(pd))
              .zipWithIndex
              .map {
                case (line, lineNo) =>
                  val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
                  Content(pageIndex + 1.toString, lineNo.toString, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices)
              }.toList

            fillSibilingContent(pageContents)
            pageContents
          }

      IndexerResult(
        uri,
        FilenameUtils.getBaseName(Paths.get(uri).toString()),
        Files.size(Paths.get(uri)),
        FileTimeUtils.getCreated(uri),
        FileTimeUtils.getLastModified(uri),
        contents,
        this.getClassName,
        new Date)

    } finally {
      file.close
    }

  }

}