package jp.co.rn.inthermis.logic.walker.impl

import java.io.{ ByteArrayInputStream, InputStream }
import java.net.URI
import java.time.{ LocalDateTime, OffsetDateTime }

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.apache.commons.lang3.StringUtils

import dispatch.{ Http, as, implyRequestHandlerTuple, url }
import dispatch.Defaults.executor
import jp.co.rn.inthermis.logic.indexer.FileIndexer
import jp.co.rn.inthermis.logic.indexer.impl.WikiTextIndexer
import jp.co.rn.inthermis.logic.walker.{ ResourceWalker, ResourceWalkerConfig }
import jp.co.rn.inthermis.models.IndexerResource
import play.libs.Json

object MediaWikiWalker extends ResourceWalker {
  val props = "PROPS"

  val maxContinue = 10
  val gaplimit = 50
  val requestInterval = 1000
  val defaultProps = Map(
    "format" -> "json",
    "action" -> "query",
    "generator" -> "allpages",
    "gaplimit" -> String.valueOf(gaplimit),
    "gapfilterredir" -> "nonredirects",
    "prop" -> "revisions|info",
    "inprop" -> "url",
    "rvprop" -> "timestamp|content")

  override def walk(config: ResourceWalkerConfig, generateIndex: IndexerResource => Unit): Unit = {
    query(config.uri.toString, Map.empty, maxContinue, 0, generateIndex)
  }

  override val specificFileIndexer: Option[FileIndexer] = Option(WikiTextIndexer)

  def query(uri: String, continueProps: Map[String, String], limit: Long, count: Long, generateIndex: IndexerResource => Unit): Unit = {
    // 限界値を超えたらとめる
    if (count > limit) return

    // APIを連続してAPIを投げ過ぎないようにする
    Thread.sleep(requestInterval)

    val svc = url(uri) <<? defaultProps <<? continueProps
    val future = Http(svc OK as.String)
    val res = Await.result(future, 120.seconds)

    Json.parse(res).get("query").get("pages").foreach { page =>
      val title = page.get("title").asText
      val url = page.get("fullurl").asText

      page.get("revisions").foreach { rev =>
        val timestamp = OffsetDateTime.parse(rev.get("timestamp").asText).toLocalDateTime
        //        println(rev.get("*").asText)
        val content = rev.get("*").asText

        generateIndex(MediaWikiResource(
          URI.create(url),
          url,
          title,
          content.length,
          timestamp,
          timestamp,
          content))
      }
    }

    // 繰り返し書利用のパラメータを取得
    val newProps = Json.parse(res).get("continue").fields
      .foldLeft(Map.newBuilder[String, String]) { (builder, elm) =>
        builder += (elm.getKey -> elm.getValue.asText)
      }
      .result
    // 繰り返し処理が必要な場合は、処理する
    newProps.get("gapcontinue") match {
      case Some(s) if StringUtils.isNoneEmpty(s) => query(uri, newProps, limit, count + 1, generateIndex)
      case _                                     =>
    }
  }
}

case class MediaWikiResource(
  override val uri: URI,
  override val displayLocation: String,
  override val name: String,
  override val size: Long,
  override val created: LocalDateTime,
  override val lastModified: LocalDateTime,
  val content: String)
    extends IndexerResource {

  override def getInputStream: InputStream = {
    new ByteArrayInputStream(content.getBytes("utf-8"))
  }
}
