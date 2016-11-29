package jp.co.rn.inthermis.controllers.action.search

import java.util.Date
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.libs.functional.syntax._
import jp.co.rn.inthermis.utils.JsonCombinators
import jp.co.rn.inthermis.elasticsearch.index.WordIndex

case class SearchResponse(
  searchResults: Seq[WordIndex],
  resultCount: Int,
  ellapsedTime: Long,
  isLastResponse: Boolean)

object SearchResponse {
  def apply(): SearchResponse = SearchResponse(Seq.empty, 0, 0, true)

  implicit val searchResultWrites: Writes[WordIndex] = (
    (__ \ "uri").write[String] and
    (__ \ "displayLocation").write[String] and
    (__ \ "resourceName").write[String] and
    (__ \ "resourceSize").write[Int] and
    (__ \ "resourceTypeName").write[String] and
    (__ \ "resourceCreated").write[Date](Writes.dateWrites("yyyy-MM-dd")) and
    (__ \ "resourceModified").write[Date](Writes.dateWrites("yyyy-MM-dd")) and
    (__ \ "keys").write[String] and
    OWrites[Any](_ => Json.obj()) and
    (__ \ "content").write[String] and
    (__ \ "indexerClassName").write[String] and
    (__ \ "iconCssClassName").write[String])(unlift(WordIndex.unapply))

  implicit val searchResponseWrites: Writes[SearchResponse] = Json.writes[SearchResponse]
}

