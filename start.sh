#!/bin/sh

nohup java -Xms1g -Xmx1g -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -inMemory -port 8000 >ddb.log 2>&1 &


