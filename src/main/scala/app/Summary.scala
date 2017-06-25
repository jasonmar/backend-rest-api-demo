package app

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

object Summary {

  val SMMRY_API_KEY: String = sys.env.getOrElse("SMMRY_API_KEY", "")
  val BASE_URI: String = s"http://api.smmry.com/&SM_API_KEY=$SMMRY_API_KEY&SM_LENGTH=3&SM_WITH_BREAK&SM_URL="

  // http://smmry.com/api
  case class SmmryResponse(
    sm_api_message: Option[String] = None, // Contains notices, warnings, and error messages.
    sm_api_character_count: Option[String] = None, // Contains the amount of characters returned
    sm_api_title: Option[String] = None, // Contains the title when available
    sm_api_content: Option[String] = None, // Contains the summary
    sm_api_keyword_array: Option[String] = None, // Contains top ranked keywords in descending order
    sm_api_error: Option[Int] = None
  ) {
    override def toString: String = {
      sm_api_content.getOrElse(sm_api_message.getOrElse("Unable to parse SmmryResponse"))
    }
  }

  object MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val fmt: RootJsonFormat[SmmryResponse] = jsonFormat6(SmmryResponse)
  }

  def parseSmmryResponse(s: String): SmmryResponse = {
    import MyJsonProtocol._
    s.parseJson.convertTo[SmmryResponse]
  }

  def parseSmmryResponse(r: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext, system: akka.actor.ActorSystem): Future[SmmryResponse] = {
    Unmarshal(r.entity)
      .to[String]
      .map(parseSmmryResponse)
  }

  def summarize(uri: String)(implicit mat: Materializer, ec: ExecutionContext, system: akka.actor.ActorSystem): Future[SmmryResponse] = {
    Http()
      .singleRequest(HttpRequest(uri = s"$BASE_URI$uri"))
      .flatMap[SmmryResponse](parseSmmryResponse)
  }
}
