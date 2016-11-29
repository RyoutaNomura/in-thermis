package jp.co.rn.inthermis.controllers.action.loadInitData

import jp.co.rn.inthermis.enums.{ DateRangeCriteria, SearchResultOrder }
import jp.co.rn.inthermis.logic.indexer.FileIndexerFactory
import jp.co.rn.inthermis.settings.ApplicationConfig
import jp.co.rn.inthermis.utils.EnumClass
import play.Logger

object LoadInitDataAction {

  val logger = Logger.of(this.getClass)

  def execute: LoadInitDataResponse = {
    val searchResultOrder = SearchResultOrder.values
      .map { x =>
        Map(
          "key" -> x.getKey,
          "displayName" -> x.displayName)
      }
      .toSeq

    val dateRangeCriteria = DateRangeCriteria.values
      .map { x =>
        Map(
          "key" -> x.getKey,
          "displayName" -> x.displayName)
      }
      .toSeq

    val resourceWalkers = ApplicationConfig.resourceWalkerConfigs
      .map { x =>
        Map(
          "key" -> x.id.toString,
          "displayName" -> x.name)
      }
      .toSeq

    val resourceIndexers = FileIndexerFactory.getCache
      .filter { x => x.isShowAsCriteria }
      .map { x =>
        Map(
          "key" -> x.getClassName,
          "displayName" -> x.getResourceTypeName,
          "iconCssClassName" -> x.getIconCssClassName)
      }
      .toSeq
      .sortBy(_.get("displayName"))

    LoadInitDataResponse(searchResultOrder, dateRangeCriteria, resourceWalkers, resourceIndexers)
  }
}
