package jp.co.rn.inthermis.controllers.action.search

import java.time.LocalDateTime

import jp.co.rn.inthermis.elasticsearch.types.WordIndex
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.time.format.DateTimeFormatter
import java.util.UUID

case class SearchResponse(
  searchResults: Seq[WordIndex],
  resultCount: Int,
  ellapsedTime: Long,
  isLastResponse: Boolean)

object SearchResponse {
  def apply(): SearchResponse = SearchResponse(Seq.empty, 0, 0, true)

  implicit val localDateTimeWrites = Writes.temporalWrites[LocalDateTime, DateTimeFormatter](
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

  implicit val searchResultWrites: Writes[WordIndex] = (
    (__ \ "uri").write[String] and
    (__ \ "resourceLocationId").write[String] and
    (__ \ "displayLocation").write[String] and
    (__ \ "resourceName").write[String] and
    (__ \ "resourceSize").write[Long] and
    (__ \ "resourceTypeName").write[String] and
    (__ \ "resourceCreated").write[LocalDateTime] and
    (__ \ "resourceModified").write[LocalDateTime] and
    (__ \ "keys").write[Seq[String]] and
    OWrites[Any](_ => Json.obj()) and
    (__ \ "content").write[String] and
    (__ \ "indexerClassName").write[String] and
    (__ \ "iconCssClassName").write[String])(unlift(WordIndex.unapply))

  implicit val searchResponseWrites: Writes[SearchResponse] = Json.writes[SearchResponse]
}

