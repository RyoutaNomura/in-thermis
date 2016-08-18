package daos.old

import com.datastax.driver.core.Session
import utils.CassandraHelper
import dtos.old.WordIndexByKeyDTO
import scala.reflect.runtime.universe

class WordindexByKeyDAO {

  def select(session: Session, searchKey: String, keyId: String): Seq[WordIndexByKeyDTO] = {
    val cql = "SELECT * FROM word_index_by_key WHERE search_key = ? AND key_id = ?"
    CassandraHelper.getRows(session, classOf[WordIndexByKeyDTO], cql, searchKey, keyId)
  }

  def insert(session: Session, dto: WordIndexByKeyDTO) {
    val cql = "INSERT INTO word_index_by_key(" +
      " search_key" +
      ",key_id" +
      ",resuly" +
      ") VALUES(" +
      "?,?,?,?" +
      ")"

    CassandraHelper.execCqlAsync(session, cql,
      dto.searchKey,
      dto.keyId,
      dto.result)
  }
}
