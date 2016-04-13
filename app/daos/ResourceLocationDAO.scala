package daos

import scala.collection.JavaConversions._
import dtos.ResourceLocationDTO
import utils.CassandraHelper
import java.net.URI
import java.util.UUID
import com.datastax.driver.core.Session

object ResourceLocationDAO {

  //  private var helper = new CassandraHelper(DBSettings.host, DBSettings.port, DBSettings.keyspace)

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
    CassandraHelper.execCql(session, s"INSERT INTO resource_location(id, uri, name, size, created, modified, indexer_class_name, index_generated) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
      dto.id,
      dto.uri,
      dto.name,
      Long.box(dto.size),
      dto.created,
      dto.modified,
      dto.indexerClassName,
      dto.indexGenerated)
  }
}