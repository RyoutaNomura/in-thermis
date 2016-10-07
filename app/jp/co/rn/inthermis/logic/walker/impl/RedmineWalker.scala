package jp.co.rn.inthermis.logic.walker.impl

import com.taskadapter.redmineapi.RedmineManagerFactory
import jp.co.rn.inthermis.models.IndexerResource
import jp.co.rn.inthermis.logic.walker.{ ResourceWalker, ResourceWalkerConfig }
import java.net.URI
import java.time.LocalDateTime
import java.io.InputStream
import java.io.ByteArrayInputStream

object RedmineWalker extends ResourceWalker {

  val host = "https://www.hostedredmine.com";
  val apiAccessKey = "a3221bfcef5750219bd0a2df69519416dba17fc9";
  val projectKey = "taskconnector-test";

  override def walk(config: ResourceWalkerConfig, generateIndex: IndexerResource => Unit): Unit = {
    // プロジェクト内のチケット全取得
    val manager = RedmineManagerFactory.createWithApiKey(host, apiAccessKey)
    val issueManager = manager.getIssueManager
    // TODO 前回実行日より後に更新されたチケットのみ取得
    val issues = issueManager.getIssues(projectKey, null)
  }
}

case class RedmineResource(
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

