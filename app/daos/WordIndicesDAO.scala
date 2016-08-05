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

class WordIndicesDAO {

  def select(session: Session, word: String, resourceLocationId: UUID): Seq[WordIndicesDTO] = {
    val cql = "SELECT * FROM word_indices WHERE resource_location_id = ? AND word = ?"
    CassandraHelper.getRows(session, classOf[WordIndicesDTO], cql, word, resourceLocationId)
  }

  def delete(session: Session, resourceLocationId: UUID) {
    CassandraHelper.execCqlAsync(session, "DELETE FROM word_indices WHERE resource_location_id = ?", resourceLocationId)
  }

  def insert(session: Session, dto: WordIndicesDTO) {

    val cql = "INSERT INTO word_indices(" +
      " word" +
      ",resource_location_id" +
      ",content" +
      ",content_id" +
      ",content_key1" +
      ",content_key2" +
      ",content_key3" +
      ",indices" +
      ",indices_in_resource" +
      ",next_content" +
      ",prev_content" +
      ",resource_display_location" +
      ",resource_indexer_name" +
      ",resource_last_modified" +
      ",resource_name" +
      ",resource_size" +
      ",resource_uri" +
      ",resource_walker_name" +
      ") VALUES(" +
      "?,?,?,?,?,?,?,?,?,?," +
      "?,?,?,?,?,?,?,?" +
      ")"

    CassandraHelper.execCqlAsync(session, cql,
      dto.word,
      dto.resourceLocationId,
      dto.content,
      dto.contentId,
      dto.contentKey1,
      dto.contentKey2,
      dto.contentKey3,
      dto.indices,
      Double.box(dto.indicesInResource),
      dto.nextContent,
      dto.prevContent,
      dto.resourceDisplayLocation,
      dto.resourceIndexerName,
      dto.resourceLastModified,
      dto.resourceName,
      dto.resourceSize,
      dto.resourceUri,
      dto.resourceWalkerName)
  }
}
