package jp.co.rn.inthermis.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json._
import org.apache.commons.lang3.StringUtils

case class ElasticSearchCriteria(
    query: Query,
    from: Int,
    size: Int,
    highlight: Highlight,
    sort: Seq[Map[String, String]]) {

  def toJson: JsValue = Json.toJson(this)
}
object ElasticSearchCriteria {
  implicit val writes: Writes[ElasticSearchCriteria] = Json.writes[ElasticSearchCriteria]
}

class Query
object Query {
  implicit val writes: Writes[Query] = Writes(query =>
    query match {
      case s: SimpleQuery => Json.toJson(s)(SimpleQuery.writes)
      case m: MatchQuery  => Json.toJson(m)(MatchQuery.writes)
    })
}

case class SimpleQuery(simpleQueryString: SimpleQueryString) extends Query
object SimpleQuery {
  implicit val writes: Writes[SimpleQuery] = (
    (__ \ "simple_query_string").write[SimpleQueryString]).contramap { (query: SimpleQuery) => query.simpleQueryString }
}

case class MatchQuery(matchQuery: Map[String, String]) extends Query
object MatchQuery {
  implicit val writes: Writes[MatchQuery] = (
    (__ \ "match").write[Map[String, String]]).contramap { (query: MatchQuery) => query.matchQuery }
}

case class SimpleQueryString(fields: Set[String], query: String)
object SimpleQueryString {
  implicit val writes: Writes[SimpleQueryString] = Json.writes[SimpleQueryString]
}

case class Highlight(preTags: String, postTags: String, fields: Fields)
object Highlight {
  def apply(): Highlight = Highlight(StringUtils.EMPTY, StringUtils.EMPTY, Fields())
  implicit val writes: Writes[Highlight] = (
    (__ \ "pre_tags").write[String] and
    (__ \ "post_tags").write[String] and
    (__ \ "fields").write[Fields])(unlift(Highlight.unapply))
}

case class Fields(content: Map[String, String])
object Fields {
  def apply(): Fields = Fields(Map.empty)
  implicit val writes: Writes[Fields] = Json.writes[Fields]
}
