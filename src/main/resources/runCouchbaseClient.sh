#!/usr/bin/env bash
java -jar couchbaseClient.jar $1 $2 "$3" 2> couchbaseClient.log
