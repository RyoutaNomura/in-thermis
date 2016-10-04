package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.text.PDFTextStripper

import com.google.common.base.Splitter

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.{ Content, IndexerResource, IndexerResult }

object PdfIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Adobe PDF"

  override def getKeyTitles: Tuple3[String, String, String] = ("Page", "Line", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-pdf-o"

  override def isTarget(uri: URI): Boolean = uri.toString().endsWith(".pdf")

  override def generateIndex(resource: IndexerResource): IndexerResult = {

    val is = resource.getInputStream
    val rabfis = new RandomAccessBufferedFileInputStream(is)

    try {
      val parser = new PDFParser(rabfis)
      parser.parse

      val pd = parser.getPDDocument
      try {
        val stripper = new PDFTextStripper
        val contents =
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
          resource,
          contents,
          this.getClassName)

      } finally {
        pd.close
      }
    } finally {
      rabfis.close()
      is.close()
    }

  }

}
