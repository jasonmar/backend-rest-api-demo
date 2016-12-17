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

package dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document._
import com.amazonaws.services.dynamodbv2.document.spec.{BatchWriteItemSpec, PutItemSpec, QuerySpec}
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model._

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable

import dynamodb.Dynamo._

object Items {

  case class ScanResult2(results: ScanResult, items: Vector[Map[String,AttributeValue]])

  def readScanResult(s: ScanResult): Option[Vector[Map[String,AttributeValue]]] = {
    for {
      items <- Option(s.getItems)
    } yield {
      items.asScala.toVector // convert Java List to Scala Vector
        .map(_.asScala.toMap) // convert Java Map to Scala Map
    }
  }

  @tailrec
  def scanRec(
    client: AmazonDynamoDB,
    tableName: String,
    attributes: Vector[String],
    consistentRead: Boolean = false,
    limit: Int = 25,
    recLimit: Int = 8,
    last: Option[java.util.Map[String, AttributeValue]] = None,
    results: mutable.Queue[Map[String,AttributeValue]] = mutable.Queue.empty[Map[String,AttributeValue]]
  ): ScanResult2 = {
    val res = scanTable(client, tableName, attributes, consistentRead, limit, last)
    readScanResult(res) match {
      case Some(items) =>
        items.foreach{ item => results.enqueue(item) }
      case _ =>
    }

    val lastKeyOpt = Option(res.getLastEvaluatedKey)
    val noKeysRemaining = lastKeyOpt.isEmpty
    if (noKeysRemaining || recLimit < 1){
      ScanResult2(res, results.toVector)
    } else {
      scanRec(client, tableName, attributes, consistentRead, limit, recLimit - 1, last = lastKeyOpt, results = results)
    }
  }

  def scanTable(
    client: AmazonDynamoDB,
    tableName: String,
    attributes: Vector[String],
    consistentRead: Boolean = false,
    limit: Int = 25,
    last: Option[java.util.Map[String, AttributeValue]] = None
  ): ScanResult = {
    val r = new ScanRequest()
      .withTableName(tableName)
      .withAttributesToGet(attributes.asJavaCollection)
      .withConsistentRead(consistentRead)
      .withLimit(limit)

    last match {
      case Some(key) => client.scan(r.withExclusiveStartKey(key))
      case _ => client.scan(r)
    }
  }

  /**
    * http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_BatchWriteItem.html
    * @param dynamoDB
    * @param tableName
    * @param items
    * @return
    */
  def write(dynamoDB: DynamoDB, tableName: String, items: Vector[Item]): Vector[BatchWriteItemOutcome] = {
    val it = items.iterator
    val results = mutable.Queue.empty[BatchWriteItemOutcome]
    while (it.hasNext){
      val itemBatch = it.take(25)
      val twi = Items.writeItems(tableName, itemBatch.toVector)
      val writeOutcome = dynamoDB.batchWriteItem(twi)
      results.enqueue(writeOutcome)
      val batchOutcome = writeUnprocessed(dynamoDB, writeOutcome)
      results.enqueue(batchOutcome)
    }
    results.toVector
  }

  @tailrec
  def writeUnprocessed(dynamoDB: DynamoDB, batchWriteItemOutcome: BatchWriteItemOutcome): BatchWriteItemOutcome = {
    val unprocessed = batchWriteItemOutcome.getUnprocessedItems
    if (unprocessed.isEmpty){
      batchWriteItemOutcome
    } else {
      writeUnprocessed(dynamoDB, dynamoDB.batchWriteItemUnprocessed(unprocessed))
    }
  }

  def batchWrite(items: Vector[TableWriteItems]): BatchWriteItemSpec = {
    new BatchWriteItemSpec()
      .withTableWriteItems(items:_*)
  }

  def writeItems(tableName: String, items: Iterable[Item]): TableWriteItems = {
    new TableWriteItems(tableName)
      .withItemsToPut(items.asJavaCollection)
  }

  def putItem(tableName: String, item: Map[String,AttributeValue]): PutItemRequest = {
    new PutItemRequest()
      .withItem(item.asJava)
      .withTableName(tableName)
  }
  def putItems(item: Item): PutItemSpec = {
    new PutItemSpec()
      .withItem(item)
  }

  trait DynamoItemBuilder extends AWSSDKObjectBuilder {
    def pk(): PrimaryKey
    override def result(): Item
    def result2(): Map[String, AttributeValue] // The schema of the item, including non-Key attributes
  }

  def stringAttr(k: String, s: String): (String, AttributeValue) = k -> new AttributeValue().withS(s)
  def numAttr(k: String, s: String): (String, AttributeValue) = k -> new AttributeValue().withN(s)

  def keyMap(attrs: (String,AttributeValue)*): Map[String,AttributeValue] = {
    attrs.toMap
  }

  def getItemRequest(
    tableName: String,
    keyAttributeMap: Map[String,AttributeValue],
    projectionExpression: Option[String] = None,
    exr: Option[Map[String,String]] = None,
    c: Boolean = false
  ): GetItemRequest = {
    new GetItemRequest()
      .withTableName(tableName)
      .withKey(keyAttributeMap.asJava)
      .withProjectionExpression(projectionExpression.orNull)
      .withExpressionAttributeNames(exr.map(_.asJava).orNull)
      .withConsistentRead(c)
  }

  def getItemsRequest(tableName: String, items: Map[String,KeysAndAttributes]): BatchGetItemRequest = {
    new BatchGetItemRequest()
      .withRequestItems(items.asJava)
  }

  def getItem(db: AmazonDynamoDB, r: GetItemRequest): Option[Map[String,AttributeValue]] = {
    Option(db.getItem(r).getItem).map(_.asScala.toMap)
  }

  def keysAndAttributes(projection: String): KeysAndAttributes = {
    new KeysAndAttributes()
      .withConsistentRead(false)
      .withProjectionExpression(projection)
  }

  /**
    * http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/QueryingJavaDocumentAPI.html
    *
    * @param valueMap
    * @param keyConditions Conditions on Partition Key and Sort Key only, limit one condition per key
    *                      "Id = :v_id and ReplyDateTime > :v_reply_dt_tm"
    * @param filterExpression Conditions on Attributes
    *                      "PostedBy = :v_posted_by"
    * @param consistent
    * @param maxPage
    * @param maxResult
    * @return
    */
  def query(valueMap: ValueMap, keyConditions: String, filterExpression: String, consistent: Boolean = false, maxPage: Int = 32, maxResult: Int = 32): QuerySpec = {
    new QuerySpec()
      .withKeyConditionExpression(keyConditions)
      .withFilterExpression(filterExpression)
      .withValueMap(valueMap)
      .withConsistentRead(consistent)
      .withMaxPageSize(maxPage)
      .withMaxResultSize(maxResult)
  }

  def print(c: ItemCollection[QueryOutcome]): Unit = {
    c.iterator().asScala.foreach{item =>
      System.out.println(item.toJSON)
    }
  }
}
