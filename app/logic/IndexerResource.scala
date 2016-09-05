package logic

import java.io.InputStream
import java.net.URI
import java.time.LocalDateTime

trait IndexerResource {
  def uri: URI
  def displayLocation: String
  def name: String
  def size: Long
  def created: LocalDateTime
  def lastModified: LocalDateTime

  def getInputStream: InputStream
}
