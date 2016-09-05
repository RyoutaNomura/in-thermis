package utils

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneOffset

object FileTimeUtils {
  def getCreated(uri: URI): LocalDateTime = getLastModified(uri)
  def getLastModified(uri: URI): LocalDateTime = {
    LocalDateTime.ofInstant(Instant.ofEpochMilli(Files.getLastModifiedTime(Paths.get(uri)).toMillis), ZoneOffset.UTC)
  }
}
