package logic

import com.datastax.driver.core.Session
import logic.walker.{ ResourceWalkerConfig, ResourceWalkerFactory }
import play.Logger

object ResourceIndexer {

  private val logger = Logger.of(this.getClass)

  def generateIndex(session: Session, config: ResourceWalkerConfig) {
    var walker = ResourceWalkerFactory.create(config)
    walker.walk(session, config)
  }
}
