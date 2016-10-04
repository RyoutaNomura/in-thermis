package jp.co.rn.inthermis.dtos

import java.util.{ Date, UUID }

import org.apache.commons.lang3.StringUtils

case class WordIndexDTO(
    var word: String,
    var resourceContentId: UUID,
    var indices: Map[Int, Int],
    var indicesCount: Int,
    var content: String,
    var prevContent: String,
    var nextContent: String,
    var contentKey1: String,
    var contentKey2: String,
    var contentKey3: String,
    var resourceUri: String,
    var resourceDisplayLocation: String,
    var resourceName: String,
    var resourceSize: Long,
    var resourceWalkerName: String,
    var resourceIndexerName: String,
    var resourceLastModified: Date,
    var resourceLocationId: UUID) {
}

object WordIndexDTO {
  def apply(): WordIndexDTO =
    new WordIndexDTO(
      StringUtils.EMPTY,
      UUID.randomUUID(),
      Map.empty,
      0,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      0,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      new Date(),
      UUID.randomUUID())

}
