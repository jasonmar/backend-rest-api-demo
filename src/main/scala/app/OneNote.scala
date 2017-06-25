package app

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentType, HttpCharsets, HttpRequest, MediaTypes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

object OneNote {
  val BASE_URI = "https://graph.microsoft.com/v1.0"
  val HTML_CONTENT_TYPE: ContentType.WithCharset = MediaTypes.`text/html`.toContentType(HttpCharsets.`UTF-8`)
  def section(id: String) = s"/me/onenote/sections/$id/pages"

  def newSection(sectionId: String, msg: String, bearerToken: String)(implicit mat: Materializer, ec: ExecutionContext, system: akka.actor.ActorSystem): Future[String] = {
    val sectionPath = section(sectionId)
    Http().singleRequest(
        HttpRequest(uri = s"$BASE_URI$sectionPath")
          .addHeader(RawHeader("Authorization", bearerToken))
          .addHeader(RawHeader("Content-Type", "text/html"))
    ).flatMap[String](r => Unmarshal(r.entity).to[String])
  }

}
