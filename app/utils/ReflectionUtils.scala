package utils

import scala.reflect.runtime.{ universe => ru }
import ru._

import scala.util.{ Try, Success, Failure }
import scala.reflect.ClassTag

object ReflectionUtils {

  private val runtimeMirror = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  def getObjectInstance[T: TypeTag](t: Type):T = {
    val moduleSymbol = runtimeMirror.staticModule(t.typeSymbol.fullName)
    val module = runtimeMirror.reflectModule(moduleSymbol)
    module.instance.asInstanceOf[T]
  }

  def createInstance[T: TypeTag](t: Type): Try[T] = Try {
    val classMirror = runtimeMirror.reflectClass(t.typeSymbol.asClass)
    val constructor = t.member(ru.termNames.CONSTRUCTOR).filter {
      _.asMethod.paramLists match {
        case List(Nil) => true
        case _         => false
      }
    }.asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor)
    constructorMirror().asInstanceOf[T]
  }

  def createInstance[T: TypeTag](t: Type, args: Any*): Try[T] = Try {
    val classMirror = runtimeMirror.reflectClass(t.typeSymbol.asClass)
    val constructor = t.member(ru.termNames.CONSTRUCTOR).filter {
      _.asMethod.paramLists match {
        case List(Nil) => false
        case _         => true
      }
    }.asMethod
    def wrapper(args: Any*) = classMirror.reflectConstructor(constructor)(args: _*)
    wrapper(args: _*).asInstanceOf[T]
  }

  def setValueToInstance[T: TypeTag: ClassTag](instance: T, name: String, value: Option[Any]) {
    value match {
      case Some(v) =>
        val t = ru.typeOf[T]
        val instanceMirror = runtimeMirror.reflect(instance)

        val symbol = t.decl(ru.TermName(name))
        if (symbol.isTerm) {
          val termSymbol = symbol.asTerm
          val fieldMirror = instanceMirror.reflectField(termSymbol)
          fieldMirror.set(v)
        } else {
          throw new RuntimeException(s"$name is not term of ${t.toString()}")
        }
      case None =>
    }
  }

  def toType(clazz: Class[_]): Type = {
    runtimeMirror.classSymbol(clazz).toType
  }
}