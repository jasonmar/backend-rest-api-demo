## Description

This example application provides an example REST API using DynamoDB to store documents.

There are two types provided:
* Id - stored in the "ids" table with Partition Key "par"
* Document - stored in the "docs" table with Partition Key "par" and Sort Key "num"

There is a common field named "par" which is used as the DynamoDB Hash Key for both tables. This is used as the partition key for the "ids" table and is also called the Primary Key for this DynamoDB table.

The "docs" table also has a numeric Range Key field named "num" in addition to the Hash Key. The Primary Key for this DyanmoDB table is a composite key which includes both the Hash Key "par" and Range Key "num".

When retrieving a single item from "ids", only the Hash Key field is provided.
When retrieving a single item from "docs", both the Hash Key and Range Key must be provided.


## Motivation

The goal is to provide an easy to understand reference for building applications using Akka-Http 10.0.0, Scala 2.12 and DynamoDB.


## Features

* Add an Id
* Add a Document (Id must exist for Document to be created)
* Scan and list Ids with limit
* Scan and list Documents with limit

## Instructions

Download the DynamoDBLocal from AWS for testing.
Use the provided restart.sh script to start the local DynamoDB instance.
Execute `sbt run` to start the server
Use `test.sh` to test the server using curl and the aws cli
You may need to run `aws configure` to get the aws cli working


## Test Output
```




  # Test:

  Should get 404


GET http://localhost:8080/id/abc

HTTP/1.1 404 Not Found
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:37 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 83

The requested resource could not be found but may be available again in the future.




  # Test:

  Should post Id abc


POST http://localhost:8080/id {"par": "abc"}

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:37 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 2

OK




  # Test:

  Should retrieve Id abc JSON


GET http://localhost:8080/id/abc

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:37 GMT
Content-Type: application/json
Content-Length: 13

{"par":"abc"}




  # Test:

  Should post Document A


POST http://localhost:8080/doc {"par": "abc", "num":123, "pay":"def"}

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:38 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 2

OK




  # Test:

  Should retrieve Document A JSON


GET http://localhost:8080/doc/abc/123

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:38 GMT
Content-Type: application/json
Content-Length: 35

{"par":"abc","num":123,"pay":"def"}




  # Test:

  Should get 500 Bad Request


POST http://localhost:8080/doc {"par": "xyz", "num":987, "pay":"tuv"}

HTTP/1.1 400 Bad Request
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:38 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 55

The request contains bad syntax or cannot be fulfilled.




  # Test:

  Should post Id xyz


POST http://localhost:8080/id {"par": "xyz"}

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:38 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 2

OK




  # Test:

  Should post Document B


POST http://localhost:8080/doc {"par": "xyz", "num":987, "pay":"tuv"}

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:39 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 2

OK




  # Test:

  Should list Ids A,B JSON


GET http://localhost:8080/id

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:39 GMT
Content-Type: application/json
Content-Length: 39

{"items":[{"par":"abc"},{"par":"xyz"}]}




  # Test:

  Should list Documents A,B JSON


GET http://localhost:8080/doc

HTTP/1.1 200 OK
Server: akka-http/10.0.0
Date: Sat, 17 Dec 2016 05:27:39 GMT
Content-Type: application/json
Content-Length: 83

{"items":[{"par":"abc","num":123,"pay":"def"},{"par":"xyz","num":987,"pay":"tuv"}]}




  # Test:

  Should post many Documents

..........



  # Test:

  Should list many Documents


GET http://localhost:8080/doc

{"items":[{"par":"ccc","num":111,"pay":"ccc 111"},{"par":"ccc","num":222,"pay":"ccc 222"},{"par":"ccc","num":333,"pay":"ccc 333"},{"par":"ccc","num":444,"pay":"ccc 444"},{"par":"ccc","num":555,"pay":"ccc 555"},{"par":"iii","num":111,"pay":"iii 111"},{"par":"iii","num":222,"pay":"iii 222"},{"par":"iii","num":333,"pay":"iii 333"},{"par":"iii","num":444,"pay":"iii 444"},{"par":"iii","num":555,"pay":"iii 555"},{"par":"eee","num":111,"pay":"eee 111"},{"par":"eee","num":222,"pay":"eee 222"},{"par":"eee","num":333,"pay":"eee 333"},{"par":"eee","num":444,"pay":"eee 444"},{"par":"eee","num":555,"pay":"eee 555"},{"par":"hhh","num":111,"pay":"hhh 111"},{"par":"hhh","num":222,"pay":"hhh 222"},{"par":"hhh","num":333,"pay":"hhh 333"},{"par":"hhh","num":444,"pay":"hhh 444"},{"par":"hhh","num":555,"pay":"hhh 555"},{"par":"abc","num":123,"pay":"def"},{"par":"ddd","num":111,"pay":"ddd 111"},{"par":"ddd","num":222,"pay":"ddd 222"},{"par":"ddd","num":333,"pay":"ddd 333"},{"par":"ddd","num":444,"pay":"ddd 444"},{"par":"ddd","num":555,"pay":"ddd 555"},{"par":"fff","num":111,"pay":"fff 111"},{"par":"fff","num":222,"pay":"fff 222"},{"par":"fff","num":333,"pay":"fff 333"},{"par":"fff","num":444,"pay":"fff 444"},{"par":"fff","num":555,"pay":"fff 555"},{"par":"ggg","num":111,"pay":"ggg 111"},{"par":"ggg","num":222,"pay":"ggg 222"},{"par":"ggg","num":333,"pay":"ggg 333"},{"par":"ggg","num":444,"pay":"ggg 444"},{"par":"ggg","num":555,"pay":"ggg 555"},{"par":"jjj","num":111,"pay":"jjj 111"},{"par":"jjj","num":222,"pay":"jjj 222"},{"par":"jjj","num":333,"pay":"jjj 333"},{"par":"jjj","num":444,"pay":"jjj 444"},{"par":"jjj","num":555,"pay":"jjj 555"},{"par":"bbb","num":111,"pay":"bbb 111"},{"par":"bbb","num":222,"pay":"bbb 222"},{"par":"bbb","num":333,"pay":"bbb 333"},{"par":"bbb","num":444,"pay":"bbb 444"},{"par":"bbb","num":555,"pay":"bbb 555"},{"par":"xyz","num":987,"pay":"tuv"},{"par":"aaa","num":111,"pay":"aaa 111"},{"par":"aaa","num":222,"pay":"aaa 222"},{"par":"aaa","num":333,"pay":"aaa 333"},{"par":"aaa","num":444,"pay":"aaa 444"},{"par":"aaa","num":555,"pay":"aaa 555"}]}



```

## Authors and Copyright

Copyright (C) 2016 Jason Mar
