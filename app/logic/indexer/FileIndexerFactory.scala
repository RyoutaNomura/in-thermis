package logic.indexer

import java.net.URI

import scala.annotation.migration
import scala.collection.JavaConversions._
import scala.reflect.runtime.universe

import org.apache.commons.lang3.StringUtils

import com.google.common.reflect.ClassPath

import logic.indexer.impl.NullIndexer
import utils.ReflectionUtils

object FileIndexerFactory {

  private val IMPL_PKG = "logic.indexer.impl"
  private val CACHE: Seq[FileIndexer] = {

    ClassPath
      .from(this.getClass.getClassLoader)
      .getTopLevelClasses(IMPL_PKG)
      .map { classInfo =>
        val typ = ReflectionUtils.toType(classInfo.load)
        ReflectionUtils.getObjectInstance(typ).asInstanceOf[FileIndexer]
      }.toSeq
      .sortBy { _.getPriority }
      .reverse
  }

  CACHE.foreach { x => println(s"Indexer loaded: ${x.getClassName}") }

  def create(className: String): FileIndexer = {
    CACHE.find { x => StringUtils.equals(x.getClassName, className) } match {
      case Some(s) => s
      case None    => NullIndexer
    }
  }

  def create(uri: URI): FileIndexer = {
    CACHE.find { x => x.isTarget(uri) } match {
      case Some(indexer) => indexer
      case None          => NullIndexer
    }
  }
}