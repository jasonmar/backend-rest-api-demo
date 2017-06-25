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

object Routing {

  def mainRoute(db: AmazonDynamoDB)(implicit sys: ActorSystem, mat: Materializer, dis: ExecutionContext): Route = {
    decodeRequestWith(Gzip,NoCoding){
      encodeResponseWith(NoCoding,Gzip){
        get{
          pathEndOrSingleSlash{
            complete(StatusCodes.OK)
          }
        }~
        post{
          pathPrefix("sms"){
            entity(as[String]){e =>
              System.out.println(e)
              onSuccess(Summary.summarize(e)){summary =>
                complete(
                  StatusCodes.OK,
                  HttpEntity(
                    MediaTypes.`application/xml`.toContentType(HttpCharsets.`UTF-8`),
                    Twilio.respondWithMessage(summary.toString)
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}
