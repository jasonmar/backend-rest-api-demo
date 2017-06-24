package app

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json._

import scala.concurrent.Future

object Summary {

  val API_KEY: String = sys.env.getOrElse("SMMRY_API_KEY", "")
  val BASE_URI: String = s"http://api.smmry.com/&SM_API_KEY=${API_KEY}&SM_LENGTH=3&SM_WITH_BREAK&SM_URL="

  def summarize(uri: String): Future[String] = {
    System.out.println(uri)
    val httpRequest = HttpRequest(uri = s"${BASE_URI}${uri}")
    val futureResponse: Future[HttpResponse] = Http().singleRequest(httpRequest)
    futureResponse.flatMap[String](r =>
      Unmarshal(r.entity).to[String]
    )
  }
}
