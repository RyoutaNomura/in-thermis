package jp.co.rn.inthermis.controllers.action.loadInitData

import jp.co.rn.inthermis.enums.DateRangeCriteria
import play.api.libs.json.Json
import jp.co.rn.inthermis.utils.JsonCombinators
import play.api.libs.json.Writes

case class LoadInitDataResponse(
  searchOrderOptions: Seq[Map[String, String]],
  dateRangeCriteria: Seq[Map[String, String]],
  resourceWalkers: Seq[Map[String, String]],
  resourceIndexers: Seq[Map[String, String]])

object LoadInitDataResponse {
  implicit val responseWrites: Writes[LoadInitDataResponse] = Json.writes[LoadInitDataResponse]
}
