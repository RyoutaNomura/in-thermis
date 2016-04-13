package logic.indexer.impl

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Date
import org.apache.commons.lang3.StringUtils
import org.odftoolkit.odfdom.dom.element.table.TableTableCellElementBase
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement
import org.odftoolkit.simple.SpreadsheetDocument
import org.odftoolkit.simple.table.Cell
import org.odftoolkit.simple.table.Row
import org.odftoolkit.simple.table.Table
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.Content
import models.IndexerResult
import org.apache.commons.io.FilenameUtils
import utils.ReflectionUtils
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

object OdsIndexer extends FileIndexer {
  val className = ReflectionUtils.toType(OdsIndexer.getClass).typeSymbol.fullName

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString match {
    case v if v.endsWith(".ods") => true
    case _                       => false
  }

  override def generateIndex(uri: URI): IndexerResult = {
    val document = SpreadsheetDocument.loadDocument(new File(uri))
    val contents = (0 until document.getSheetCount)
      .map(document.getSheetByIndex)
      .flatMap { sheet => generateIndex(uri, sheet) }
      .toSeq

    IndexerResult(
      uri,
      FilenameUtils.getBaseName(Paths.get(uri).toString()),
      Files.size(Paths.get(uri)),
      new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis()),
      new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis()),
      contents,
      className,
      new Date)
  }

  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet: ", "Row: ", StringUtils.EMPTY)
  override def getResourceTypeName: String = "OpenDocument SpreadSheet"

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