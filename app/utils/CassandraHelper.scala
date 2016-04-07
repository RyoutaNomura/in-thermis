package utils

import collection.JavaConversions._
import scala.collection.JavaConverters._
import java.nio.ByteBuffer
import java.nio.charset.Charset
import scala.reflect.runtime.{ universe => ru }
import ru._
import scala.reflect.ClassTag
import scala.util.{ Try, Success, Failure }
import com.google.common.base.CaseFormat
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Statement
import com.datastax.driver.core.SimpleStatement
import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.Row
import com.datastax.driver.core.DataType
import java.util.Collection
import com.datastax.driver.core.Session
import play.api.libs.concurrent.Akka
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Metadata

object CassandraHelper {
  def apply() = new CassandraHelper
}
class CassandraHelper {

  private val runtimeMirror = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  private var cluster: Cluster = _
  
  private var keyspace = "mykeyspace"
  
  var metadata: Metadata = _
  
  def open(node: String, port: Int, keyspace: String) {
    cluster = Cluster.builder.addContactPoint(node).withPort(port).build
    
    metadata = cluster.getMetadata
    println(s"Connected to cluster: ${metadata.getClusterName}");
    metadata.getAllHosts.foreach { host => println(s"Datatacenter: ${host.getDatacenter}; Host: ${host.getAddress}; Rack: ${host.getRack}") }
  }
  
  def close {
    cluster.close
  }

  def execCql(cql: String, params: AnyRef*) = {
//        println(s"try to execute CQL: $cql with $params" )
    val start = System.currentTimeMillis

    val session = cluster.connect(keyspace)
    try {
      val stmt = session.prepare(cql)
      session.execute(new BoundStatement(stmt).bind(params:_*))
    } catch {
      case t: Throwable => throw t
    } finally {
      session.close
//      println(s"time:${System.currentTimeMillis - start}ms")
    }
  }

//  def execCql(cql: String): ResultSet = {
//    println(s"try to execute CQL: $cql")
//    val start = System.currentTimeMillis
//
//    val session = cluster.connect(keyspace)
//    try {
//      session.execute(new SimpleStatement(cql))
//    } catch {
//      case t: Throwable => throw t
//    } finally {
//      session.close
//      println(s"time:${System.currentTimeMillis - start}ms")
//    }
//  }

    def getRows[T: TypeTag: ClassTag](clazz: Class[T], cql: String, params: AnyRef*): Seq[T] = {
    val seq = Seq.empty
    
//    def wrapper(args: Any*) = execCql(cql, args:_*)
//    val cqlResult = wrapper(params:_*)
    val cqlResult = execCql(cql, params:_*)
    val types = cqlResult.getColumnDefinitions.map { definition => (definition.getName, definition.getType) }.toMap
    cqlResult.map { cqlrow => convert(cqlrow, clazz, types) }.toSeq
  }
    
//  def getRows[T: TypeTag: ClassTag](cql: String, clazz: Class[T]): Seq[T] = {
//    val seq = Seq.empty
//    val cqlResult = execCql(cql)
//    val types = cqlResult.getColumnDefinitions.map { definition => (definition.getName, definition.getType) }.toMap
//    cqlResult.map { cqlrow => convert(cqlrow, clazz, types) }.toSeq
//  }

  private def convert[T: TypeTag: ClassTag](row: Row, c: Class[T], types: Map[String, DataType]): T = {
    val t = ru.typeOf[T]
    ReflectionUtils.createInstance(t) match {
      case Success(instance: T) => {
        types.foreach(f => {
          val dbName = f._1
          val fieldName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, dbName)
          ReflectionUtils.setValueToInstance(instance, fieldName, Option(cast(row.getObject(dbName))))
        })
        instance
      }
      case Failure(f) => throw new RuntimeException("Illegal Type: " + f)
    }
  }

  private def cast(javaValue: Any): Any = {
    javaValue match {
      case null                       => null
      case m: java.util.Map[_, _]     => m.map { f => (cast(f._1), cast(f._2)) }.toMap
      case l: java.util.List[_]       => l.map { cast }.toSeq
      case s: java.util.Set[_]        => s.map { cast }.toSet
      case c: java.util.Collection[_] => c.map { cast }
      case t: com.datastax.driver.core.TupleValue => {
        var values = Seq[Int]()
        try {
          for (i <- 0 to 100) { values = values :+ t.get(i, classOf[Int]) }
        } catch {
          case t: Throwable => //t.printStackTrace()
        }
        val tupleClass = Class.forName("scala.Tuple" + values.size)
        val tupleType = runtimeMirror.classSymbol(tupleClass).toType
        ReflectionUtils.createInstance(tupleType, values.map(cast):_*).get
      }
      case _ => javaValue
    }
  }
}