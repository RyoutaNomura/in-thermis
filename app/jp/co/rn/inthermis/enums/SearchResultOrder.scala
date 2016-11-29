package jp.co.rn.inthermis.enums

import scala.reflect.runtime.universe

import jp.co.rn.inthermis.utils.{ EnumClass, EnumObject }
import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, __ }

sealed abstract case class SearchResultOrder(key: String, displayName: String) extends EnumClass {
  override def getKey: String = key
}

object SearchResultOrder extends EnumObject[SearchResultOrder] {
  //  object COUNT_DESC extends SearchResultOrder("COUNT_DESC", "件数の多い順")
  //  object COUNT_ASC extends SearchResultOrder("COUNT_ASC", "件数の少ない順")
  object SCORE extends SearchResultOrder("SCORE", "スコア順")
  object RESOURCE_UPDATED_DESC extends SearchResultOrder("RESOURCE_UPDATED_DESC", "更新日の新しい順")
  object RESOURCE_UPDATED_ASC extends SearchResultOrder("RESOURCE_UPDATED_ASC", "更新日の古い順")
  object RESOURCE_URI_ASC extends SearchResultOrder("RESOURCE_URI_ASC", "URI順")
  object RESOURCE_URI_DESC extends SearchResultOrder("RESOURCE_URI_DESC", "URIの逆順")
  object RESOURCE_NAME_ASC extends SearchResultOrder("RESOURCE_NAME_ASC", "ファイル名順")
  object RESOURCE_NAME_DESC extends SearchResultOrder("RESOURCE_NAME_DESC", "ファイル名の逆順")

  implicit val searchResultWrites: Writes[SearchResultOrder] = (
    (__ \ "key").write[String] and
    (__ \ "displayName").write[String])(unlift(SearchResultOrder.unapply))
}
