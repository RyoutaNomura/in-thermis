package jp.co.rn.inthermis.logic.indexer

import java.net.URI
import scala.annotation.migration
import scala.collection.JavaConversions._
import scala.reflect.runtime.universe
import org.apache.commons.lang3.StringUtils
import com.google.common.reflect.ClassPath
import jp.co.rn.inthermis.logic.indexer.impl.NullIndexer
import jp.co.rn.inthermis.utils.ReflectionUtils
import play.Logger

object FileIndexerFactory {

  private val logger = Logger.of(this.getClass)

  private val implPackage = "jp.co.rn.inthermis.logic.indexer.impl"

  val cache: Seq[FileIndexer] = {
    ClassPath
      .from(this.getClass.getClassLoader)
      .getTopLevelClasses(implPackage)
      .map { classInfo =>
        val typ = ReflectionUtils.toType(classInfo.load)
        ReflectionUtils.getObjectInstance(typ).asInstanceOf[FileIndexer]
      }.toSeq
      .sortBy { _.getPriority }
      .reverse
  }

  cache.foreach { x => logger.info(s"Indexer loaded: ${x.getClassName}") }

  def create(className: String): FileIndexer = {
    cache.find { x => StringUtils.equals(x.getClassName, className) } match {
      case Some(s) => s
      case None    => NullIndexer
    }
  }

  def create(uri: URI): FileIndexer = {
    cache.find { x => x.isTarget(uri) } match {
      case Some(indexer) => indexer
      case None          => NullIndexer
    }
  }
}
