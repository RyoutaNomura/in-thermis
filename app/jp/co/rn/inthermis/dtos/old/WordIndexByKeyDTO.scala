package jp.co.rn.inthermis.dtos.old

import org.apache.commons.lang3.StringUtils
import jp.co.rn.inthermis.dtos.WordIndexDTO

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
