package logic.walker.impl

import java.net.URI

import logic.indexer.entity.IndexerResource
import logic.walker.ResourceWalker

object RedmineWalker extends ResourceWalker {
  override def walk(uri: URI, generateIndex: IndexerResource => Unit): Unit = {
  }
}