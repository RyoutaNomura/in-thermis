package logic.walker.impl

import com.taskadapter.redmineapi.RedmineManagerFactory

import logic.IndexerResource
import logic.walker.{ ResourceWalker, ResourceWalkerConfig }

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
