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
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContext, Future}
import app.Schema._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB

import scala.util._

object Routing {

  val idPath = "id"
  val docPath = "doc"

  /**
    * http://doc.akka.io/docs/akka-http/current/scala/http/introduction.html#routing-dsl-for-http-servers
    */
  def mainRoute(db: AmazonDynamoDB)(implicit sys: ActorSystem, mat: Materializer, dis: ExecutionContext): Route = {
    decodeRequestWith(Gzip,NoCoding){
      encodeResponseWith(NoCoding,Gzip){
        get{
          pathPrefix(Segment){id =>
            entity(as[String]) {json =>
              System.out.println(json)
              complete(
                StatusCodes.OK,
                HttpEntity(
                  MediaTypes.`application/xml`.toContentType(HttpCharsets.`UTF-8`),
                  Twilio.respondWithMessage("this is a text message for GET")
                )
              )
            }
          }
        }~
        post{
          pathPrefix(Segment){id =>
            entity(as[String]) {json =>
              System.out.println(json)
              complete(
                StatusCodes.OK,
                HttpEntity(
                  MediaTypes.`application/xml`.toContentType(HttpCharsets.`UTF-8`),
                  Twilio.respondWithMessage("this is a text message for GET")
                )
              )
            }
          }
        }
      }
    }
  }
}
