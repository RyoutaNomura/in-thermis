package utils

import org.apache.commons.lang3.StringUtils

import scala.reflect.runtime.{ universe => ru }
import ru._

trait EnumClass {
  def getKey: String
}

abstract class EnumObject[A <: EnumClass: TypeTag] {
  val values: Seq[A] = {
    val typeSymbol = ReflectionUtils.toType(this.getClass).typeSymbol.asType
    ReflectionUtils.getNestedObjects(typeSymbol)
  }

  def valueOf(str: String): A = {
    values.find { t =>
      StringUtils.equals(t.getKey, str)
    }.getOrElse(throw new RuntimeException(s"no such enum ${str}")).asInstanceOf[A]
  }
}
