package jp.co.rn.inthermis.dtos

import java.util.{ Date, UUID }

import org.apache.commons.lang3.StringUtils

case class ResourceLocationDTO(
    var resourceLocationId: UUID,
    var resourceUri: String,
    var resourceDisplayLocation: String,
    var resourceName: String,
    var resourceSize: Long,
    var resourceWalkerName: String,
    var resourceIndexerName: String,
    var resourceLastModified: Date,
    var indexGenerated: Date) {
}

object ResourceLocationDTO {
  def apply(): ResourceLocationDTO = new ResourceLocationDTO(
    UUID.randomUUID(),
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    -1,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    new Date(),
    new Date())
}
