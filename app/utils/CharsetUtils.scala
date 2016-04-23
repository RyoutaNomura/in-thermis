package utils

import java.net.URI
import java.nio.charset.CodingErrorAction

import scala.io.Codec

import com.ibm.icu.text.CharsetDetector

object CharsetUtils {
  def detectEncoding(uri: URI): String = {
    val is = uri.toURL.openStream

    try {
      var ret = Some(new CharsetDetector().setText(is).detect)
      ret match {
        case Some(s) => s.getName match {
          case "Shift_JIS" => "MS932"
          case _           => s.getName
        }
        case _ => throw new RuntimeException("Cannot detect source charset.")
      }
    } finally {
      is.close()
    }
  }

  def getCodec(uri: URI): Codec =
    Codec(CharsetUtils.detectEncoding(uri))
      .onUnmappableCharacter(CodingErrorAction.IGNORE)
      .onMalformedInput(CodingErrorAction.IGNORE)

}