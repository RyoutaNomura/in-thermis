package jp.co.rn.inthermis.elasticsearch.types

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WordIndex(
    uri: String,
    resourceLocationId: String,
    displayLocation: String,
    resourceName: String,
    resourceSize: Long,
    resourceTypeName: String,
    resourceCreated: LocalDateTime,
    resourceModified: LocalDateTime,
    keys: Seq[String],
    content: String,
    highlightendContent: String,
    indexerClassName: String,
    iconCssClassName: String) {
}

object WordIndex {
  val contentArrayRead: Reads[String] = new Reads[String] {
    override def reads(json: JsValue): JsResult[String] = json match {
      case JsArray(d) =>
        JsSuccess(d.map(_.as[JsString].value).mkString(" ...... "))
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.jsArray"))))
    }
  }

  implicit val elasticsearchRead: Reads[WordIndex] = (
    (__ \ "_source" \ "uri").read[String] and
    (__ \ "_source" \ "resource_location_id").read[String] and
    (__ \ "_source" \ "display_location").read[String] and
    (__ \ "_source" \ "resource_name").read[String] and
    (__ \ "_source" \ "resource_size").read[Long] and
    (__ \ "_source" \ "resource_type_name").read[String] and
    (__ \ "_source" \ "resource_created").read[LocalDateTime](Reads.localDateTimeReads("yyyy-MM-dd HH:mm:ss")) and
    (__ \ "_source" \ "resource_modified").read[LocalDateTime](Reads.localDateTimeReads("yyyy-MM-dd HH:mm:ss")) and
    (__ \ "_source" \ "keys").read[Seq[String]] and
    (__ \ "_source" \ "content").read[String] and
    (__ \ "highlight" \ "content").read[String](contentArrayRead) and
    (__ \ "_source" \ "indexer_class_name").read[String] and
    (__ \ "_source" \ "icon_css_class_name").read[String])(WordIndex.apply _)

  implicit val localDateTimeWrites = Writes.temporalWrites[LocalDateTime, DateTimeFormatter](
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

  implicit val elasticsearchWrite: Writes[WordIndex] = (
    (__ \ "uri").write[String] and
    (__ \ "resource_location_id").write[String] and
    (__ \ "display_location").write[String] and
    (__ \ "resource_name").write[String] and
    (__ \ "resource_size").write[Long] and
    (__ \ "resource_type_name").write[String] and
    (__ \ "resource_created").write[LocalDateTime] and
    (__ \ "resource_modified").write[LocalDateTime] and
    (__ \ "keys").write[Seq[String]] and
    (__ \ "content").write[String] and
    OWrites[Any](_ => Json.obj()) and
    (__ \ "indexer_class_name").write[String] and
    (__ \ "icon_css_class_name").write[String])(unlift(WordIndex.unapply))
}
