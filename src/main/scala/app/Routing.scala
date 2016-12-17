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
          pathPrefix(idPath / Segment){id =>
            // Retrieve a specific Id from DynamoDB using the Partition Key
            // This should be a fast operation
            val getId = Future(Try(Persistence.getId(db, id))) // Wrap in a Try to capture exceptions
            onSuccess(getId) {
              case Success(Some(item)) =>
                complete(item)
              case Success(None) => // Request was successful but there was no item returned
                complete(StatusCodes.NotFound)
              case Failure(e) => // An exception was caught, print it and respond with an Http error
                e.printStackTrace(System.err)
                complete(StatusCodes.InternalServerError)
            }
          }~
          pathPrefix(docPath / Segment / Segment){ (id, num) =>
            // Retrieve a specific Document from DynamoDB using the Partition Key and Sort Key
            // This should be a fast operation
            val getDoc = Future(Try(Persistence.getDoc(db, id, num))) // Wrap in a Try to capture exceptions
            onSuccess(getDoc) {
              case Success(Some(item)) =>
                complete(item)
              case Success(None) => // Request was successful but there was no item returned
                complete(StatusCodes.NotFound)
              case Failure(e) => // An exception was caught, print it and respond with an Http error
                e.printStackTrace(System.err)
                complete(StatusCodes.InternalServerError)
            }
          }~
          pathPrefix(idPath){
            // List Ids in ids table
            val scanIds = Future(Try(Scan.ids(db)))
            onSuccess(scanIds) {
              case Success(Some(item)) =>
                complete(item)
              case Success(None) =>
                complete(StatusCodes.NotFound)
              case Failure(e) =>
                e.printStackTrace(System.err)
                complete(StatusCodes.InternalServerError)
            }
          }~
          pathPrefix(docPath){
            // List documents in docs table
            val scanDocs = Future(Try(Scan.docs(db)))
            onSuccess(scanDocs) {
              case Success(Some(item)) =>
                complete(item)
              case Success(None) =>
                complete(StatusCodes.NotFound)
              case Failure(e) =>
                e.printStackTrace(System.err)
                complete(StatusCodes.InternalServerError)
            }
          }
        }~
        post{
          pathPrefix(idPath){
            entity(as[Id]){id => // Use SprayJsonSupport to create Id object
              val postId = Future(Try(Persistence.postId(db, id)))
              onSuccess(postId) {
                case Success(_) =>
                  complete(StatusCodes.OK)
                case Failure(e) =>
                  e.printStackTrace(System.err)
                  complete(StatusCodes.InternalServerError)
              }
            }
          }~
          pathPrefix(docPath){
            entity(as[Document]){document => // Use SprayJsonSupport to create Id object
              val postDocument = Future{
                // First attempt to retrieve the Id
                Try(Persistence.getId(db, document.par)) match {
                  case Success(Some(_)) =>
                    // Allow the request to proceed if Id exists
                    Try(Persistence.postDocument(db, document))
                  case _ =>
                    // If the Id is not found then the request fails
                    Failure(new Persistence.MissingKeyException(s"Id ${document.par} doesn't exist"))
                }
              }
              onSuccess(postDocument) {
                case Success(_) =>
                  complete(StatusCodes.OK)
                case Failure(e: Persistence.MissingKeyException) => // Special failure case here where Id didn't exist
                  // Don't print the exception since it is known to be the user's fault
                  complete(StatusCodes.BadRequest)
                case Failure(e) =>
                  e.printStackTrace(System.err)
                  complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }
  }
}
