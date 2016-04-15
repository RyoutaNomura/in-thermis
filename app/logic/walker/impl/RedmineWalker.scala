package logic.walker.impl

import collection.JavaConversions._
import logic.walker.ResourceWalker
import java.net.URI
import com.taskadapter.redmineapi.RedmineManager
import com.taskadapter.redmineapi.RedmineManagerFactory
import settings.RedmineSettings

object RedmineWalker extends ResourceWalker {
  override def walk(uri: URI, foreach: URI => Unit): Unit = {
    val mgr = RedmineManagerFactory.createWithApiKey(RedmineSettings.uri, RedmineSettings.apiAccessKey);


  }
}