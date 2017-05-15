package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.io.Source
import scala.util.control.Exception._

import org.apache.commons.lang3.StringUtils

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.Content
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import jp.co.rn.inthermis.models.ContentIndexerResult
import play.Logger

object TextIndexer extends FileIndexer {
  
  private val logger = Logger.of(this.getClass)

  override def getResourceTypeName: String = "Plain Text"

  override def getKeyTitles: Tuple3[String, String, String] = ("Line", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-text-o"

  override def isTarget(uri: URI): Boolean = uri.toString() match {
    case s if s.endsWith(".txt") => true
    case s if s.endsWith(".md")  => true
    case _                       => false
  }

  override def generateContentIndex(resource: IndexerResource): Option[ContentIndexerResult] = {
    val is = resource.getInputStream
    val source = Source.fromInputStream(is)(resource.getCodec)
    
    allCatch withApply { e=>
            logger.error(s"error occurred during indexing ${resource.uri}", e)
            Option.empty
            
    } andFinally {
      source.close
      is.close
      
    } apply {
      val content = source.getLines().mkString(System.lineSeparator)
      Option(ContentIndexerResult(resource, Map(Seq.empty -> content), this.getClassName))
      
    }
  }
  
  override def generateIndex(resource: IndexerResource): LineIndexerResult = {
    var is = resource.getInputStream
    val source = Source.fromInputStream(is)(resource.getCodec)

    try {
      val contents = source.getLines.zipWithIndex
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
      source.close()
      is.close()
    }
  }
}
