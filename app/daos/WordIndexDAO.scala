package daos

import java.util.{ Date, UUID }
import scala.reflect.runtime.universe
import com.datastax.driver.core.Session
import dtos.WordIndexDTO
import utils.CassandraHelper
import scala.collection.JavaConverters._

object WordIndexDAO {

  def select(session: Session, word: String): Seq[WordIndexDTO] = {
    CassandraHelper.getRows(session, classOf[WordIndexDTO], "SELECT * FROM word_index WHERE word = ? ", word)
  }

  def selectByResourceIndexerName(session: Session, word: String, resourceIndexerName: String): Seq[WordIndexDTO] = {
    CassandraHelper.getRows(session, classOf[WordIndexDTO], "SELECT * FROM word_index WHERE word = ? AND resource_indexer_name = ? ", word, resourceIndexerName)
  }

  def selectByResourceWalkerName(session: Session, word: String, resourceWalkerName: String): Seq[WordIndexDTO] = {
    CassandraHelper.getRows(session, classOf[WordIndexDTO], "SELECT * FROM word_index WHERE word = ? AND resource_walker_name = ? ", word, resourceWalkerName)
  }

  def selectByResourceLastModified(session: Session, word: String, resourceLastModified: Date): Seq[WordIndexDTO] = {
    CassandraHelper.getRows(session, classOf[WordIndexDTO], "SELECT * FROM word_index WHERE word = ? AND resource_last_modified >= ? ", word, resourceLastModified)
  }

  def deleteByResourceLocationId(session: Session, resourceLocationId: UUID) {
    CassandraHelper.execCqlAsync(session, "DELETE FROM word_index WHERE resource_location_id = ?", resourceLocationId)
  }

  def insert(session: Session, dto: WordIndexDTO) {

    val cql = "INSERT INTO word_index(" +
      " word" +
      ",resource_content_id" +
      ",indices" +
      ",indices_count" +
      ",content" +
      ",prev_content" +
      ",next_content" +
      ",content_key1" +
      ",content_key2" +
      ",content_key3" +
      ",resource_uri" +
      ",resource_display_location" +
      ",resource_name" +
      ",resource_size" +
      ",resource_walker_name" +
      ",resource_indexer_name" +
      ",resource_last_modified" +
      ",resource_location_id" +
      ") VALUES(" +
      "?,?,?,?,?,?,?,?,?,?," +
      "?,?,?,?,?,?,?,?" +
      ")"

    CassandraHelper.execCqlAsync(session, cql,
      dto.word,
      dto.resourceContentId,
      dto.indices.asJava,
      Int.box(dto.indicesCount),
      dto.content,
      dto.prevContent,
      dto.nextContent,
      dto.contentKey1,
      dto.contentKey2,
      dto.contentKey3,
      dto.resourceUri,
      dto.resourceDisplayLocation,
      dto.resourceName,
      Long.box(dto.resourceSize),
      dto.resourceWalkerName,
      dto.resourceIndexerName,
      dto.resourceLastModified,
      dto.resourceLocationId)
  }
}
