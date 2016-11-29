package jp.co.rn.inthermis.modules

import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor.{ ActorSystem, Props }
import javax.inject.{ Inject, Singleton }
import jp.co.rn.inthermis.actor.IndexerActor
import play.api.{ Configuration, Environment }
import play.api.inject.{ ApplicationLifecycle, Binding, Module }
import org.apache.commons.lang3.StringUtils

class IndexerModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(bind[IndexerTask].toSelf.eagerly)
  }
}

@Singleton
class IndexerTask @Inject() (lifecycle: ApplicationLifecycle) {

  val system = ActorSystem("system")
  val actor = system.actorOf(Props[IndexerActor], "indexer-actor")
  //  val cancellable = system.scheduler.schedule(
  //    30.second, 10.minute, actor, StringUtils.EMPTY)
}
