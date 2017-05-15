package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.collection.JavaConversions._
import scala.util.control.Exception._

import org.apache.commons.lang3.StringUtils
import org.odftoolkit.simple.TextDocument

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.Content
import jp.co.rn.inthermis.models.ContentIndexerResult
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import play.Logger

object OdtIndexer extends FileIndexer {

  private val logger = Logger.of(this.getClass)

  override def getResourceTypeName: String = "OpenDocument Text"

  override def getKeyTitles: Tuple3[String, String, String] = ("Paragraph", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-word-o"

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case v if v.endsWith(".odt") => true
    case _ => false
  }

  override def generateContentIndex(resource: IndexerResource): Option[ContentIndexerResult] = {

    val is = resource.getInputStream

    allCatch withApply {e =>
      logger.error(s"error occurred during indexing ${resource.uri}", e)
      Option.empty

    } andFinally {
      is.close
    } apply {
      
      val document = TextDocument.loadDocument(is)
      val contents = document.getParagraphIterator.map { _.getTextContent }.mkString(System.lineSeparator)
      Option(ContentIndexerResult(resource, Map(Seq.empty -> contents), this.getClassName))
    }
  }

  override def generateIndex(resource: IndexerResource): LineIndexerResult = {

    val is = resource.getInputStream

    try {
      val document = TextDocument.loadDocument(is)
      val contents = document.getParagraphIterator.map { _.getTextContent }.zipWithIndex.map {
        case (p, pno) =>
          val indices = StringAnalyzer.analyze(p).map { x => (x.word, x.start, x.length) }
          Content(pno + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, p, StringUtils.EMPTY, StringUtils.EMPTY, indices)
      }.toList

      LineIndexerResult(
        resource,
        contents,
        this.getClassName)

    } finally {
      is.close()
    }
  }

}
