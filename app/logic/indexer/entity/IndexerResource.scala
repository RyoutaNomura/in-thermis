package logic.indexer.entity

import java.io.InputStream
import java.net.URI
import java.util.Date

trait IndexerResource {
  def uri: URI
  def displayLocation: String
  def name: String
  def size: Long
  def created: Date
  def lastModified: Date

  def getInputStream: InputStream
}