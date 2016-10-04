package jp.co.rn.inthermis.utils

import scala.collection.JavaConversions._
import scala.reflect.ClassTag
import scala.reflect.runtime.{ universe => ru }
import ru._
import scala.util.{ Failure, Success }
import com.datastax.driver.core.{ BoundStatement, Cluster, DataType, Metadata, PreparedStatement, ResultSet, Row, Session }
import com.google.common.base.CaseFormat
import play.Logger
import java.lang.Boolean
import com.datastax.driver.core.ResultSetFuture
import jp.co.rn.inthermis.settings.ApplicationConfig

object CassandraHelper {

  private val logger = Logger.of(this.getClass)

  private val runtimeMirror = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  private var cluster: Cluster = _

  var metadata: Metadata = _

  private val stmtCache: scala.collection.mutable.Map[String, PreparedStatement] = scala.collection.mutable.Map.empty

  def open(node: String, port: Int, keyspace: String) {
    cluster = Cluster.builder.addContactPoint(node).withPort(port).build

    metadata = cluster.getMetadata
    logger.info(s"Connected to cluster: ${metadata.getClusterName}");
    metadata.getAllHosts.foreach { host => logger.info(s"Datatacenter: ${host.getDatacenter}; Host: ${host.getAddress}; Rack: ${host.getRack}") }
  }

  def close() {
    cluster.close()
    logger.info(s"CassandraHelper.cluster closed.")
  }

  def getSession: Session = cluster.connect(ApplicationConfig.keyspace)

  def execCql(session: Session, cql: String, params: AnyRef*): ResultSet = {
    try {
      val stmt = this.stmtCache.getOrElseUpdate(cql, session.prepare(cql))
      val bs = new BoundStatement(stmt).bind(params: _*)
      session.execute(bs)
    } catch {
      case t: Throwable => {
        logger.error(s"cql: $cql, params: $params", t)
        throw t
      }
    }
  }

  def execCqlAsync(session: Session, cql: String, params: AnyRef*): ResultSetFuture = {
    try {
      val stmt = this.stmtCache.getOrElseUpdate(cql, session.prepare(cql))
      val bs = new BoundStatement(stmt).bind(params: _*)
      session.executeAsync(bs)

    } catch {
      case t: Throwable => {
        logger.error(s"cql: $cql, params: $params", t)
        throw t
      }
    }
  }

  def getRows[T: TypeTag: ClassTag](session: Session, clazz: Class[T], cql: String, params: AnyRef*): Seq[T] = {
    val cqlResult = execCql(session, cql, params: _*)
    val types = cqlResult.getColumnDefinitions.map { definition => (definition.getName, definition.getType) }.toMap
    cqlResult.map { cqlrow => convert(cqlrow, clazz, types) }.toSeq
  }

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
      case Failure(f) =>
        logger.error("Could not create instance: " + t)
        throw f
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
        val tupleType = ReflectionUtils.toType(tupleClass)
        ReflectionUtils.createInstance(tupleType, values.map(cast): _*).get
      }
      case _ => javaValue
    }
  }
}
