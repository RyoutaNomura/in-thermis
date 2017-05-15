package jp.co.rn.inthermis.models

import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.UUID

import scala.collection.Seq

import org.apache.commons.lang3.StringUtils

import jp.co.rn.inthermis.dtos._
import jp.co.rn.inthermis.elasticsearch.types.WordIndex

case class LineIndexerResult(
    val uri: URI,
    val displayLocation: String,
    val name: String,
    val size: Long,
    val resourceCreated: LocalDateTime,
    val resourceModified: LocalDateTime,
    val contents: Seq[Content],
    val indexerClassName: String,
    val indexGenerated: LocalDateTime) {

  private val locationId = UUID.randomUUID
  private val contentIds = contents.map { c => (c, UUID.randomUUID) }.toMap

  def generateResourceLocationDTO(walkerClassName: String): ResourceLocationDTO = {
    ResourceLocationDTO(
      locationId,
      uri.toString,
      displayLocation,
      name,
      size,
      walkerClassName,
      indexerClassName,
      Date.from(resourceModified.toInstant(ZoneOffset.UTC)),
      Date.from(indexGenerated.toInstant(ZoneOffset.UTC)))
  }

  def generateResourceContentDTOs: Seq[ResourceContentDTO] = {
    contentIds.map { f =>
      var contentId = f._2
      var content = f._1
      ResourceContentDTO(
        contentId,
        content.key1,
        content.key2,
        content.key3,
        content.content,
        content.prevContent,
        content.nextContent,
        locationId)
    }.toSeq
  }

  def generateWordIndexESDTO(resourceLocationId: UUID, walkerClassName: String): Seq[WordIndex] = {
    contents.map { c =>
      WordIndex(
        uri.toString(),
        resourceLocationId.toString,
        displayLocation,
        name,
        size,
        walkerClassName,
        resourceCreated,
        resourceModified,
        Seq(c.key1, c.key2, c.key3),
        c.content,
        StringUtils.EMPTY,
        indexerClassName,
        StringUtils.EMPTY)
    }
  }
}

object LineIndexerResult {
  def apply(): LineIndexerResult = LineIndexerResult(
    URI.create(StringUtils.EMPTY),
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    -1,
    LocalDateTime.now,
    LocalDateTime.now,
    Seq.empty,
    StringUtils.EMPTY,
    LocalDateTime.now)

  def apply(resource: IndexerResource,
    contents: Seq[Content],
    indexerClassName: String): LineIndexerResult = LineIndexerResult(
    resource.uri,
    resource.displayLocation,
    resource.name,
    resource.size,
    resource.created,
    resource.lastModified,
    contents,
    indexerClassName,
    LocalDateTime.now)
}

case class Content(
  var key1: String,
  var key2: String,
  var key3: String,
  var content: String,
  var prevContent: String,
  var nextContent: String,
  indices: Seq[Tuple3[String, Int, Int]])

object Content {
  def apply(): Content = Content(
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    Seq.empty)
}

