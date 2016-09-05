package models

import java.net.URI
import java.util.{ Date, UUID }
import scala.collection.mutable.{ HashMap, HashSet }
import org.apache.commons.lang3.StringUtils
import dtos.{ ResourceLocationDTO, WordIndexDTO }
import dtos.ResourceContentDTO
import logic.IndexerResource
import dtos.ResourceContentDTO
import scala.collection._
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.ZoneOffset

// TODO コンストラクタでやるのはわかりづらいので、utilityっぽくする
case class IndexerResult(
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

  def generateWordIndexDTOs(walkerName: String): Seq[WordIndexDTO] = {
    contentIds.map { id =>
      val contentId = id._2
      val content = id._1
      content.indices.groupBy(c => c._1).map { f =>
        val word = f._1
        val indice = f._2
          .foldLeft(immutable.Map.newBuilder[Int, Int]) { (map, t) =>
            map += Tuple2(t._2, t._3)
          }.result()

        WordIndexDTO(
          word,
          contentId,
          indice,
          indice.size,
          content.content,
          content.prevContent,
          content.nextContent,
          content.key1,
          content.key2,
          content.key3,
          uri.toString,
          displayLocation,
          name,
          size,
          walkerName,
          indexerClassName,
          Date.from(resourceModified.toInstant(ZoneOffset.UTC)),
          locationId)
      }
    }.flatten.toSeq
  }

  //  private def generateIndexMap: Map[String, Map[UUID, Set[Tuple2[Int, Int]]]] = {
  //    contents.map { c =>
  //      contentIds.get(c) match {
  //        case Some(s) => (s, c.indices)
  //        case None    => throw new IllegalStateException
  //      }
  //    }.flatMap { c =>
  //      c._2.map { i =>
  //        val word = i._1
  //        val contentId = c._1
  //        val indices = (i._2, i._3)
  //        (word, contentId, indices)
  //      }
  //    }.foldLeft(HashMap.empty[String, HashMap[UUID, HashSet[Tuple2[Int, Int]]]]) {
  //      (acc, pair) =>
  //        val word = pair._1
  //        val contentId = pair._2
  //        val indices = pair._3
  //
  //        val contents = acc.getOrElseUpdate(word, HashMap.empty)
  //        val contentIndices = contents.getOrElseUpdate(contentId, HashSet.empty)
  //        contentIndices.add(indices)
  //        acc
  //    }.map { m =>
  //      (m._1, m._2.map {
  //        i => (i._1, i._2.toSet)
  //      }.toMap)
  //    }.toMap
  //  }
}

object IndexerResult {
  def apply(): IndexerResult = IndexerResult(
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
            indexerClassName: String): IndexerResult = IndexerResult(
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

