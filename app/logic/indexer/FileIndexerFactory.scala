package logic.indexer

import java.net.URI

import scala.collection.JavaConversions._
import scala.collection.mutable.LinkedHashMap
import scala.reflect.runtime.universe

import com.google.common.reflect.ClassPath

import logic.indexer.impl.NullIndexer
import utils.ReflectionUtils

object FileIndexerFactory {

  private val IMPL_PKG = "logic.indexer.impl"

  private val RUNTIME_MIRROR = universe.runtimeMirror(Thread.currentThread.getContextClassLoader)

  private val CACHE: LinkedHashMap[String, FileIndexer] = {
    ClassPath.from(Thread.currentThread.getContextClassLoader).getTopLevelClasses(IMPL_PKG)
      .map { classInfo => classInfo.load }
      .map { clazz => ReflectionUtils.toType(clazz) }
      .map { t => (t.typeSymbol.fullName, ReflectionUtils.getObjectInstance(t).asInstanceOf[FileIndexer]) }
      .toSeq
      .sortBy { f => f._2.getPriority }
      .reverse
      .foldLeft(LinkedHashMap[String, FileIndexer]()) {
        (acc, elm) =>
          acc.update(elm._1, elm._2)
          println(s"Indexer loaded: " + elm._1)
          acc
      }
  }

  def create(className: String): FileIndexer = {
    CACHE.get(className) match {
      case Some(s) => s
      case None    => NullIndexer
    }
  }

  def create(uri: URI): FileIndexer = {
    CACHE.values.filter { f => f.isTarget(uri) }.headOption match {
      case Some(indexer) =>
        println(s"Indexer selected: $indexer")
        indexer
      case None =>
        println(s"Indexer selected: NullIndexer")
        NullIndexer
    }

  }
}