/*
 *    Copyright 2016 Jason Mar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package app

import akka.actor.ActorSystem
import akka.http.scaladsl.coding.{Gzip, NoCoding}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer

import scala.concurrent.ExecutionContext
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import scala.concurrent.duration._

object Routing {
  val XML_CONTENT_TYPE: ContentType.WithCharset = MediaTypes.`application/xml`.toContentType(HttpCharsets.`UTF-8`)
  val HTML_CONTENT_TYPE: ContentType.WithCharset = MediaTypes.`text/html`.toContentType(HttpCharsets.`UTF-8`)
  val JSON_CONTENT_TYPE: ContentType.WithFixedCharset = MediaTypes.`application/json`

  def mainRoute(db: AmazonDynamoDB)(implicit sys: ActorSystem, mat: Materializer, dis: ExecutionContext): Route = {
    decodeRequestWith(Gzip,NoCoding){
      encodeResponseWith(NoCoding,Gzip){
        get{
          pathEndOrSingleSlash{
            getFromResource("index.htm", HTML_CONTENT_TYPE)
          }
        }~
        post{
          pathPrefix("sms"){
            // https://github.com/akka/akka-http/issues/1177
            toStrictEntity(2.seconds){
              // http://doc.akka.io/docs/akka-http/10.0.7/scala/http/routing-dsl/directives/form-field-directives/formFields.html
              formFields('Body, 'From, 'To){(body, from, to) =>
                onSuccess(Summary.summarize(body)){summary =>
                  val twiml = Twilio.messageTwiml(summary.toString)
                  complete(HttpResponse(entity = HttpEntity(XML_CONTENT_TYPE, twiml)))
                }
              }
            }
          }~
          pathPrefix("submit"){
            toStrictEntity(2.seconds){
              formFields('Body, 'From, 'To){(body, from, to) =>
                onSuccess(Summary.summarize(body)){summary =>
                  val bearerToken = scala.sys.env.getOrElse("BEARER_TOKEN", "")
                  val sectionId = scala.sys.env.getOrElse("SECTION_ID", "")
                  onSuccess(OneNote.newSection(sectionId, summary.toString, bearerToken)){response =>
                    //val twiml = Twilio.messageTwiml(summary.toString)
                    complete(HttpResponse(entity = HttpEntity(JSON_CONTENT_TYPE, string = response)))
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
