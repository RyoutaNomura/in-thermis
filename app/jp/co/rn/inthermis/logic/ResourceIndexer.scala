package jp.co.rn.inthermis.logic

import com.datastax.driver.core.Session
import jp.co.rn.inthermis.logic.walker.{ ResourceWalkerConfig, ResourceWalkerFactory }
import play.Logger
import jp.co.rn.inthermis.daos.ResourceLocationDAO

object ResourceIndexer {

  private val logger = Logger.of(this.getClass)

  def generateIndex(session: Session, config: ResourceWalkerConfig) {
    ResourceWalkerFactory.create(config).walk(session, config)
  }

  def deleteAllIndex(session: Session, config: ResourceWalkerConfig) {
    ResourceWalkerFactory.create(config).deleteAll(session, config)
  }
}
