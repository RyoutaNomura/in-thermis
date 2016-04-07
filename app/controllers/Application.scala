package controllers

import java.net.URI
import java.nio.file.Paths
import scala.collection.JavaConversions._
import daos._
import logic.indexer.ResourceIndexer
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import play.api.mvc.Controller
import settings.DBSettings
import utils.CassandraHelper
import play.libs.Akka
import scala.concurrent.duration._
import play.api.libs.json.Json
import models.SearchResult
import logic.indexer.FileIndexerFactory

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def doSearch(word: String) = Action {

    val start = System.currentTimeMillis

    val helper = CassandraHelper()
    helper.open(DBSettings.host, DBSettings.port, DBSettings.keyspace)

    try {
      val start2 = System.currentTimeMillis

      val wordIndices = WordIndicesDAO(helper).select(word).sortBy { x => x.count }.reverse
//      println(wordIndices)

      val locationIds = wordIndices.map { x => x.resourceLocationId }.toSet
      val locations = ResourceLocationDAO(helper).select(locationIds)

      val contentIds = wordIndices.map { x => x.indices.maxBy(_._2.size)._1 }.toSet
      val contents = ResourceContentDAO(helper).select(contentIds)

      val results = wordIndices.map { x =>
        {
          var resourceContentId = x.indices.maxBy(_._2.size)._1

          val location = locations.get(x.resourceLocationId) match {
            case Some(v) => v
            case None    => throw new RuntimeException(s"no location: ${x.resourceLocationId}")
          }
          val content = contents.get(resourceContentId) match {
            case Some(v) => v
            case None    => throw new RuntimeException(s"no content: $resourceContentId")
          }
          val indices = x.indices.get(resourceContentId) match {
            case Some(v) => v
            case None    => throw new RuntimeException(s"indices not found by resourceContentId:$resourceContentId")
          }
          
          // ファイル以外のリソースも考慮
          val indexer = FileIndexerFactory.create(location.indexerClassName)
          val keyStr = Seq(
            indexer.getKeyTitles._1 + content.key1,
            indexer.getKeyTitles._2 + content.key2,
            indexer.getKeyTitles._3 + content.key3)
            .filter { x => !x.isEmpty }
            .mkString(" / ")
          val resourceTypeName = indexer.getResourceTypeName

          SearchResult(
            word,
            location.uri,
            location.name,
            location.size,
            resourceTypeName,
            location.created,
            location.modified,
            keyStr,
            content.content,
            indices,
            location.indexerClassName,
            location.indexGenerated)
        }
      }.toSeq

//      println(Json.toJson(results))
      println(s"${System.currentTimeMillis() - start2}ms")

      Ok(Json.toJson(results))

    } finally {
      Akka.system.scheduler.scheduleOnce(0.second) {
        helper.close
        println("helper closed.")
      }
      println(s"${System.currentTimeMillis() - start}ms in total")
    }
  }

  def runIndexer = Action {
    request =>
      val helper = CassandraHelper()
      helper.open(DBSettings.host, DBSettings.port, DBSettings.keyspace)
      try {
        ResourceIndexer.createIndex(helper, Paths.get("/Users/RyoutaNomura/Desktop/odssample").toUri)
      } finally {
        helper.close
      }
      Ok
  }
}
