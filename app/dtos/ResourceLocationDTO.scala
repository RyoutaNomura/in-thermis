package dtos

import java.util.Date
import org.apache.commons.lang3.StringUtils
import java.util.UUID
import java.net.URI

case class ResourceLocationDTO(
    var id: UUID,
    var uri: String,
    var name: String,
    var size: Long,
    var created: Date,
    var modified: Date,
    var indexerClassName: String,
    var indexGenerated: Date) {

  def this() = this(
      UUID.randomUUID, 
      StringUtils.EMPTY, 
      StringUtils.EMPTY,
      -1,
      new Date,
      new Date,
      StringUtils.EMPTY,
      new Date)
}