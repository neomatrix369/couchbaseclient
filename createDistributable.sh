mvn clean package
mkdir -p distribute
targetJarLocation=$(echo `find target/*dependencies*.jar`)
cp $targetJarLocation distribute/couchbaseClient.jar
cp src/main/resources/runCouchbaseClient.sh distribute/runCouchbaseClient.sh
chmod u+x distribute/runCouchbaseClient.sh

zip couchbaseClient.zip distribute/*.* -x "*.log"

echo "Jar and sh files for couchbaseClient available in the distribute folder"
ls distribute