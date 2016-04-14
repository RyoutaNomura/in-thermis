package logic.walker

import java.net.URI
import logic.walker.impl.FileWalker

object ResourceWalkerFactory {
  def create(uri: URI): ResourceWalker = {
    uri.getScheme match {
      case "file" => FileWalker
      case _      => throw new RuntimeException("Only file scheme allowed.")
    }
  }
}