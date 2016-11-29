package jp.co.rn.inthermis.elasticsearch

case class ElasticSearchResponse[T](
  hits: Seq[T],
  total: Int)
