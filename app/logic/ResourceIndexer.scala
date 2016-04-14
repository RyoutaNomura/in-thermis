package logic

import collection.JavaConversions._
import java.net.URI
import com.datastax.driver.core.Session
import logic.walker.ResourceWalkerFactory

object ResourceIndexer {

  def generateIndex(session: Session, uri: URI) {
    ResourceWalkerFactory.create(uri).walk(session, uri)
  }
}