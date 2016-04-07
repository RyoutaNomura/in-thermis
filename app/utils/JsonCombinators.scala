package utils

import language.higherKinds
import play.api.libs.json.JsArray
import play.api.libs.json.Writes

object JsonCombinators {
  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[Tuple2[A, B]] = new Writes[Tuple2[A, B]] {
    def writes(tuple: Tuple2[A, B]) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
  }
}