package logic.indexer.impl

import java.net.URI
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.google.common.base.Joiner

import logic.IndexerResource
import logic.analyzer.StringAnalyzer
import logic.indexer.FileIndexer
import models.{ Content, IndexerResult }

object XlsIndexer extends FileIndexer {

  override def getResourceTypeName: String = "Microsoft Excel Document"

  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet", "Row", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString.endsWith(".xls") || uri.toString.endsWith(".xlsx")

  override def generateIndex(resource: IndexerResource): IndexerResult = {
    val is = resource.getInputStream
    val workbook = resource.uri.toString match {
      case s if s.endsWith(".xls")  => new HSSFWorkbook(is)
      case s if s.endsWith(".xlsx") => new XSSFWorkbook(is)
      case _                        => throw new IllegalArgumentException(s"${resource.uri} is not supported.")
    }

    try {
      val contents = (0 until workbook.getNumberOfSheets)
        .map(workbook.getSheetAt)
        .flatMap { sheet =>
          (0 to sheet.getPhysicalNumberOfRows)
            .map(sheet.getRow)
            .filter(Option(_).isDefined)
            .filter(_.getPhysicalNumberOfCells > 0)
            .map { row =>
              val rowStr = Joiner.on("\t").join(row.cellIterator.map(cell => cell.getCellType match {
                case Cell.CELL_TYPE_STRING  => cell.getStringCellValue
                case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue.toString
                case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue.toString
                case _                      => StringUtils.EMPTY
              }))
              val indices = StringAnalyzer.analyze(rowStr).map { x => (x.word, x.start, x.length) }
              // TODO rownumがずれているかもしれない
              Content(sheet.getSheetName, row.getRowNum.toString(), StringUtils.EMPTY, rowStr, StringUtils.EMPTY, StringUtils.EMPTY, indices)
            }
        }.toList
      fillSibilingContent(contents)

      IndexerResult(
        resource,
        contents,
        this.getClassName,
        new Date)

    } finally {
      workbook.close()
      is.close()
    }
  }
}
