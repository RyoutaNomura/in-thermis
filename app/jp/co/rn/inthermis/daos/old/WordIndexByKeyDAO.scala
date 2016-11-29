package jp.co.rn.inthermis.daos.old

import scala.reflect.runtime.universe

import com.datastax.driver.core.Session

import jp.co.rn.inthermis.dtos.old.WordIndexByKeyDTO
import jp.co.rn.inthermis.utils.CassandraHelper

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
