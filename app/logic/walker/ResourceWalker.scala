package logic.walker

import java.net.URI

trait ResourceWalker {
  def walk(uri: URI, f: => Unit): Unit
}