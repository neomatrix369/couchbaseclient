#!/usr/bin/env bash
echo "*** Building artifacts via maven ***"
mvn clean package

echo "*** Creating the distribute folder ***"
rm -fr distribute
mkdir distribute

echo "*** Searching for the jar artifact ***"
targetJarLocation=$(echo `find target/*dependencies*.jar`)

echo "*** Copying necessary files for the zip artifact ***"
cp $targetJarLocation distribute/couchbaseClient.jar
cp src/main/resources/runCouchbaseClient.sh distribute/runCouchbaseClient.sh
cp README.md distribute/README.md

echo "*** Making the script file an executable ***"
chmod u+x distribute/runCouchbaseClient.sh

echo "*** Zipping all the files into a zip file for distribution ***"
zip couchbaseClient.zip distribute/*.* -x "*.log"

echo "Jar and sh files for couchbaseClient available in the distribute folder."
ls -lash distribute
