package jp.co.rn.inthermis.models

import java.io.InputStream
import java.net.URI
import java.time.LocalDateTime
import scala.io.Codec

trait IndexerResource {
  def uri: URI
  def displayLocation: String
  def name: String
  def size: Long
  def created: LocalDateTime
  def lastModified: LocalDateTime

  def getInputStream: InputStream
  def getCodec: Codec
}
