package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.util.control.Exception._

import org.apache.commons.lang3.StringUtils
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.Content
import jp.co.rn.inthermis.models.ContentIndexerResult
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import play.Logger

object DocIndexer extends FileIndexer {

  private val logger = Logger.of(this.getClass)

  override def getResourceTypeName: String = "Microsoft Word"

  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet", "Row", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-word-o"

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case s if s.endsWith(".doc") => true
    case s if s.endsWith(".docx") => true
    case _ => false
  }

  override def generateContentIndex(resource: IndexerResource): Option[ContentIndexerResult] = {

    val is = resource.getInputStream
    val extractor = resource.uri.toString match {
      case s if s.endsWith(".doc") => new WordExtractor(new HWPFDocument(is))
      case s if s.endsWith(".docx") => new XWPFWordExtractor(new XWPFDocument(is))
      case _ => throw new IllegalArgumentException(s"${resource.uri} is not supported.")
    }

    allCatch withApply { e =>
      logger.error(s"error occurred during generating index of ${resource.uri}", e)
      Option.empty

    } andFinally {
      extractor.close
      is.close

    } apply {
      Option(ContentIndexerResult(resource, Map(Seq.empty -> extractor.getText), this.getClassName))
    }
  }

  override def generateIndex(resource: IndexerResource): LineIndexerResult = {

    val is = resource.getInputStream
    val extractor = resource.uri.toString match {
      case s if s.endsWith(".doc") => new WordExtractor(new HWPFDocument(is))
      case s if s.endsWith(".docx") => new XWPFWordExtractor(new XWPFDocument(is))
      case _ => throw new IllegalArgumentException(s"${resource.uri} is not supported.")
    }

    try {
      val contents = extractor.getText.lines.zipWithIndex.map {
        case (line, lineNo) =>
          val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
          Content(lineNo + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices)
      }.toList
      fillSibilingContent(contents)

      LineIndexerResult(
        resource,
        contents,
        this.getClassName)

    } finally {
      extractor.close()
      is.close()
    }
  }
}
