package daos

import java.util.UUID

import scala.collection.JavaConversions._
import scala.reflect.runtime.universe

import com.datastax.driver.core.Session

import dtos.ResourceLocationDTO
import utils.CassandraHelper

object ResourceLocationDAO {

  def select(session: Session, ids: Set[UUID]): Map[UUID, ResourceLocationDTO] = {
    if (ids.isEmpty) {
      Map.empty
    } else {
      val idList: java.util.List[UUID] = ids.toSeq
      CassandraHelper.getRows(session, classOf[ResourceLocationDTO], s"SELECT * FROM resource_location WHERE id IN ?", idList)
        .map(r => (r.id, r))
        .toMap
    }
  }

  def delete(session: Session, id: UUID) {
    CassandraHelper.execCql(session, s"DELETE FROM resource_location WHERE id = ?", id)
  }

  def find(session: Session, uri: String): Option[ResourceLocationDTO] = {
    CassandraHelper.getRows(session, classOf[ResourceLocationDTO], "SELECT * FROM resource_location WHERE uri = ? ", uri).headOption
  }

  def insert(session: Session, dto: ResourceLocationDTO) {
    val cql = "INSERT INTO resource_location(" +
      "id, uri, display_location, name, size, created, modified, indexer_class_name, index_generated" +
      ") VALUES(" +
      "?, ?, ?, ?, ?, ?, ?, ?, ?" +
      ")"

    CassandraHelper.execCql(session,
      cql,
      dto.id,
      dto.uri,
      dto.displayLocation,
      dto.name,
      Long.box(dto.size),
      dto.created,
      dto.modified,
      dto.indexerClassName,
      dto.indexGenerated)
  }
}
