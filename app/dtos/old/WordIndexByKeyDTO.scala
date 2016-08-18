package dtos.old

import org.apache.commons.lang3.StringUtils
import dtos.WordIndexDTO

case class WordIndexByKeyDTO(
    var searchKey: String,
    var keyId: String,
    var result: List[WordIndexDTO]) {
}

object WordIndexByKeyDTO {
  def apply(): WordIndexByKeyDTO = new WordIndexByKeyDTO(
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    List.empty)
}
