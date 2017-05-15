package jp.co.rn.inthermis.utils

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object FileTimeUtils {
  def getCreated(uri: URI): LocalDateTime = getLastModified(uri)
  def getLastModified(uri: URI): LocalDateTime = {
    val filetime = Files.getLastModifiedTime(Paths.get(uri))
    LocalDateTime.ofInstant(filetime.toInstant, ZoneOffset.UTC)
  }
}
