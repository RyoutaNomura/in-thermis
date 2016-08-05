package daos

import java.util.UUID
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.reflect.runtime.universe
import org.apache.commons.lang3.StringUtils
import com.datastax.driver.core.{ DataType, Session, TupleValue }
import dtos.WordIndicesDTO
import enums.DateRangeCriteria
import utils.CassandraHelper
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import dtos.WordIndicesDTO
import dtos.WordIndicesByKeyDTO

class WordIndicesByKeyDAO {

  def select(session: Session, word: String, indexType: String, searchKey: String): Seq[WordIndicesByKeyDTO] = {
    val cql = "SELECT * FROM word_indices_by_key WHERE index_type = ? AND search_key = ?"
    CassandraHelper.getRows(session, classOf[WordIndicesByKeyDTO], cql, word, indexType, searchKey)
  }

  def insert(session: Session, dto: WordIndicesByKeyDTO) {
    val cql = "INSERT INTO word_indices_by_key(" +
      " word" +
      ",index_type" +
      ",search_key" +
      ",resuly" +
      ") VALUES(" +
      "?,?,?,?" +
      ")"

    CassandraHelper.execCqlAsync(session, cql,
      dto.word,
      dto.indexType,
      dto.searchKey,
      dto.result)
  }
}
