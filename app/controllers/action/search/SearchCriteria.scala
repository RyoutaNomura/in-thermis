package controllers.action.search

import org.apache.commons.lang3.StringUtils

import enums.{ DateRangeCriteria, SearchResultOrder }
import play.api.mvc.RequestHeader

case class SearchCriteria(
  text: String,
  order: SearchResultOrder,
  fetchSize: Int,
  currentPage: Int,
  dateRangeCriteria: DateRangeCriteria,
  resourceWalkers: Set[String],
  resourceIndices: Set[String])

case object SearchCriteria {

  val defaultFetchSize = 5

  def apply(request: RequestHeader): SearchCriteria = {
    var text = StringUtils.EMPTY
    var order: SearchResultOrder = SearchResultOrder.COUNT_DESC
    var fetchSize = defaultFetchSize
    var currentPage = 0
    var dateRangeCriteria: DateRangeCriteria = DateRangeCriteria.THIS_YEAR
    var resourceWalkers: Set[String] = Set.empty
    var resourceIndices: Set[String] = Set.empty

    request.queryString.foreach(f => {
      f._1 match {
        case "text"              => text = f._2.head
        case "order"             => order = SearchResultOrder.valueOf(f._2.head)
        case "fetchSize"         => fetchSize = f._2.head.toInt
        case "currentPage"       => currentPage = f._2.head.toInt
        case "dateRangeCriteria" => dateRangeCriteria = DateRangeCriteria.valueOf(f._2.head)
        case "resourceWalkers"   => resourceWalkers = f._2.toSet
        case "resourceIndices"   => resourceIndices = f._2.toSet
      }
    })
    SearchCriteria(text, order, fetchSize, currentPage, dateRangeCriteria, resourceWalkers, resourceIndices)
  }
}
