package logic.walker

import scala.annotation.migration
import scala.collection.JavaConversions._
import scala.reflect.runtime.{ universe => ru }
import ru._
import com.google.common.reflect.ClassPath
import play.Logger
import utils.ReflectionUtils

object ResourceWalkerFactory {

  private val logger = Logger.of(this.getClass)

  private val implPackage = "logic.walker.impl"
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

  def create(config: ResourceWalkerConfig): ResourceWalker = {
    cache.get(config.implClassName) match {
      case Some(s) => s
      case None    => throw new RuntimeException(s"${config.implClassName} not found")
    }
  }
}
