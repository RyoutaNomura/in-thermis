package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.collection.JavaConversions._
import scala.util.control.Exception._

import org.apache.commons.lang3.StringUtils
import org.odftoolkit.odfdom.dom.element.table.TableTableCellElementBase
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement
import org.odftoolkit.simple.SpreadsheetDocument
import org.odftoolkit.simple.table.Cell
import org.odftoolkit.simple.table.Row
import org.odftoolkit.simple.table.Table

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.Content
import jp.co.rn.inthermis.models.ContentIndexerResult
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import play.Logger

object OdsIndexer extends FileIndexer {

  private val logger = Logger.of(this.getClass)

  override def getResourceTypeName: String = "OpenDocument SpreadSheet"

  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet", "Row", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def getIconCssClassName: String = "fa-file-excel-o"

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case v if v.endsWith(".ods") => true
    case _ => false
  }

  override def generateContentIndex(resource: IndexerResource): Option[ContentIndexerResult] = {
    
    val is = resource.getInputStream

    allCatch withApply {e =>
      logger.error(s"error occurred during indexing ${resource.uri}", e)
      Option.empty
      
    } andFinally {
      is.close()
      
    } apply {
      val document = SpreadsheetDocument.loadDocument(is)
      val contents = for (sheetNo <- (0 until document.getSheetCount)) yield {
        val sheet = document.getSheetByIndex(sheetNo)
        val content = for (row <- this.getRows(sheet)) yield {
          this.getCells(row).map { _.getStringValue }.mkString(StringUtils.EMPTY) match {
            case s if StringUtils.isNotEmpty(s) => s
            case _ => StringUtils.EMPTY
          }
        }
        (Seq(sheet.getTableName), content.mkString(System.lineSeparator))
      }
      Option(ContentIndexerResult(resource, contents.toMap, this.getClassName))
    }
  }

  override def generateIndex(resource: IndexerResource): LineIndexerResult = {
    var is = resource.getInputStream

    try {
      val document = SpreadsheetDocument.loadDocument(is)
      val contents = (0 until document.getSheetCount)
        .map(document.getSheetByIndex)
        .flatMap { sheet => generateIndex(resource.uri, sheet) }
        .toSeq

      LineIndexerResult(
        resource,
        contents,
        this.getClassName)

    } finally {
      is.close()
    }
  }

  private def generateIndex(uri: URI, sheet: Table): Seq[Content] = {
    val indices = getRows(sheet).map { row => generateIndex(uri, sheet, row) }.collect { case Some(s) => s }.toList
    fillSibilingContent(indices)
    indices
  }
  private def generateIndex(uri: URI, sheet: Table, row: Row): Option[Content] = {
    val line = getCells(row)
      .map(_.getStringValue)
      .collect { case s if StringUtils.isNotEmpty(s) => s }
      .mkString("\t")
    if (line.isEmpty) {
      None
    } else {
      val indices = StringAnalyzer.analyze(line).map { x => (x.word, x.start, x.length) }
      // TODO rownumがずれているかもしれない
      Some(Content(sheet.getTableName, row.getRowIndex.toString, StringUtils.EMPTY, line, StringUtils.EMPTY, StringUtils.EMPTY, indices))
    }
  }

  private def getRows(sheet: Table): Seq[Row] = {
    val rows = sheet.getOdfElement.getElementsByTagName("table:table-row")
    (0 until rows.getLength)
      .map { i => rows.item(i) match { case t: TableTableRowElement => t } }
      .map { Row.getInstance }
      .toSeq
  }

  private def getCells(row: Row): Seq[Cell] = {
    val cells = row.getOdfElement.getElementsByTagName("table:table-cell")
    (0 until cells.getLength)
      .map { i => cells.item(i) match { case t: TableTableCellElementBase => t } }
      .map { Cell.getInstance }
      .toSeq
  }

}
