package jp.co.rn.inthermis.utils

import java.io.InputStream
import java.nio.charset.CodingErrorAction

import scala.io.Codec

import com.ibm.icu.text.CharsetDetector

object CharsetUtils {
  def detectEncoding(is: InputStream): String = {

    var ret = Some(new CharsetDetector().setText(is).detect)
    ret match {
      case Some(s) => s.getName match {
        case "Shift_JIS" => "MS932"
        case _           => s.getName
      }
      case _ => throw new RuntimeException("Cannot detect source charset.")
    }
  }

  def getCodec(is: InputStream): Codec =
    Codec(CharsetUtils.detectEncoding(is))
      .onUnmappableCharacter(CodingErrorAction.IGNORE)
      .onMalformedInput(CodingErrorAction.IGNORE)
}
