package daos

import java.util.UUID
import scala.collection.JavaConversions._
import scala.reflect.runtime.universe
import com.datastax.driver.core.{ Session, TupleValue, DataType }
import dtos.WordIndicesDTO
import utils.CassandraHelper
import com.datastax.driver.core.querybuilder.QueryBuilder
import settings.DBSettings
import com.datastax.driver.core.PreparedStatement

object WordIndicesDAO {

  def select(session: Session, word: String): Seq[WordIndicesDTO] = {
    if (word.isEmpty) {
      Seq.empty
    } else {
      CassandraHelper.getRows(session, classOf[WordIndicesDTO], "SELECT * FROM word_indices WHERE word = ?", word)
    }
  }

  def delete(session: Session, resourceLocationId: UUID) {
    CassandraHelper.execCqlAsync(session, "DELETE FROM word_indices WHERE resource_location_id = ?", resourceLocationId)
  }

  def insert(session: Session, dto: WordIndicesDTO) {
    val indices: java.util.Map[UUID, java.util.Set[TupleValue]] = {
      dto.indices.map { f =>
        val contentId = f._1
        val tupleSet: java.util.Set[TupleValue] = f._2.map { t =>
          val tupleType = CassandraHelper.metadata.newTupleType(DataType.cint, DataType.cint)
          tupleType.newValue(Int.box(t._1), Int.box(t._2))
        }
        (contentId, tupleSet)
      }
    }

    val cql = "INSERT INTO word_indices(" +
      " word" +
      ",resource_location_id" +
      ",count" +
      ",resource_updated" +
      ",resource_uri" +
      ",resource_name" +
      ",indices" +
      ") VALUES(?,?,?,?,?,?,?)"

    CassandraHelper.execCqlAsync(session, cql,
      dto.word,
      dto.resourceLocationId,
      Long.box(dto.count),
      dto.resourceUpdated,
      dto.resourceUri,
      dto.resourceName,
      indices)
  }
}
