package controllers.action.search

import java.util.Date
import org.apache.commons.lang3.StringUtils
import play.api.libs.functional.syntax.{ toFunctionalBuilderOps, unlift }
import play.api.libs.json.{ Json, Writes, __ }
import utils.JsonCombinators

case class SearchResponse(
  searchResults: Seq[SearchResult],
  resultCount: Int,
  ellapsedTime: Long,
  isLastResponse: Boolean)

object SearchResponse {
  implicit val searchResponseWrites = Json.writes[SearchResponse]
}

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
    prevContent: String,
    nextContent: String,
    positions: Set[Tuple2[Int, Int]],
    indexerClassName: String) {

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
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    Set.empty,
    StringUtils.EMPTY)
}

object SearchResult {
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
    (__ \ "prevContent").write[String] and
    (__ \ "nextContent").write[String] and
    (__ \ "positions").lazyWrite(Writes.traversableWrites[Tuple2[Int, Int]](JsonCombinators.tuple2Writes)) and
    (__ \ "indexerClassName").write[String])(unlift(SearchResult.unapply))
}
