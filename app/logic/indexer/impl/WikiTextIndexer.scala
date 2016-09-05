package logic.indexer.impl

import java.net.URI
import java.util.Date
import scala.io.Source
import org.apache.commons.lang3.StringUtils
import logic.IndexerResource
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }
import utils.CharsetUtils
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp
import org.sweble.wikitext.engine.WtEngineImpl
import org.sweble.wikitext.engine.PageTitle
import org.sweble.wikitext.engine.PageId
import org.sweble.wikitext.example.TextConverter

object WikiTextIndexer extends FileIndexer {

  override def getResourceTypeName: String = "WikiText"

  override def getKeyTitles: Tuple3[String, String, String] = ("Line", StringUtils.EMPTY, StringUtils.EMPTY)

  override def getPriority: Int = -1

  override def isTarget(uri: URI): Boolean = false

  override def generateIndex(resource: IndexerResource): IndexerResult = {
    var is = resource.getInputStream
    try {
      val orgsource = Source.fromInputStream(is)(CharsetUtils.getCodec(is)).mkString
      val converted = convert(resource.name, orgsource)

      val contents = converted.lines.zipWithIndex
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

  def convert(title: String, wikitext: String): String = {
    val config = DefaultConfigEnWp.generate
    val engine = new WtEngineImpl(config)
    val pageTitle = PageTitle.make(config, title)
    val pageId = new PageId(pageTitle, -1)

    val cp = engine.postprocess(pageId, wikitext, null)

    val wrapCol = 80
    val p = new TextConverter(config, wrapCol);
    p.go(cp.getPage()).asInstanceOf[String]
  }
}
