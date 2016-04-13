package logic.walker.impl

import collection.JavaConversions._
import logic.walker.ResourceWalker
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

class FileWalker extends ResourceWalker {
  override def walk(uri: URI, f: => Unit): Unit = Files.walk(Paths.get(uri)).iterator().foreach(_ => f)
}