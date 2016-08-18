package utils

import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe => ru }
import ru._
import scala.util.Try

object ReflectionUtils {

  private val rm = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  def getObjectModuleMirror[A: TypeTag](t: Type): ModuleMirror = {
    val typeName = t.typeSymbol.fullName
    val moduleSymbol = rm.staticModule(typeName)
    rm.reflectModule(moduleSymbol)
  }

  def getObjectInstance[A: TypeTag](t: Type): A = {
    val module = getObjectModuleMirror(t)
    val instance = module.instance
    instance.asInstanceOf[A]
  }

  def createInstance[A: TypeTag](t: Type): Try[A] = Try {
    // もう少し書きようが有る気がする
    t.member(ru.termNames.CONSTRUCTOR).alternatives
      .find(_.asMethod.paramLists match {
        case List(Nil) => true
        case _         => false
      }) match {
        case Some(s) =>
          val classMirror = rm.reflectClass(t.typeSymbol.asClass)
          val constructorMirror = classMirror.reflectConstructor(s.asMethod)
          constructorMirror().asInstanceOf[A]
        case None =>
          val mm = getObjectModuleMirror(t)
          mm.symbol.info.decl(ru.TermName("apply")).alternatives
            .find(_.asMethod.paramLists match {
              case List(Nil) => true
              case _         => false
            }) match {
              case Some(s) =>
                val c = rm.reflect(mm.instance)
                val m = c.reflectMethod(s.asMethod)
                m.apply().asInstanceOf[A]
              case None => throw new RuntimeException("no simple constructor or apply method: " + t.member(ru.termNames.CONSTRUCTOR))
            }
      }
  }

  def createInstance[A: TypeTag](t: Type, args: Any*): Try[A] = Try {
    //    val classMirror = rm.reflectClass(t.typeSymbol.asClass)
    //    val constructor = t.member(ru.termNames.CONSTRUCTOR).filter {
    //      _.asMethod.paramLists match {
    //        case List(Nil) => false
    //        case _         => true
    //      }
    //    }.asMethod
    //    def wrapper(args: Any*) = classMirror.reflectConstructor(constructor)(args: _*)
    //    wrapper(args: _*).asInstanceOf[A]

    // もう少し書きようが有る気がする
    t.member(ru.termNames.CONSTRUCTOR).alternatives
      .find(_.asMethod.paramLists match {
        case List(Nil) => true
        case _         => false
      }) match {
        case Some(s) =>
          val classMirror = rm.reflectClass(t.typeSymbol.asClass)
          def wrapper(args: Any*) = classMirror.reflectConstructor(s.asMethod)(args: _*)
          wrapper(args: _*).asInstanceOf[A]
        case None =>
          val mm = getObjectModuleMirror(t)
          mm.symbol.info.decl(ru.TermName("apply")).alternatives
            .find(_.asMethod.paramLists match {
              case List(Nil) => true
              case _         => false
            }) match {
              case Some(s) =>
                val c = rm.reflect(mm.instance)
                val m = c.reflectMethod(s.asMethod)
                m.apply(args: _*).asInstanceOf[A]
              case None => throw new RuntimeException("no simple constructor or apply method: " + t.member(ru.termNames.CONSTRUCTOR))
            }
      }
  }

  def setValueToInstance[A: TypeTag: ClassTag](instance: A, name: String, value: Option[Any]) {
    value match {
      case Some(v) =>
        val t = ru.typeOf[A]
        val instanceMirror = rm.reflect(instance)

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
    rm.classSymbol(clazz).toType
  }

  def getNestedObjects[A: TypeTag](typeSymbol: TypeSymbol): Seq[A] = {
    typeSymbol.toType.decls.collect {
      case s if s.isModule =>
        rm.reflectModule(s.asModule).instance.asInstanceOf[A]
    }.toSeq
  }
}
