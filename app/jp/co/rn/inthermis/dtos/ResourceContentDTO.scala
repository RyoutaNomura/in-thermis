package jp.co.rn.inthermis.dtos

import java.util.UUID

import org.apache.commons.lang3.StringUtils

case class ResourceContentDTO(
    var resourceContentId: UUID,
    var key1: String,
    var key2: String,
    var key3: String,
    var content: String,
    var prevContent: String,
    var nextContent: String,
    var resourceLocationId: UUID) {

  def this() = this(
    UUID.randomUUID,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    UUID.randomUUID)
}
