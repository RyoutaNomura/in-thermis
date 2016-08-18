package daos

import java.util.UUID

import com.datastax.driver.core.Session

import dtos.ResourceContentDTO
import utils.CassandraHelper

object ResourceContentDAO {

  def insert(session: Session, dto: ResourceContentDTO) {
    val cql = "INSERT INTO resource_content(resource_content_id, key1, key2, key3, content, prev_content, next_content, resource_location_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)"

    CassandraHelper.execCqlAsync(session,
      cql,
      dto.resourceContentId,
      dto.key1,
      dto.key2,
      dto.key3,
      dto.content,
      dto.prevContent,
      dto.nextContent,
      dto.resourceLocationId)
  }

  def deleteByResourceLocationId(session: Session, resourceLocationId: UUID) {
    CassandraHelper.execCqlAsync(session, "DELETE FROM resource_content WHERE recource_location_id = ? ",
      resourceLocationId)
  }
}
