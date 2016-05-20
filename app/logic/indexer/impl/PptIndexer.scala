package logic.indexer.impl

import java.net.URI
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.apache.poi.hslf.usermodel.{ HSLFSlideShow, HSLFTextParagraph }
import org.apache.poi.xslf.usermodel.XMLSlideShow

import logic.IndexerResource
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }

object PptIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Microsoft PowerPoint Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Slide", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString() match {
    case s if s.endsWith(".ppt")  => true
    case s if s.endsWith(".pptx") => true
    case _                        => false
  }

  override def generateIndex(resource: IndexerResource): IndexerResult = {

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

      IndexerResult(
        resource,
        contents,
        this.getClassName,
        new Date)

    } finally {
      is.close()
    }
  }
}
