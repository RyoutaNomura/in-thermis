package dtos

import org.apache.commons.lang3.StringUtils
import java.util.UUID

case class ResourceContentDTO(
    var id: UUID,
    var key1: String,
    var key2: String,
    var key3: String,
    var content: String,
    var resourceLocationId: UUID) {

  def this() = this(
    UUID.randomUUID,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    UUID.randomUUID)
}