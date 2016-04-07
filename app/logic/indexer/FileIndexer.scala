package logic.indexer

import java.net.URI

import models.IndexerResult

trait FileIndexer {
  def getPriority: Int
  def isTarget(uri: URI): Boolean
  def generateIndex(uri: URI): IndexerResult
  def getKeyTitles: Tuple3[String, String, String]
  def getResourceTypeName: String
}