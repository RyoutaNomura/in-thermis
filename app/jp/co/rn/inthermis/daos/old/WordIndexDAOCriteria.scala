package jp.co.rn.inthermis.daos.old

import java.util.Date

class WordIndexDAOCriteria(
  var resourceWalkerName: Option[String],
  var resourceIndexerName: Option[String],
  var resourceLastModified: Option[Date])

object WordIndexDAOCriteria {
  def apply: WordIndexDAOCriteria = {
    new WordIndexDAOCriteria(
      Option.empty,
      Option.empty,
      Option.empty)
  }
}
