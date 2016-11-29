package jp.co.rn.inthermis.actor

import akka.actor.Actor
import jp.co.rn.inthermis.logic.ResourceIndexer
import jp.co.rn.inthermis.settings.ApplicationConfig
import jp.co.rn.inthermis.utils.CassandraHelper
import play.Logger

class IndexerActor extends Actor {

  private val logger = Logger.of(this.getClass)

  override def receive: Actor.Receive = {
    case message: String => execute
  }

  /** 処理本体 */
  def execute(): Unit = {
    val session = CassandraHelper.getSession
    try {
      ApplicationConfig.resourceWalkerConfigs.foreach { x =>
        ResourceIndexer.generateIndex(session, x)
      }
    } finally {
      session.closeAsync()
    }
  }
}
