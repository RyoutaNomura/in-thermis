package dtos

import java.util.{ Date, UUID }

import org.apache.commons.lang3.StringUtils

case class WordIndicesDTO(
    var word: String,
    var resourceLocationId: UUID,
    var count: Long,
    var resourceUpdated: Date,
    var resourceUri: String,
    var resourceName: String,
    var indices: Map[UUID, Set[Tuple2[Int, Int]]]) {

  def this() = this(
    StringUtils.EMPTY,
    UUID.randomUUID,
    -1,
    new Date,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    Map.empty)
}

object WordIndicesDTO {
  def apply(): WordIndicesDTO = this()
}
