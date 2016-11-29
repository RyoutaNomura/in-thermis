package jp.co.rn.inthermis.controllers.action.search

import scala.collection.immutable.Map

import org.apache.commons.lang3.StringUtils

import jp.co.rn.inthermis.elasticsearch.ElasticSearchCriteria
import jp.co.rn.inthermis.enums.{ DateRangeCriteria, SearchResultOrder }
import jp.co.rn.inthermis.enums.SearchResultOrder._
import play.api.libs.json._
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.RequestHeader
import jp.co.rn.inthermis.elasticsearch._

case class SearchCriteria(
    text: String,
    order: SearchResultOrder,
    fetchSize: Int,
    currentPage: Int,
    dateRangeCriteria: DateRangeCriteria,
    resourceWalkers: Set[String],
    resourceIndexers: Set[String]) {

  def toElasticSearchCriteria: ElasticSearchCriteria = {
    ElasticSearchCriteria(
      Query(
        SimpleQueryString(
          Set("content"),
          text)),
      (currentPage * fetchSize),
      fetchSize,
      Highlight("<span class='highlightend-text'>", "</span>", Fields(Map.empty[String, String])),
      Seq(
        order match {
          case SCORE                 => Map.empty
          case RESOURCE_UPDATED_DESC => Map("resource_modified" -> "desc")
          case RESOURCE_UPDATED_ASC  => Map("resource_modified" -> "asc")
          case RESOURCE_URI_ASC      => Map("uri" -> "asc")
          case RESOURCE_URI_DESC     => Map("uri" -> "desc")
          case RESOURCE_NAME_ASC     => Map("resource_name" -> "asc")
          case RESOURCE_NAME_DESC    => Map("resource_name" -> "desc")
        }))
  }
  //
  //  def toQueryJson: JsObject = {
  //    Json.obj(
  //      "query" -> Json.obj(
  //        "simple_query_string" -> Json.obj(
  //          "fields" -> Json.arr("content"),
  //          "query" -> text)),
  //      "from" -> (currentPage * fetchSize),
  //      "size" -> fetchSize,
  //      "highlight" -> Json.obj(
  //        "pre_tags" -> Json.arr("<span class='highlightend-text'>"),
  //        "post_tags" -> Json.arr("</span>"),
  //        "fields" -> Json.obj(
  //          "content" -> Json.obj())),
  //      "sort" -> Json.arr(
  //        order match {
  //          case SCORE                 => "_score"
  //          case RESOURCE_UPDATED_DESC => Json.obj("resource_modified" -> "desc")
  //          case RESOURCE_UPDATED_ASC  => Json.obj("resource_modified" -> "asc")
  //          case RESOURCE_URI_ASC      => Json.obj("uri" -> "asc")
  //          case RESOURCE_URI_DESC     => Json.obj("uri" -> "desc")
  //          case RESOURCE_NAME_ASC     => Json.obj("resource_name" -> "asc")
  //          case RESOURCE_NAME_DESC    => Json.obj("resource_name" -> "desc")
  //        }))
  //  }
}

case object SearchCriteria {

  val defaultFetchSize = 5

  def apply(request: RequestHeader): SearchCriteria = {
    var text = StringUtils.EMPTY
    var order: SearchResultOrder = SearchResultOrder.SCORE
    var fetchSize = defaultFetchSize
    var currentPage = 0
    var dateRangeCriteria: DateRangeCriteria = DateRangeCriteria.THIS_YEAR
    var resourceWalkers: Set[String] = Set.empty
    var resourceIndexers: Set[String] = Set.empty

    request.queryString.foreach(f => {
      if (StringUtils.isNotEmpty(f._2.head)) {
        f._1 match {
          case "text"              => text = f._2.head
          case "order"             => order = SearchResultOrder.valueOf(f._2.head)
          case "fetchSize"         => fetchSize = f._2.head.toInt
          case "currentPage"       => currentPage = f._2.head.toInt
          case "dateRangeCriteria" => dateRangeCriteria = DateRangeCriteria.valueOf(f._2.head)
          case "resourceWalkers"   => resourceWalkers = f._2.toSet
          case "resourceIndexers"  => resourceIndexers = f._2.toSet
        }
      }
    })
    SearchCriteria(text, order, fetchSize, currentPage, dateRangeCriteria, resourceWalkers, resourceIndexers)
  }
}
