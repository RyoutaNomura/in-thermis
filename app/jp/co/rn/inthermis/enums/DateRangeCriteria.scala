package jp.co.rn.inthermis.enums

import scala.reflect.runtime.universe
import play.api.libs.functional.syntax.{ toFunctionalBuilderOps, unlift }
import play.api.libs.json.{ Writes, __ }
import jp.co.rn.inthermis.utils.{ EnumClass, EnumObject }
import java.util.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

sealed abstract case class DateRangeCriteria(key: String, displayName: String) extends EnumClass {
  override def getKey: String = key
  def resolve: LocalDate = {
    this match {
      case DateRangeCriteria.TODAY      => LocalDate.now
      case DateRangeCriteria.THIS_WEEK  => LocalDate.now.minusWeeks(1)
      case DateRangeCriteria.THIS_MONTH => LocalDate.now.minusMonths(1)
      case DateRangeCriteria.THIS_YEAR  => LocalDate.now.minusYears(1)
      case DateRangeCriteria.ALL        => LocalDate.of(1900, 1, 1)
      case _                            => throw new IllegalStateException()
    }
  }
  def resolveAsDate: Date = Date.from(resolve.atStartOfDay(ZoneId.systemDefault).toInstant)
}

object DateRangeCriteria extends EnumObject[DateRangeCriteria] {
  object TODAY extends DateRangeCriteria("TODAY", "Today")
  object THIS_WEEK extends DateRangeCriteria("THIS_WEEK", "This week")
  object THIS_MONTH extends DateRangeCriteria("THIS_MONTH", "This month")
  object THIS_YEAR extends DateRangeCriteria("THIS_YEAR", "This year")
  object ALL extends DateRangeCriteria("ALL", "All")

  implicit val searchResultWrites: Writes[SearchResultOrder] = (
    (__ \ "key").write[String] and
    (__ \ "displayName").write[String])(unlift(SearchResultOrder.unapply))
}
