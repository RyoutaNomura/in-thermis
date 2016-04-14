package logic.walker.impl

import collection.JavaConversions._
import logic.walker.ResourceWalker
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

object FileWalker extends ResourceWalker {
  override def walk(uri: URI, foreach: URI => Unit): Unit = Files.walk(Paths.get(uri)).iterator().map(_.toUri).foreach(foreach)
}