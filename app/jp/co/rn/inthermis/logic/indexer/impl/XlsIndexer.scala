package jp.co.rn.inthermis.logic.indexer.impl

import java.net.URI

import scala.collection.JavaConversions._
import scala.util.control.Exception._

import org.apache.commons.lang3.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.google.common.base.Joiner

import jp.co.rn.inthermis.logic.analyzer.StringAnalyzer
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.models.Content
import jp.co.rn.inthermis.models.ContentIndexerResult
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.models.LineIndexerResult
import play.Logger

object XlsIndexer extends FileIndexer {

  private val logger = Logger.of(this.getClass)

  override def getResourceTypeName: String = "Microsoft Excel"

  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet", "Row", StringUtils.EMPTY)

  override def getPriority: Int = 0

  override def isTarget(uri: URI): Boolean = uri.toString.endsWith(".xls") || uri.toString.endsWith(".xlsx")

  override def getIconCssClassName: String = "fa-file-excel-o"

  override def generateContentIndex(resource: IndexerResource): Option[ContentIndexerResult] = {

    val is = resource.getInputStream

    allCatch withApply { e =>
      logger.error(s"error occurred during indexing ${resource.uri}", e)
      Option.empty

    } andFinally {
      is.close

    } apply {
      val workbook = resource.uri.toString() match {
        case s if s.endsWith(".xls") => new HSSFWorkbook(is)
        case s if s.endsWith(".xlsx") => new XSSFWorkbook(is)
        case _ => throw new IllegalArgumentException(s"${resource.uri} is not supported.")
      }

      val content = (for (i <- (0 until workbook.getNumberOfSheets); row <- workbook.getSheetAt(i)) yield {
        Option(row) match {
          case Some(s) if s.getPhysicalNumberOfCells > 0 => StringUtils.EMPTY
          case Some(s) => (for (c <- s.cellIterator()) yield {
            c.getCellType match {
              case Cell.CELL_TYPE_STRING => c.getStringCellValue
              case Cell.CELL_TYPE_NUMERIC => c.getNumericCellValue.toString
              case Cell.CELL_TYPE_BOOLEAN => c.getBooleanCellValue.toString
              case _ => StringUtils.EMPTY
            }
          }).mkString
          case None => StringUtils.EMPTY
        }
      }).mkString(System.lineSeparator)

      Option(ContentIndexerResult(resource, Map(Seq.empty -> content), this.getClassName))
    }
  }

  override def generateIndex(resource: IndexerResource): LineIndexerResult = {
    val is = resource.getInputStream
    val workbook = resource.uri.toString match {
      case s if s.endsWith(".xls") => new HSSFWorkbook(is)
      case s if s.endsWith(".xlsx") => new XSSFWorkbook(is)
      case _ => throw new IllegalArgumentException(s"${resource.uri} is not supported.")
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
                case Cell.CELL_TYPE_STRING => cell.getStringCellValue
                case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue.toString
                case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue.toString
                case _ => StringUtils.EMPTY
              }))
              val indices = StringAnalyzer.analyze(rowStr).map { x => (x.word, x.start, x.length) }
              // TODO rownumがずれているかもしれない
              Content(sheet.getSheetName, row.getRowNum.toString(), StringUtils.EMPTY, rowStr, StringUtils.EMPTY, StringUtils.EMPTY, indices)
            }
        }.toList
      fillSibilingContent(contents)

      LineIndexerResult(
        resource,
        contents,
        this.getClassName)
    } finally {
      workbook.close()
      is.close()
    }
  }
}
