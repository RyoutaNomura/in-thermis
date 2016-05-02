package logic.walker

import logic.walker.impl.FileWalker

object ResourceWalkerFactory {

  def create(config: ResourceWalkerConfig): ResourceWalker = {
    config.uri.getScheme match {
      case "file" => FileWalker
      case _      => throw new RuntimeException("Only file scheme allowed.")
    }
  }
}
