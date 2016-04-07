package daos

import scala.collection.JavaConversions._ 
import dtos.ResourceLocationDTO
import utils.CassandraHelper
import java.net.URI
import java.util.UUID

object ResourceLocationDAO {
  def apply(helper: CassandraHelper) = new ResourceLocationDAO(helper)
}
class ResourceLocationDAO(val helper: CassandraHelper)  {

  //  private var helper = new CassandraHelper(DBSettings.host, DBSettings.port, DBSettings.keyspace)

  def select(ids: Set[UUID]): Map[UUID, ResourceLocationDTO] = {
    if (ids.isEmpty) {
      Map.empty
    } else {
      val idList: java.util.List[UUID] = ids.toSeq 
      helper.getRows(classOf[ResourceLocationDTO], s"SELECT * FROM resource_location WHERE id IN ?", idList)
        .map(r => (r.id, r))
        .toMap
    }
  }

  def delete(id: UUID) {
    helper.execCql(s"DELETE FROM resource_location WHERE id = ?", id)
  }

  def find(uri: String): Option[ResourceLocationDTO] = {
    helper.getRows(classOf[ResourceLocationDTO], "SELECT * FROM resource_location WHERE uri = ? ", uri).headOption
  }

  def insert(dto: ResourceLocationDTO) {
    helper.execCql(s"INSERT INTO resource_location(id, uri, name, size, created, modified, indexer_class_name, index_generated) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
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