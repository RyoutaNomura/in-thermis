package dtos

import org.apache.commons.lang3.StringUtils
import com.datastax.driver.core.TupleValue
import com.datastax.driver.core.Metadata
import com.datastax.driver.core.DataType
import com.datastax.driver.core.TupleType
import java.util.UUID
import java.util.HashMap

case class WordIndicesDTO(
    var resourceLocationId: UUID,
    var word: String,
    var count: Long,
    var indices: Map[UUID, Set[Tuple2[Int, Int]]]) {

  def this() = this(
    UUID.randomUUID,
    StringUtils.EMPTY,
    -1,
    Map.empty)

  //  def findTargetContentId(): Option[UUID] = {
  //    indices.size match {
  //      case 0 => None
  //      case _ => Some(indices.maxBy(_._2.size)._1)
  //    }
  //  }

  //  def genereateIndices(meta: Metadata): java.util.Map[String, java.util.Set[TupleValue]] = {
  //    val tupleType = meta.newTupleType(DataType.cint, DataType.cint);
  //    indices.map{f => 
  //      val key = f._1
  //      val value:java.util.Set[TupleValue] = f._2.map{t => tupleType.newValue(Int.box(t._1), Int.box(t._2))}
  //      (key, value)
  //    }
  //  }
}