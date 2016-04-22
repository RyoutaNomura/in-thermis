package logic.indexer.impl

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.hslf.usermodel.{ HSLFSlideShow, HSLFTextParagraph }
import org.apache.poi.xslf.usermodel.XMLSlideShow

import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }
import utils.FileTimeUtils

object PptIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Microsoft PowerPoint Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Slide: ", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString() match {
    case s if s.endsWith(".ppt")  => true
    case s if s.endsWith(".pptx") => true
    case _                        => false
  }

  override def generateIndex(uri: URI): IndexerResult = {

    val stream = uri.toURL.openStream

    try {
      val slides = uri.toString() match {
        case s if s.endsWith(".ppt") => {
          val slideshow = new HSLFSlideShow(stream)
          slideshow.getSlides.map { slide =>
            slide.getTextParagraphs.foldLeft(StringBuilder.newBuilder)((sb, plist) => sb.append(HSLFTextParagraph.getText(plist))).toString
          }.toList
        }
        case s if s.endsWith(".pptx") => {
          val slideshow = new XMLSlideShow(stream)
          slideshow.getSlides.map { slide =>
            slide.getCommonSlideData.getText.foldLeft(StringBuilder.newBuilder)((sb, p) => sb.append(p.getText)).toString
          }.toList
        }
        case _ => throw new RuntimeException(s"not support type: $uri")
      }

      val contents = slides.zipWithIndex.flatMap {
        case (slide, slideNo) =>
          slide.lines.zipWithIndex.map {
            case (line, lineNo) =>
              val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
              Content(
                slideNo + 1.toString,
                lineNo.toString,
                StringUtils.EMPTY,
                line,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                indices)
          }.toList
      }.toList
      fillSibilingContent(contents)

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
      stream.close()
    }
  }
}