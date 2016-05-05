package daos

import java.util.UUID

import scala.collection.JavaConversions._
import scala.reflect.runtime.universe

import com.datastax.driver.core.Session

import dtos.ResourceContentDTO
import utils.CassandraHelper

object ResourceContentDAO {

  def select(session: Session, ids: Set[UUID]): Map[UUID, ResourceContentDTO] = {
    if (ids.isEmpty) {
      Map.empty
    } else {
      val idList: java.util.List[UUID] = ids.toSeq
      CassandraHelper.getRows(session, classOf[ResourceContentDTO], s"SELECT * FROM resource_content WHERE id IN ?", idList)
        .map(r => (r.id, r))
        .toMap
    }
  }

  def insert(session: Session, dto: ResourceContentDTO) {
    val cql = "INSERT INTO resource_content(id, key1, key2, key3, content, prev_content, next_content, resource_location_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)"

    CassandraHelper.execCqlAsync(session,
      cql,
      dto.id,
      dto.key1,
      dto.key2,
      dto.key3,
      dto.content,
      dto.prevContent,
      dto.nextContent,
      dto.resourceLocationId)
  }

  def delete(session: Session, id: UUID) {
    CassandraHelper.execCqlAsync(session, "DELETE FROM resource_content WHERE recource_location_id = ? ",
      id)
  }
}
