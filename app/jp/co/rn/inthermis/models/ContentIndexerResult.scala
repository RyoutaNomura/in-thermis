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
import jp.co.rn.inthermis.logic.indexer.FileIndexerFactory

case class ContentIndexerResult(
    val uri: URI,
    val displayLocation: String,
    val name: String,
    val size: Long,
    val resourceCreated: LocalDateTime,
    val resourceModified: LocalDateTime,
    val contents: Map[Seq[String], String],
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

  def generateResourceContentDTOs: Seq[ResourceContentDTO] = Seq.empty

  def generateWordIndexESDTO(resourceLocationId: UUID, walkerClassName: String): Set[WordIndex] = {

    val indexer = FileIndexerFactory.create(indexerClassName)

    contents.map(f => {
      WordIndex(
        uri.toString(),
        resourceLocationId.toString,
        displayLocation,
        name,
        size,
        indexer.getResourceTypeName,
        resourceCreated,
        resourceModified,
        f._1,
        f._2,
        StringUtils.EMPTY,
        indexerClassName,
        indexer.getIconCssClassName)
    }).toSet
  }
}

object ContentIndexerResult {
  def apply(): ContentIndexerResult = ContentIndexerResult(
    URI.create(StringUtils.EMPTY),
    StringUtils.EMPTY,
    StringUtils.EMPTY,
    -1,
    LocalDateTime.now,
    LocalDateTime.now,
    Map.empty,
    StringUtils.EMPTY,
    LocalDateTime.now)

  def apply(resource: IndexerResource, contents: Map[Seq[String], String], indexerClassName: String): ContentIndexerResult = ContentIndexerResult(
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

