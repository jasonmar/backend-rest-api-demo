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

import app.Schema.{Document, Id}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import dynamodb.Items

object Persistence {

  class MissingKeyException(message: String = null, cause: Throwable = null) extends
    RuntimeException(MissingKeyException.defaultMessage(message, cause), cause)

  object MissingKeyException {
    def defaultMessage(message: String, cause: Throwable) =
      if (message != null) message
      else if (cause != null) cause.toString()
      else null
  }

  def getId(db: AmazonDynamoDB, par: String): Option[Id] = {
    val r = Items.getItemRequest(
      Schema.IdTable.name,
      Schema.IdTable.keyMap(par)
    )
    for {
      itemMap <- Items.getItem(db, r)
      id <- itemMap.get(Schema.HASH_KEY).map(_.getS)
    } yield {
      Id(id)
    }
  }

  def getDoc(db: AmazonDynamoDB, par: String, num: String): Option[Document] = {
    val r = Items.getItemRequest(
      Schema.DocsTable.name,
      Schema.DocsTable.keyMap(par, num)
    )
    for {
      itemMap <- Items.getItem(db, r)
      id <- itemMap.get(Schema.HASH_KEY).map(_.getS)
      num1 <- itemMap.get(Schema.RANGE_KEY).map(_.getN)
      pay1 <- itemMap.get(Schema.PAYLOAD).map(_.getS)
    } yield {
      Document(id, num1.toLong, pay1)
    }
  }

  def postId(db: AmazonDynamoDB, id: Id): PutItemResult = {
    val r = Items.putItem(id.table.name, id.result2())
    db.putItem(r)
  }

  def postDocument(db: AmazonDynamoDB, document: Document): PutItemResult = {
    val r = Items.putItem(document.table.name, document.result2())
    db.putItem(r)
  }

}
