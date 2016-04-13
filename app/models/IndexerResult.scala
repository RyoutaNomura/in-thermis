package models

import java.net.URI
import java.util.Date
import java.util.UUID
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import dtos.ResourceContentDTO
import dtos.ResourceLocationDTO
import dtos.WordIndicesDTO
import org.apache.commons.lang3.StringUtils

case class IndexerResult(
    uri: URI,
    name: String,
    size: Long,
    resourceCreated: Date,
    resourceModified: Date,
    contents: Seq[Content],
    indexerClassName: String,
    indexerGenerated: Date) {

  private val locationId = UUID.randomUUID
  private val contentIds = contents.map { c => (c, UUID.randomUUID) }.toMap

  val locationDTO = ResourceLocationDTO(
    locationId,
    uri.toString,
    name,
    size,
    resourceCreated,
    resourceModified,
    indexerClassName,
    indexerGenerated)

  val contentDTOs = contentIds.map { f =>
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

  val wordIndicesDTOs = {
    generateIndexMap.map { i =>
      val word = i._1
      val count = i._2.values.foldLeft(0)((acc,i) => acc + i.size)
      val indices = i._2.map { idx => (idx._1, idx._2.toSet) }.toMap
      WordIndicesDTO(
        locationId,
        word,
        count,
        indices)
    }.toSeq
  }

  private def generateIndexMap: Map[String, Map[UUID, Set[Tuple2[Int, Int]]]] = {
    contents.map { c =>
      contentIds.get(c) match {
        case Some(s) => (s, c.indices)
        case None    => throw new IllegalStateException
      }
    }.flatMap { c =>
      c._2.map { i =>
        val word = i._1
        val contentId = c._1
        val indices = (i._2, i._3)
        (word, contentId, indices)
      }
    }.foldLeft(HashMap.empty[String, HashMap[UUID, HashSet[Tuple2[Int, Int]]]]) {
      (acc, pair) =>
        val word = pair._1
        val contentId = pair._2
        val indices = pair._3
        
        val contents = acc.getOrElseUpdate(word, HashMap.empty)
        val contentIndices = contents.getOrElseUpdate(contentId, HashSet.empty)
        contentIndices.add(indices)
        acc
    }.map { m =>
      (m._1, m._2.map {
        i => (i._1, i._2.toSet)
      }.toMap)
    }.toMap
  }
}

object IndexerResult {
  def apply(): IndexerResult = {
    IndexerResult(
      URI.create(StringUtils.EMPTY),
      StringUtils.EMPTY,
      -1,
      new Date,
      new Date,
      Seq.empty,
      StringUtils.EMPTY,
      new Date)
  }
}

case class Content(
    var key1: String,
    var key2: String,
    var key3: String,
    var content: String,
    var prevContent: String,
    var nextContent: String,
    // word, start, length
//    indices: Map[String, Set[Tuple2[Int, Int]]])
    indices: Seq[Tuple3[String, Int, Int]]
)

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
 
