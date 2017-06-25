package app

import com.twilio.sdk.verbs._

object Twilio {

  // https://www.twilio.com/docs/api/twiml/sms/twilio_request
  case class TwilioRequest(
    MessageSid: Option[String] = None,
    SmsSid: Option[String] = None,
    AccountSid: Option[String] = None,
    MessagingServiceSid: Option[String] = None,
    From: String,
    To: String,
    Body: String,
    NumMedia: Option[String] = None,
    MediaContentType: Option[String] = None,
    MediaUrl: Option[String] = None,
    MediaUrl0: Option[String] = None,
    MediaUrl1: Option[String] = None,
    MediaUrl2: Option[String] = None,
    MediaUrl3: Option[String] = None,
    FromCity: Option[String] = None,
    FromState: Option[String] = None,
    FromZip: Option[String] = None,
    ToCity: Option[String] = None,
    ToState: Option[String] = None,
    ToZip: Option[String] = None,
    ToCountry: Option[String] = None
  )

  def messageTwiml(msg: String): String = {
    val r = new TwiMLResponse()
    r.append(new Message(msg))
    r.toEscapedXML
  }
}
