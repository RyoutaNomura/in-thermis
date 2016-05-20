package controllers

import java.nio.file.Paths

import controllers.action.{ SearchAction, SearchResultOrder }
import logic.ResourceIndexer
import logic.walker.ResourceWalkerConfig
import play.Logger
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller }
import utils.CassandraHelper

class Application extends Controller {

  private val logger = Logger.of(this.getClass)

  def index: Action[AnyContent] = Action {
    Ok(views.html.index())
  }

  def loadInitData: Action[AnyContent] = Action { request =>
    Ok(Json.toJson(SearchResultOrder.values))
  }

  def doSearch(text: String, order: String): Action[AnyContent] = Action { request =>
    val start = System.currentTimeMillis()
    val result = SearchAction.execute(text, SearchResultOrder.valueOf(order))
    logger.info(s"${result.size} result(s) in ${System.currentTimeMillis() - start} ms.");
    Ok(Json.toJson(result))
  }

  def runIndexer: Action[AnyContent] = Action {
    request =>
      val session = CassandraHelper.getSession
      try {
        val config = ResourceWalkerConfig("name",
          Paths.get("/Users/RyoutaNomura/Desktop/odssample").toUri,
          "logic.walker.impl.FileWalker",
          Map.empty)
        ResourceIndexer.generateIndex(session, config)
      } finally {
        session.closeAsync()
      }
      Ok
  }
}
