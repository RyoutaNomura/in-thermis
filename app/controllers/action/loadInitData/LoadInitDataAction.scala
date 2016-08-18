package controllers.action.loadInitData

import enums.{ DateRangeCriteria, SearchResultOrder }
import logic.indexer.FileIndexerFactory
import settings.ApplicationConfig
import utils.EnumClass

object LoadInitDataAction {
  def execute: LoadInitDataResponse = {
    val searchResultOrder = SearchResultOrder.values.map { x => Map("key" -> x.getKey, "displayName" -> x.displayName) }.toSeq
    val dateRangeCriteria = DateRangeCriteria.values.map { x => Map("key" -> x.getKey, "displayName" -> x.displayName) }.toSeq
    val resourceWalkers = ApplicationConfig.resourceWalkerConfigs.map { x => Map("key" -> x.id.toString, "displayName" -> x.name) }.toSeq
    val resourceIndices = FileIndexerFactory.cache.map { x => Map("key" -> x.getClassName, "displayName" -> x.getResourceTypeName) }.toSeq.sortBy(_.get("displayName"))

    LoadInitDataResponse(searchResultOrder, dateRangeCriteria, resourceWalkers, resourceIndices)
  }
}
