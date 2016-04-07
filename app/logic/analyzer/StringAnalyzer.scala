package logic.analyzer

import scala.collection.JavaConversions._
import org.apache.commons.lang3.StringUtils
import net.java.sen.SenFactory
import scala.collection.mutable.Buffer
import java.util.ArrayList
import com.google.common.collect.Lists
import scala.collection.mutable.ArrayBuffer
import net.java.sen.dictionary.Token

case class AnalyzeResult(
  word: String,
  start: Int,
  length: Int)

object StringAnalyzer {
  val reuseList = new ArrayList[Token]
  val dictDir = StringUtils.EMPTY

  def analyze(text: String): Seq[AnalyzeResult] = {
    if (text.isEmpty) {
      return Seq.empty
    }

    val tagger = SenFactory.getStringTagger(dictDir)
    
    tagger.analyze(text, reuseList)
      .filter(x => StringUtils.startsWithAny(x.getMorpheme.getPartOfSpeech, "名詞", "動詞"))
      .map{x => 
        x.getMorpheme.getBasicForm match {
          case "*" => AnalyzeResult(x.toString, x.getStart, x.getLength)
          case _   => AnalyzeResult(x.getMorpheme.getBasicForm, x.getStart, x.getLength)
        }
      }.toSeq
  }
}