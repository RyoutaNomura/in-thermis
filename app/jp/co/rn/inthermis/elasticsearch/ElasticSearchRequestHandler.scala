package jp.co.rn.inthermis.elasticsearch

import org.apache.commons.lang3.StringUtils

import jp.co.rn.inthermis.settings.ApplicationConfig
import play.Logger
import play.api.libs.json._
import scalaj.http.Http
import scalaj.http.HttpRequest
import scala.util.Try
import scala.util.Failure

object ElasticSearchRequestHandler {

  private val logger = Logger.of(ElasticSearchRequestHandler.getClass)

  private def generateSearchRequest(criteria: ElasticSearchCriteria): HttpRequest =
    Http(s"${ApplicationConfig.elasticSearchUri}${ApplicationConfig.elasticSearchIndexName}/${ApplicationConfig.elasticSearchType}/_search")
      .postData(criteria.toJson.toString)
      .method("GET")
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")

  def search[T](criteria: ElasticSearchCriteria)(implicit reads: Reads[T]): ElasticSearchResponse[T] = {

    val req = generateSearchRequest(criteria)
//    logger.debug(s"Request: ${req}")
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
            logger.debug(elm.toString)
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

  private def generateCreateIndexRequest[T](indices: Traversable[T])(implicit writes: Writes[T]): Option[HttpRequest] = {

    val post = indices.foldLeft(Seq.newBuilder[JsValue]) { (builder, i) =>
      builder += Json.obj("index" -> Json.obj(
        "_index" -> ApplicationConfig.elasticSearchIndexName,
        "_type" -> ApplicationConfig.elasticSearchType))
      builder += Json.toJson(i)
    }.result

    post.size match {
      case 0 => Option.empty
      case _ => Option(
        Http(ApplicationConfig.elasticSearchUri + "_bulk")
          .postData(post.mkString(StringUtils.EMPTY, System.lineSeparator, System.lineSeparator))
          .method("GET")
          .header("Content-Type", "application/json")
          .header("Charset", "UTF-8"))
    }
  }

  def createIndices[T](indices: Traversable[T])(implicit writes: Writes[T]): Unit = {

    generateCreateIndexRequest(indices) match {

      case Some(s) => s.asString match {

        case res if res.isError =>
          logger.debug(s"error occurred during register follow data")
          logger.debug(indices.mkString(System.lineSeparator))
          throw new RuntimeException(res.body)

        case res => (Json.parse(res.body) \ "errors") match {

          case JsDefined(d) => d match {
            case JsBoolean(b) if (b == true) => throw new RuntimeException(res.body)
            case _                           =>
          }

          case u: JsUndefined => {
            throw new RuntimeException(res.body)
          }
        }
      }

      case None => logger.warn("no data to register")
    }
  }

  private def generateDeleteRequest(criteria: ElasticSearchCriteria): HttpRequest =
    Http(s"${ApplicationConfig.elasticSearchUri}${ApplicationConfig.elasticSearchIndexName}/${ApplicationConfig.elasticSearchType}/_delete_by_query")
      .postData(criteria.toJson.toString)
      .method("GET")
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")

  def deleteIndices(criteria: ElasticSearchCriteria): Try[Int] = Try {
    val req = generateDeleteRequest(criteria)
    logger.debug(s"Request: ${req}")

    val res = req.asString
    res match {
      case r if r.isError => {
        res.throwError
        0
      }
      case r => {
        val json = Json.parse(r.body)
        logger.debug(json.toString)

        (json \ "deleted") match {
          case JsDefined(JsNumber(n)) => n.intValue
          case _                      => throw new RuntimeException("delete not found")
        }
      }
    }
  }

}