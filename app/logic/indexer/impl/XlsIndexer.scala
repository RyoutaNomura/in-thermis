package logic.indexer.impl

import scala.collection.JavaConversions._
import logic.indexer.FileIndexer
import org.apache.commons.lang3.StringUtils
import java.net.URI
import java.nio.file.Paths
import models.IndexerResult
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import com.google.common.base.Joiner
import logic.analyzer.StringAnalyzer
import models.Content
import org.apache.commons.io.FilenameUtils
import java.nio.file.Files
import java.util.Date
import utils.ReflectionUtils
import org.apache.poi.ss.usermodel.Cell

object XlsIndexer extends FileIndexer {
  val className = ReflectionUtils.toType(XlsIndexer.getClass).typeSymbol.fullName

  override def getResourceTypeName: String = "Microsoft Excel Document"
  override def getKeyTitles: Tuple3[String, String, String] = ("Sheet", "Row", StringUtils.EMPTY)
  override def getPriority: Int = 0
  override def isTarget(uri: URI): Boolean = uri.toString.endsWith(".xls")
  override def generateIndex(uri: URI): IndexerResult = {
    val stream = uri.toURL.openStream
    val workbook = new HSSFWorkbook(stream)
    try {
      val contents = (0 until workbook.getNumberOfSheets)
        .map(workbook.getSheetAt)
        .flatMap { sheet =>
          (0 to sheet.getPhysicalNumberOfRows)
            .map(sheet.getRow)
            .filter(_ != null)
            .filter(_.getPhysicalNumberOfCells > 0)
            .map { row =>
              val rowStr = Joiner.on("\t").join(row.cellIterator.map(cell => cell.getCellType match {
                case Cell.CELL_TYPE_STRING => cell.getStringCellValue
                case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue.toString
                case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue.toString
                case _ => StringUtils.EMPTY
              }))
              val indices = StringAnalyzer.analyze(rowStr).map { x => (x.word, x.start, x.length) }
              Content(sheet.getSheetName, row.getRowNum.toString(), StringUtils.EMPTY, rowStr, StringUtils.EMPTY, StringUtils.EMPTY, indices)
            }
        }.toList
      fillSibilingContent(contents)

      IndexerResult(
        uri,
        FilenameUtils.getBaseName(Paths.get(uri).toString()),
        Files.size(Paths.get(uri)),
        new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis()),
        new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis()),
        contents,
        className,
        new Date)
    } finally {
      workbook.close
      stream.close()
    }
  }
}