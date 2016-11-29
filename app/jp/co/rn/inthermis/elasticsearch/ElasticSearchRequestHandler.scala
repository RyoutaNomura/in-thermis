package jp.co.rn.inthermis.elasticsearch

import jp.co.rn.inthermis.settings.ApplicationConfig
import jp.co.rn.inthermis.elasticsearch.index._
import play.Logger
import play.api.libs.json._

import play.api.libs.json.Reads
import scalaj.http.Http
import scalaj.http.HttpRequest

object ElasticSearchRequestHandler {

  val logger = Logger.of(ElasticSearchRequestHandler.getClass)

  def generateSearchRequest(criteria: ElasticSearchCriteria): HttpRequest =
    Http(ApplicationConfig.generateElasticSearchUriPrefix + "word/entry/_search")
      .postData(criteria.toJson.toString)
      .method("GET")
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")

  def search[T](criteria: ElasticSearchCriteria)(implicit reads: Reads[T]): ElasticSearchResponse[T] = {

    val req = generateSearchRequest(criteria)
    logger.debug(s"Request: ${req}")
    val res = req.asString

    if (res.isSuccess) {
      val json = Json.parse(res.body)

      val total = (json \ "hits" \ "total") match {
        case JsDefined(v) => v match {
          case JsNumber(n) => n.toInt
          case _           => 0
        }
        case u: JsUndefined =>
          u.validationError.messages.foreach { logger.error }
          0
      }

      val entities = (json \ "hits" \ "hits") match {
        case JsDefined(v) => v match {
          case JsArray(arr) => arr.foldLeft(Seq.newBuilder[T])((builder, elm) => {
            builder += elm.as[T](reads)
          }).result
          case _ => Seq.empty
        }
        case undefined: JsUndefined =>
          undefined.validationError.messages.foreach { logger.error }
          Seq.empty
      }
      ElasticSearchResponse(entities, total)

    } else {
      res.throwError
      ElasticSearchResponse(Seq.empty, 0)
    }
  }

  def generateRegisterRequest[T](indices: Traversable[T])(implicit writes: Writes[T]): HttpRequest =
    Http(ApplicationConfig.generateElasticSearchUriPrefix + "_bulk")
      .postData(Json.obj("index" -> Json.obj("_index" -> "word")).toString)
      .postData(indices.map { x => Json.toJson(x)(writes) }.mkString(""))
      .method("GET")
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")

  def registerIndex[T](indices: Traversable[T])(implicit writes: Writes[T]): Set[T] = {

    val req = generateRegisterRequest(indices)
    logger.debug(s"Request: ${req}")

    val res = req.asString
    if (res.isSuccess) {
      val json = Json.parse(res.body)
      println(json.toString)
      indices.toSet
    } else {
      Set.empty
    }
  }
}
