package daos

import scala.collection.JavaConversions._
import dtos.ResourceContentDTO
import utils.CassandraHelper
import java.util.UUID
import utils.CassandraHelper

object ResourceContentDAO {
  def apply(helper: CassandraHelper) = new ResourceContentDAO(helper) 
}
class ResourceContentDAO(val helper: CassandraHelper) {

  //  private var helper = new CassandraHelper(DBSettings.host, DBSettings.port, DBSettings.keyspace)

  def select(ids: Set[UUID]): Map[UUID, ResourceContentDTO] = {
    if (ids.isEmpty) {
      Map.empty
    } else {
      val idList: java.util.List[UUID] = ids.toSeq
      helper.getRows(classOf[ResourceContentDTO], s"SELECT * FROM resource_content WHERE id IN ?", idList)
        .map(r => (r.id, r))
        .toMap
    }
  }

  def insert(dto: ResourceContentDTO) {
    helper.execCql("INSERT INTO resource_content(id, key1, key2, key3, content, resource_location_id) VALUES(?, ?, ?, ?, ?, ?)",
      dto.id,
      dto.key1,
      dto.key2,
      dto.key3,
      dto.content,
      dto.resourceLocationId)
  }

  def delete(id: UUID) {
    helper.execCql("DELETE FROM resource_content WHERE recource_location_id = ? ",
      id)
  }
}