package daos

import scala.collection.JavaConversions._
import utils.CassandraHelper
import settings.DBSettings
import dtos.WordIndicesDTO
import dtos.WordIndicesDTO
import utils.JsonCombinators
import play.api.libs.json.Json
import java.util.UUID
import com.datastax.driver.core.DataType
import com.datastax.driver.core.TupleValue

object WordIndicesDAO {
  def apply(helper: CassandraHelper) = new WordIndicesDAO(helper)
}

class WordIndicesDAO(val helper: CassandraHelper)  {
  //  private var helper = new CassandraHelper(DBSettings.host, DBSettings.port, DBSettings.keyspace)

  def select(word: String): Seq[WordIndicesDTO] = {
    if (word.isEmpty) {
      Seq.empty
    } else {
      helper.getRows(classOf[WordIndicesDTO], "SELECT * FROM word_indices WHERE word = ?", word)
    }
  }

  def delete(resourceLocationId: UUID) {
    helper.execCql("DELETE FROM word_indices WHERE resource_location_id = ?", resourceLocationId)
  }

  def insert(dto: WordIndicesDTO) {
    val indices: java.util.Map[UUID, java.util.Set[TupleValue]] = {
      dto.indices.map { f =>
        val contentId = f._1
        val tupleSet: java.util.Set[TupleValue] = f._2.map { t =>
          val tupleType = helper.metadata.newTupleType(DataType.cint, DataType.cint)
          tupleType.newValue(Int.box(t._1), Int.box(t._2))
        }
        (contentId, tupleSet)
      }
    }

    helper.execCql("INSERT INTO word_indices(word, resource_location_id, count, indices) VALUES(?,?,?,?)",
      dto.word,
      dto.resourceLocationId,
      Long.box(dto.count),
      indices)
  }
}