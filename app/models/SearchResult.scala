package models

import java.net.URI
import java.util.Date
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.JsonCombinators

case class SearchResult(
    word: String,
    uri: String,
    resourceName: String,
    resourceSize: Long,
    resourceTypeName: String,
    resourceCreated: Date,
    resourceModified: Date,
    keys: String,
    content: String,
    positions: Set[Tuple2[Int, Int]],
    indexerClassName: String,
    indexGenerated: Date) {

  def this() = this(
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    0,
    StringUtils.EMPTY,
    new Date,
    new Date,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    Set.empty,
    StringUtils.EMPTY,
    new Date)
}

object SearchResult {
  def apply() = {
    new SearchResult()
  }

  implicit val searchResultWrites: Writes[SearchResult] = (
    (__ \ "word").write[String] and
    (__ \ "uri").write[String] and
    (__ \ "resourceName").write[String] and
    (__ \ "resourceSize").write[Long] and
    (__ \ "resourceTypeName").write[String] and
    (__ \ "resourceCreated").write[Date](Writes.dateWrites("yyyy-MM-dd")) and
    (__ \ "resourceModified").write[Date](Writes.dateWrites("yyyy-MM-dd")) and
    (__ \ "keys").write[String] and
    (__ \ "content").write[String] and
    (__ \ "positions").lazyWrite(Writes.traversableWrites[Tuple2[Int, Int]](JsonCombinators.tuple2Writes)) and
    (__ \ "indexerClassName").write[String] and
    (__ \ "indexGenerated").write[Date](Writes.dateWrites("yyyy-MM-dd"))
  )(unlift(SearchResult.unapply))
}