package utils

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Date

object FileTimeUtils {
  def getCreated(uri: URI): Date = getLastModified(uri)
  def getLastModified(uri: URI): Date = {
    new Date(Files.getLastModifiedTime(Paths.get(uri)).toMillis)
  }
}
