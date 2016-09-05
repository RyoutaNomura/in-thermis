package controllers

import java.nio.file.Paths
import controllers.action.search.SearchAction
import enums.SearchResultOrder
import logic.ResourceIndexer
import logic.walker.ResourceWalkerConfig
import play.Logger
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller }
import utils.CassandraHelper
import controllers.action.loadInitData.LoadInitDataAction
import settings.ApplicationConfig
import org.apache.commons.lang3.StringUtils
import controllers.action.search.SearchCriteria

class Application extends Controller {

  private val logger = Logger.of(this.getClass)

  def index: Action[AnyContent] = Action {
    Ok(views.html.index())
  }

  def loadInitData: Action[AnyContent] = Action { request =>
    Ok(Json.toJson(LoadInitDataAction.execute))
  }

  def doSearch: Action[AnyContent] = Action { request =>

    val start = System.currentTimeMillis()

    val criteria = SearchCriteria(request)
    val result = SearchAction.execute(criteria)

    logger.info(s"${result.searchResults.size} result(s) in ${System.currentTimeMillis() - start} ms. criteria: ${criteria}");

    Ok(Json.toJson(result))
  }

  def runIndexer: Action[AnyContent] = Action {
    request =>
      val session = CassandraHelper.getSession
      try {
        ApplicationConfig.resourceWalkerConfigs.foreach { x =>
          ResourceIndexer.generateIndex(session, x)
        }
      } finally {
        session.closeAsync()
      }
      Ok
  }

  def deleteAllIndex: Action[AnyContent] = Action {
    request =>
      val session = CassandraHelper.getSession
      try {
        ApplicationConfig.resourceWalkerConfigs.foreach { x =>
          ResourceIndexer.deleteAllIndex(session, x)
        }
      } finally {
        session.closeAsync()
      }
      Ok
  }
}
