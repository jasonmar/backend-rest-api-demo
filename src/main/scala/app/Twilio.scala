package app

import com.twilio.sdk.verbs._

object Twilio {
  def respondWithMessage(msg: String): String = {
    val r = new TwiMLResponse()
    r.append(new Message(msg))
    r.toEscapedXML
  }
}
