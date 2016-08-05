package dtos

import org.apache.commons.lang3.StringUtils

case class WordIndicesByKeyDTO(
    var word: String,
    var indexType: String,
    var searchKey: String,
    var result: Map[String, Any]) {
}

object WordIndicesByKeyDTO {
  def apply(): WordIndicesByKeyDTO = new WordIndicesByKeyDTO(
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    Map.empty)
}
