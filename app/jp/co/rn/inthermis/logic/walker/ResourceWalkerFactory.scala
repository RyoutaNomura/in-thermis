package jp.co.rn.inthermis.logic.walker

import scala.annotation.migration
import scala.collection.JavaConversions._
import scala.reflect.runtime.universe

import com.google.common.reflect.ClassPath

import jp.co.rn.inthermis.utils.ReflectionUtils
import play.Logger

object ResourceWalkerFactory {

  private val logger = Logger.of(this.getClass)

  private val implPackage = "jp.co.rn.inthermis.logic.walker.impl"
  private val cache: Map[String, ResourceWalker] = {
    ClassPath
      .from(this.getClass.getClassLoader)
      .getTopLevelClasses(implPackage)
      .map(c => ReflectionUtils.toType(c.load))
      .filter(t => ReflectionUtils.getObjectInstance(t).isInstanceOf[ResourceWalker])
      .map { t =>
        val walker = ReflectionUtils.getObjectInstance(t).asInstanceOf[ResourceWalker]
        (t.typeSymbol.fullName, walker)
      }.toMap
  }
  cache.foreach { t => logger.info(s"walker loaded: ${t._2.walkerName}") }

  def create(implClassName: String): ResourceWalker = {
    cache.get(implClassName) match {
      case Some(s) => s
      case None    => throw new RuntimeException(s"${implClassName} not found")
    }
  }

  def create(config: ResourceWalkerConfig): ResourceWalker = create(config.implClassName)
}
