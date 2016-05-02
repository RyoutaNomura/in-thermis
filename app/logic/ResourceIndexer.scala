package logic

import com.datastax.driver.core.Session

import logic.walker.{ ResourceWalkerConfig, ResourceWalkerFactory }

object ResourceIndexer {

  def generateIndex(session: Session, config: ResourceWalkerConfig) {
    var walker = ResourceWalkerFactory.create(config)
    walker.walk(session, config)
  }
}
