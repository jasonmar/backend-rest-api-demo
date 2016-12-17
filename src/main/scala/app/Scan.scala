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

import app.Schema.{Document, Documents, Id, Ids}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import dynamodb.Items

object Scan {
  def ids(db: AmazonDynamoDB, limit: Int = 256): Option[Ids] = {
    val recLimit = (limit / 25) + 1
    val res = Items.scanRec(db, Schema.IdTable.name, Schema.IdTable.attrNames, recLimit = recLimit)
    val ids: Vector[Id] = res.items.flatMap{m =>
      m.get(Schema.HASH_KEY) // get the hash key attribute
        .map(_.getS) // extract the string value
        .map(Id) // create the object
    }
    if (ids.isEmpty) None
    else Some(Ids(ids))
  }

  def docs(db: AmazonDynamoDB, limit: Int = 256): Option[Documents] = {
    val recLimit = (limit / 25) + 1
    val res = Items.scanRec(db, Schema.DocsTable.name, Schema.DocsTable.attrNames, recLimit = recLimit) // recursive scan
    val documents: Vector[Document] = res.items.flatMap{m =>
      for {
        par <- m.get(Schema.HASH_KEY) // get the hash key attribute
        num <- m.get(Schema.RANGE_KEY) // get the range key attribute
        pay <- m.get(Schema.PAYLOAD) // get the payload attribute
      } yield {
        Document(par.getS, num.getN.toLong, pay.getS) // create the object
      }
    }
    if (documents.isEmpty) None
    else Some(Documents(documents))
  }
}
