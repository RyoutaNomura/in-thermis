package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.collection.JavaConversions._
import scala.util.control.Exception._

import org.apache.commons.lang3.StringUtils
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextParagraph
import org.apache.poi.xslf.usermodel.XMLSlideShow

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.Content
import jp.co.rn.inthermis.models.ContentIndexerResult
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import play.Logger

object PptIndexer extends FileIndexer {

  private val logger = Logger.of(this.getClass)

  override def getResourceTypeName: String = "Microsoft PowerPoint"

  override def getKeyTitles: Tuple3[String, String, String] = ("Slide", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-powerpoint-o"

  override def isTarget(uri: URI): Boolean = uri.toString() match {
    case s if s.endsWith(".ppt") => true
    case s if s.endsWith(".pptx") => true
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
      val content = resource.uri.toString match {
        case s if s.endsWith(".ppt") => {
          val slideshow = new HSLFSlideShow(is)
          val b = for (s <- slideshow.getSlides; p <- s.getTextParagraphs) yield HSLFTextParagraph.getText(p)
          b.mkString(System.lineSeparator)
        }
        case s if s.endsWith(".pptx") => {
          val slideshow = new XMLSlideShow(is)
          val b = for (s <- slideshow.getSlides; p <- s.getCommonSlideData.getText) yield p.getText
          b.mkString(System.lineSeparator)
        }
        case _ => throw new RuntimeException(s"not support type: ${resource.uri}")
      }
      Option(ContentIndexerResult(resource, Map(Seq.empty -> content), this.getClassName))
    }
  }

  override def generateIndex(resource: IndexerResource): LineIndexerResult = {

    val is = resource.getInputStream

    try {
      val slides = resource.uri.toString match {
        case s if s.endsWith(".ppt") => {
          val slideshow = new HSLFSlideShow(is)
          slideshow.getSlides.map { slide =>
            slide.getTextParagraphs.foldLeft(StringBuilder.newBuilder)((sb, plist) => sb.append(HSLFTextParagraph.getText(plist))).toString
          }.toList
        }
        case s if s.endsWith(".pptx") => {
          val slideshow = new XMLSlideShow(is)
          slideshow.getSlides.map { slide =>
            slide.getCommonSlideData.getText.foldLeft(StringBuilder.newBuilder)((sb, p) => sb.append(p.getText)).toString
          }.toList
        }
        case _ => throw new RuntimeException(s"not support type: ${resource.uri}")
      }

      val contents = slides.zipWithIndex.flatMap {
        case (slide, slideNo) =>
          slide.lines.zipWithIndex.map {
            case (line, lineNo) =>
              val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
              Content(
                (slideNo + 1).toString,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                line,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                indices)
          }.toList
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
