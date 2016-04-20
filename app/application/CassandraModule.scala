package application

import scala.concurrent.Future

import javax.inject.{ Inject, Singleton }
import play.api.{ Configuration, Environment }
import play.api.inject.{ ApplicationLifecycle, Module }
import settings.DBSettings
import utils.CassandraHelper

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