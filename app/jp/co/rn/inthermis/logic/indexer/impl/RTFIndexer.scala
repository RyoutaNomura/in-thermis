package jp.co.rn.inthermis.logic.indexer.impl

import java.io.StringWriter
import java.net.URI

import org.apache.commons.lang3.StringUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.rtf.RTFParser

import javax.xml.transform.{ OutputKeys, TransformerFactory }
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult
import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.{ Content, IndexerResource, IndexerResult }

object RTFIndexer extends FileIndexer {
  override def getResourceTypeName: String = "Rich Text Format"

  override def getKeyTitles: Tuple3[String, String, String] = ("Line", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-text-o"

  override def isTarget(uri: URI): Boolean = uri.toString() match {
    case s if s.endsWith(".rtf") => true
    case _                       => false
  }

  override def generateIndex(resource: IndexerResource): IndexerResult = {
    var is = resource.getInputStream

    val metadata = new Metadata
    val sw = new StringWriter
    val factory = TransformerFactory.newInstance.asInstanceOf[SAXTransformerFactory]
    val handler = factory.newTransformerHandler
    handler.getTransformer.setOutputProperty(OutputKeys.METHOD, "text")
    handler.getTransformer.setOutputProperty(OutputKeys.INDENT, "no")
    handler.setResult(new StreamResult(sw));

    val parser = new RTFParser
    parser.parse(is, handler, metadata, new ParseContext);
    val content = sw.toString();

    try {
      val contents = content.lines.zipWithIndex
        .map {
          case (line, lineNo) =>
            val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
            Content(lineNo + 1.toString, StringUtils.EMPTY, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices)
        }.toList
      fillSibilingContent(contents)

      IndexerResult(
        resource,
        contents,
        this.getClassName)

    } finally {
      is.close()
    }
  }
}
