package daos

import scala.collection.JavaConversions._
import dtos.ResourceContentDTO
import utils.CassandraHelper
import java.util.UUID
import utils.CassandraHelper
import com.datastax.driver.core.Session

object ResourceContentDAO {

  //  private var helper = new CassandraHelper(DBSettings.host, DBSettings.port, DBSettings.keyspace)

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

  def insert(session: Session,dto: ResourceContentDTO) {
    CassandraHelper.execCql(session, "INSERT INTO resource_content(id, key1, key2, key3, content, prev_content, next_content, resource_location_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
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
    CassandraHelper.execCql(session, "DELETE FROM resource_content WHERE recource_location_id = ? ",
      id)
  }
}