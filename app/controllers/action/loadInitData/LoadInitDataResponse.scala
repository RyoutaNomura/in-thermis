package controllers.action.loadInitData

import enums.DateRangeCriteria
import play.api.libs.json.Json
import utils.JsonCombinators
import play.api.libs.json.Writes

case class LoadInitDataResponse(
  searchOrderOptions: Seq[Map[String, String]],
  dateRangeCriteria: Seq[Map[String, String]],
  resourceWalkers: Seq[Map[String, String]],
  resourceIndices: Seq[Map[String, String]])

object LoadInitDataResponse {
  implicit val responseWrites: Writes[LoadInitDataResponse] = Json.writes[LoadInitDataResponse]
}
