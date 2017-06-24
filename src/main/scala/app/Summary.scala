package app

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

object Summary {

  val API_KEY: String = sys.env.getOrElse("SMMRY_API_KEY", "")
  val BASE_URI: String = s"http://api.smmry.com/&SM_API_KEY=${API_KEY}&SM_LENGTH=3&SM_WITH_BREAK&SM_URL="

  // http://smmry.com/api
  case class SmmryResponse(
    sm_api_message: Option[String], // Contains notices, warnings, and error messages.
    sm_api_character_count: Option[String], // Contains the amount of characters returned
    sm_api_title: Option[String], // Contains the title when available
    sm_api_content: Option[String], // Contains the summary
    sm_api_keyword_array: Option[String], // Contains top ranked keywords in descending order
    sm_api_error: Option[String]
  )

  object MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val fmt = jsonFormat6(SmmryResponse)
  }

  def summarize(uri: String)(implicit mat: Materializer, ec: ExecutionContext, system: akka.actor.ActorSystem): Future[String] = {
    System.out.println(uri)
    val httpRequest = HttpRequest(uri = s"${BASE_URI}${uri}")
    val futureResponse: Future[HttpResponse] = Http().singleRequest(httpRequest)

    import MyJsonProtocol._
    futureResponse.flatMap[String](r =>
      Unmarshal(r.entity).to[String]
    )
  }
}
