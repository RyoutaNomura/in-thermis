package application

import play.api._
import javax.inject._
import play.api.inject._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import utils.CassandraHelper
import settings.DBSettings

class CassandraModule extends Module {
  def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[CassandraTask].toSelf.eagerly)
  }
}

@Singleton
class CassandraTask @Inject() (lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook(() => Future.successful {
    CassandraHelper.close()
    println("Application disconnected from Cassandra cluster.")
  })

  CassandraHelper.open(DBSettings.host, DBSettings.port, DBSettings.keyspace)
  println("Application connected to Cassandra cluster.")

}