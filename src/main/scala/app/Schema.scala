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

import com.amazonaws.services.dynamodbv2.document.{Item, PrimaryKey}
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import dynamodb.CreateTable.{Attribute, NumAttr, StringAttr, DynamoTable, PartitionKey, SortKey}
import dynamodb.Items
import spray.json.DefaultJsonProtocol._
import dynamodb.Items.DynamoItemBuilder


object Schema {

  val HASH_KEY = "par"
  val RANGE_KEY = "num"
  val PAYLOAD = "pay"

  /**
    * This object provides easy access to types which can be passed to the AWS DynamoDB Java SDK
    */
  object IdTable extends DynamoTable {
    override val name: String = "ids"
    override val attributes: Vector[Attribute] = Vector(StringAttr(HASH_KEY)) // Elements of Primary Key for table creation
    override val partitionKey: PartitionKey = PartitionKey(HASH_KEY)
    override val attrNames: Vector[String] = Vector(HASH_KEY) // Used in Scans

    // The Key Map is used by the AWS SDK to identify elements of the Primary Key
    def keyMap(par: String): Map[String, AttributeValue] = {
      Items.keyMap(Items.stringAttr(HASH_KEY, par))
    }
  }

  object DocsTable extends DynamoTable {
    override val name: String = "docs"
    override val attributes: Vector[Attribute] = Vector(StringAttr(HASH_KEY), NumAttr(RANGE_KEY)) // Elements of Primary Key for table creation
    override val partitionKey: PartitionKey = PartitionKey(HASH_KEY)
    override val sortKeyOpt: Option[SortKey] = Some(SortKey(RANGE_KEY))
    override val attrNames: Vector[String] = Vector(HASH_KEY, RANGE_KEY, PAYLOAD) // Used in Scans

    // The Key Map is used by the AWS SDK to identify elements of the Primary Key
    def keyMap(par: String, num: String): Map[String, AttributeValue] = {
      Items.keyMap(Items.stringAttr(HASH_KEY, par), Items.numAttr(RANGE_KEY, num))
    }
  }

  final case class Id(par: String) extends IdItem
  final case class Document(par: String, num: Long, pay: String) extends DocumentItem

  // Used to serialize a collection to JSON
  final case class Ids(items: Vector[Id])
  final case class Documents(items: Vector[Document])

  // import these to enable implicit SprayJson marshalling and unmarshalling
  implicit val fmtDocument = jsonFormat3(Document)
  implicit val fmtDocuments = jsonFormat1(Documents)
  implicit val fmtId = jsonFormat1(Id)
  implicit val fmtIds = jsonFormat1(Ids)

  /** In addition to specifying common fields for the Id item type,
    * This trait provides easy access to types used by AWS DynamoDB Java SDK
    */
  trait IdItem extends DynamoItemBuilder {
    val par: String
    def table: DynamoTable = IdTable
    override def pk(): PrimaryKey = new PrimaryKey(HASH_KEY, par)
    override def result(): Item = new Item().withPrimaryKey(pk())
    override def result2(): Map[String,AttributeValue] = {
      Items.keyMap(Items.stringAttr(HASH_KEY, par))
    }
  }

  /** In addition to specifying common fields for the Document item type,
    * This trait provides easy access to types used by AWS DynamoDB Java SDK
    */
  trait DocumentItem extends DynamoItemBuilder {
    val par: String
    val num: Long
    val pay: String
    def table: DynamoTable = DocsTable
    override def pk(): PrimaryKey = new PrimaryKey(HASH_KEY, par, RANGE_KEY, num)
    override def result(): Item = {
      new Item()
        .withPrimaryKey(pk())
        .withString(PAYLOAD, pay)
    }
    override def result2(): Map[String,AttributeValue] = {
      Items.keyMap(
        Items.stringAttr(HASH_KEY, par),
        Items.numAttr(RANGE_KEY, num.toString),
        Items.stringAttr(PAYLOAD, pay)
      )
    }
  }


}
