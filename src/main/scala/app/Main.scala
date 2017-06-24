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
import akka.stream.ActorMaterializer
import akka.http.scaladsl._
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import dynamodb.Dynamo

import scala.util.Try

object Main extends App {
  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val dispatch = sys.dispatcher

  System.out.println("creating dynamoDB client")
  val client = Dynamo.client()
  val db = new DynamoDB(client)

  System.out.println("starting server")
  val server = Http().bindAndHandle(
    handler = Routing.mainRoute(client),
    interface = "0.0.0.0",
    port = 8080
  )

  System.out.println("server started")
}
