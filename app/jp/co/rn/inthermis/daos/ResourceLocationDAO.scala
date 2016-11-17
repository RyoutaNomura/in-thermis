package jp.co.rn.inthermis.daos

import java.util.UUID

import scala.reflect.runtime.universe

import com.datastax.driver.core.Session

import jp.co.rn.inthermis.dtos.ResourceLocationDTO
import jp.co.rn.inthermis.utils.CassandraHelper

object ResourceLocationDAO {

  def selectById(session: Session, id: UUID): Option[ResourceLocationDTO] = {
    CassandraHelper.getRows(session, classOf[ResourceLocationDTO], s"SELECT * FROM resource_location WHERE resource_location_id = ?", id).headOption
  }

  def selectByUri(session: Session, uri: String): Option[ResourceLocationDTO] = {
    CassandraHelper.getRows(session, classOf[ResourceLocationDTO], "SELECT * FROM resource_location WHERE resource_uri = ? ", uri).headOption
  }

  def selectByWalkerName(session: Session, walkerName: String): Seq[ResourceLocationDTO] = {
    CassandraHelper.getRows(session, classOf[ResourceLocationDTO], "SELECT * FROM resource_location WHERE resource_walker_name = ? ", walkerName)
  }

  def selectAll(session: Session): Seq[ResourceLocationDTO] = {
    CassandraHelper.getRows(session, classOf[ResourceLocationDTO], "SELECT * FROM resource_location")
  }

  def delete(session: Session, id: UUID) {
    CassandraHelper.execCqlAsync(session, s"DELETE FROM resource_location WHERE resource_location_id = ?", id)
  }

  def insert(session: Session, dto: ResourceLocationDTO) {
    val cql = "INSERT INTO resource_location(" +
      "resource_location_id, " +
      "resource_uri," +
      "resource_display_location," +
      "resource_name," +
      "resource_size," +
      "resource_walker_name," +
      "resource_indexer_name," +
      "resource_last_modified," +
      "index_generated" +
      ") VALUES(" +
      "?, ?, ?, ?, ?, ?, ?, ?,?" +
      ")"

    CassandraHelper.execCqlAsync(session,
      cql,
      dto.resourceLocationId,
      dto.resourceUri,
      dto.resourceDisplayLocation,
      dto.resourceName,
      Long.box(dto.resourceSize),
      dto.resourceWalkerName,
      dto.resourceIndexerName,
      dto.resourceLastModified,
      dto.indexGenerated)
  }
}
