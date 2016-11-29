package jp.co.rn.inthermis.elasticsearch

import play.api.libs.json._
import play.api.libs.functional.syntax._

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

case class Query(simpleQueryString: SimpleQueryString)
object Query {
  implicit val writes: Writes[Query] = (
    (__ \ "simple_query_string").write[SimpleQueryString]).contramap { (query: Query) => query.simpleQueryString }
}

case class SimpleQueryString(fields: Set[String], query: String)
object SimpleQueryString {
  implicit val writes: Writes[SimpleQueryString] = Json.writes[SimpleQueryString]
}

case class Highlight(preTags: String, postTags: String, fields: Fields)
object Highlight {
  implicit val writes: Writes[Highlight] = (
    (__ \ "pre_tags").write[String] and
    (__ \ "post_tags").write[String] and
    (__ \ "fields").write[Fields])(unlift(Highlight.unapply))
}

case class Fields(content: Map[String, String])
object Fields {
  implicit val writes: Writes[Fields] = Json.writes[Fields]
}
