package jp.co.rn.inthermis.daos

import java.util.UUID

import com.datastax.driver.core.Session

import jp.co.rn.inthermis.dtos.ResourceContentDTO
import jp.co.rn.inthermis.utils.CassandraHelper

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

  def selectByResourceLocationId(session: Session, resourceLocationId: UUID): Seq[ResourceContentDTO] = {
    CassandraHelper.getRows(session, classOf[ResourceContentDTO], "SELECT * FROM resource_content WHERE resource_location_id = ?", resourceLocationId)
  }

  def delete(session: Session, resourceContentId: UUID) {
    CassandraHelper.execCqlAsync(session, "DELETE FROM resource_content WHERE resource_content_id = ? ",
      resourceContentId)
  }
}
