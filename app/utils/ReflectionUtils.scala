package utils

import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe => ru }
import ru._
import scala.util.Try

object ReflectionUtils {

  private val runtimeMirror = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  def getObjectInstance[A: TypeTag](t: Type): A = {
    val typeName = t.typeSymbol.fullName
    val moduleSymbol = runtimeMirror.staticModule(typeName)
    val module = runtimeMirror.reflectModule(moduleSymbol)
    val instance = module.instance
    instance.asInstanceOf[A]
  }

  def createInstance[A: TypeTag](t: Type): Try[A] = Try {
    val constructor = t.member(ru.termNames.CONSTRUCTOR).filter {
      _.asMethod.paramLists match {
        case List(Nil) => true
        case _         => false
      }
    }.asMethod
    val classMirror = runtimeMirror.reflectClass(t.typeSymbol.asClass)
    val constructorMirror = classMirror.reflectConstructor(constructor)
    constructorMirror().asInstanceOf[A]
  }

  def createInstance[A: TypeTag](t: Type, args: Any*): Try[A] = Try {
    val classMirror = runtimeMirror.reflectClass(t.typeSymbol.asClass)
    val constructor = t.member(ru.termNames.CONSTRUCTOR).filter {
      _.asMethod.paramLists match {
        case List(Nil) => false
        case _         => true
      }
    }.asMethod
    def wrapper(args: Any*) = classMirror.reflectConstructor(constructor)(args: _*)
    wrapper(args: _*).asInstanceOf[A]
  }

  def setValueToInstance[A: TypeTag: ClassTag](instance: A, name: String, value: Option[Any]) {
    value match {
      case Some(v) =>
        val t = ru.typeOf[A]
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