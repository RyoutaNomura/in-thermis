package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.util.control.Exception._

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.Content
import jp.co.rn.inthermis.models.ContentIndexerResult
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import jp.co.rn.inthermis.utils.CharsetUtils
import play.Logger

object HtmlIndexer extends FileIndexer {

  private val logger = Logger.of(this.getClass)

  override def getResourceTypeName: String = "HTML"

  override def getKeyTitles: Tuple3[String, String, String] = ("Line", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-html5"

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case s if s.endsWith(".html") => true
    case s if s.endsWith(".htm") => true
    case _ => false
  }

  override def generateContentIndex(resource: IndexerResource): Option[ContentIndexerResult] = {
    val is = resource.getInputStream

    allCatch withApply { e =>
      logger.error(s"error occurred during indexing ${resource.uri}", e)
      Option.empty
      
    } andFinally {
      is.close
      
    } apply {
      val charset = CharsetUtils.detectEncoding(is)
      val document = Jsoup.parse(is, resource.getCodec.name, resource.uri.toString)
      Option(ContentIndexerResult(resource, Map(Seq.empty -> document.text), this.getClassName))
    }

  }

  override def generateIndex(resource: IndexerResource): LineIndexerResult = {
    val is = resource.getInputStream

    try {
      val charset = CharsetUtils.detectEncoding(is)
      val document = Jsoup.parse(is, resource.getCodec.name, resource.uri.toString)

      val contents = document.text().lines.zipWithIndex
        .map {
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
      is.close()
    }

  }
}
