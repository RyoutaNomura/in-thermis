package application

import scala.concurrent.Future

import javax.inject.{ Inject, Singleton }
import play.Logger
import play.api.{ Configuration, Environment }
import play.api.inject.{ ApplicationLifecycle, Binding, Module }
import settings.DBSettings
import utils.CassandraHelper

class CassandraModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(bind[CassandraTask].toSelf.eagerly)
  }
}

@Singleton
class CassandraTask @Inject() (lifecycle: ApplicationLifecycle) {

  private val logger = Logger.of(this.getClass)

  lifecycle.addStopHook(() => Future.successful {
    CassandraHelper.close()

    logger.info("Application disconnected from Cassandra cluster.")
  })

  CassandraHelper.open(DBSettings.host, DBSettings.port, DBSettings.keyspace)
  logger.info("Application connected to Cassandra cluster.")
}
