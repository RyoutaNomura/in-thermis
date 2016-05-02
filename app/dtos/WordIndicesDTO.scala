package dtos

import java.util.UUID

import org.apache.commons.lang3.StringUtils

case class WordIndicesDTO(
    var resourceLocationId: UUID,
    var word: String,
    var count: Long,
    var indices: Map[UUID, Set[Tuple2[Int, Int]]]) {

  def this() = this(
    UUID.randomUUID,
    StringUtils.EMPTY,
    -1,
    Map.empty)
}
