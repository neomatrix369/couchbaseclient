#!/usr/bin/env bash
echo "*** Building artifacts via maven ***"
mvn clean package

echo "*** Creating the couchbaseClient folder ***"
rm -fr couchbaseClient
mkdir couchbaseClient

echo "*** Searching for the jar artifact ***"
targetJarLocation=$(echo `find target/*dependencies*.jar`)

echo "*** Copying necessary files for the zip artifact ***"
cp $targetJarLocation couchbaseClient/couchbaseClient.jar
cp src/main/resources/runCouchbaseClient.sh couchbaseClient/runCouchbaseClient.sh
cp README.md couchbaseClient/README.md

echo "*** Making the script file an executable ***"
chmod u+x couchbaseClient/runCouchbaseClient.sh

echo "*** Zipping all the files into a zip file for distribution ***"
zip couchbaseClient.zip couchbaseClient/*.* -x "*.log"

echo "Jar and sh files for couchbaseclient available in the couchbaseClient folder."
ls -lash couchbaseClient
