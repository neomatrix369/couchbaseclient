couchbaseclient
===============

A couchbase client that allows quick access to the couchbase cluster / node. Send SQL, receive results (JSON), handy for writing scripts and piping results into files.

The couchbase client `cbq` does not accept N1QL queries as parameters or pipes and also its not straight-forward to pipe the results of these queries to a file or another device, this utility program attempts to help overcome these limitations.

Build
-----

`createDistributable.sh` will build the artifact using maven and create a zip file in the root folder of the project.

The zip file contains a folder containing these three files:


	couchbaseClient.jar    - is a self-contained jar file. runCouchbaseClient.sh
	runCouchbaseClient.sh  - run the jar passing it the require params
	README.md              - this file

Usage of the utility program
----------------------------

###Why would you use this tool ?
- you do not want to manually ssh into Couchbase instances and type N1QL queries, across multiple environments
- you do not have access to the the Couchbase web-console or any GUI and have work from command-line UI
- you want to process the results of a N1QL query further
- you want to write script(s) that send N1QL queries to Couchbase, that can be invoked from any application that supports scripting
- you want to do any of the above in a repeated fashion or maybe even automate it via cron jobs or other tools (i.e. Jenkins, ansible, etc...)
 
**Note:** one of the caveats is, the program should have access to the Couchbase Cluster/Node (via its private IP).

###How to build it ?

    ./createDistributable.sh

###What to do next ?

Copy the zip artifact from the local machine to one of the jump boxes on AWS with the below:

    scp -i [ssh key] couchbaseClient.zip user@host.com:"/path/to/copy/to"

###How to run it ?

	unzip couchbaseClient.zip 
	   
	cd couchbaseClient
	
	./runCouchbaseClient.sh [required parameters]
	
	required parameters:
		--host, -h   - name or IP address (port number is optional) of the Couchbase cluster / node
    	--bucket, -b - name of the bucket on the Couchbase cluster / node
    	--query, -q  - Couchbase-compliant N1QL query surrounded by single or double quotes
	
	examples:
		./runCouchbaseClient.sh --host 192.168.99.100:8091 --bucket sapi --query"select * from sapi limit 5"
		
		or
		
		./runCouchbaseClient.sh --host 172.31.29.132 --bucket Transport --query 'select * from Transport limit 5'
		
		or
		
		./runCouchbaseClient.sh --host 172.31.29.132 --bucket Transport --query "select * from Transport limit 5" > queryResults.log


###Output and errors
   
   All log and error output are written into the `couchbaseClient.log` file which gets created in the folder in which the program is run in.
    
   Results of successful sql execution are printed on stdout and this can be piped into a file or sent elsewhere.
   
   Here is an example of a `couchbaseClient.log` file:
   
		Mar 04, 2016 4:23:39 PM com.couchbase.client.core.CouchbaseCore <init>
		INFO: CouchbaseEnvironment: {sslEnabled=false, sslKeystoreFile='null', sslKeystorePassword='null', queryEnabled=false, queryPort=8093, bootstrapHttpEnabled=true, bootstrapCarrierEnabled=true, bootstrapHttpDirectPort=8091, bootstrapHttpSslPort=18091, bootstrapCarrierDirectPort=11210, bootstrapCarrierSslPort=11207, ioPoolSize=4, computationPoolSize=4, responseBufferSize=16384, requestBufferSize=16384, kvServiceEndpoints=1, viewServiceEndpoints=1, queryServiceEndpoints=1, searchServiceEndpoints=1, ioPool=NioEventLoopGroup, coreScheduler=CoreScheduler, eventBus=DefaultEventBus, packageNameAndVersion=couchbase-jvm-core/1.2.5 (git: 1.2.5), dcpEnabled=false, retryStrategy=BestEffort, maxRequestLifetime=75000, retryDelay=ExponentialDelay{growBy 1.0 MICROSECONDS, powers of 2; lower=100, upper=100000}, reconnectDelay=ExponentialDelay{growBy 1.0 MILLISECONDS, powers of 2; lower=32, upper=4096}, observeIntervalDelay=ExponentialDelay{growBy 1.0 MICROSECONDS, powers of 2; lower=10, upper=100000}, keepAliveInterval=30000, autoreleaseAfter=2000, bufferPoolingEnabled=true, tcpNodelayEnabled=true, mutationTokensEnabled=false, socketConnectTimeout=1000, dcpConnectionBufferSize=20971520, dcpConnectionBufferAckThreshold=0.2, queryTimeout=75000, viewTimeout=75000, kvTimeout=2500, connectTimeout=5000, disconnectTimeout=25000, dnsSrvEnabled=false}
		Mar 04, 2016 4:23:40 PM com.couchbase.client.core.node.CouchbaseNode signalConnected
		INFO: Connected to Node 192.168.99.100
		Mar 04, 2016 4:23:41 PM com.couchbase.client.core.config.DefaultConfigurationProvider$8 call
		INFO: Opened bucket sapi
		Mar 04, 2016 4:23:41 PM com.couchbase.client.core.config.DefaultConfigurationProvider$11 call
		INFO: Closed bucket sapi
		Mar 04, 2016 4:23:41 PM com.couchbase.client.core.node.CouchbaseNode signalDisconnected
		INFO: Disconnected from Node 192.168.99.100
		Mar 04, 2016 4:23:41 PM com.couchbase.client.core.env.DefaultCoreEnvironment$4 call
		INFO: Shutdown Core Scheduler: success
		Mar 04, 2016 4:23:41 PM com.couchbase.client.core.env.DefaultCoreEnvironment$4 call
		INFO: Shutdown IoPool: success
		Mar 04, 2016 4:23:41 PM com.couchbase.client.core.env.DefaultCoreEnvironment$4 call
		INFO: Shutdown Runtime Metrics Collector: success
		Mar 04, 2016 4:23:41 PM com.couchbase.client.core.env.DefaultCoreEnvironment$4 call
		INFO: Shutdown Latency Metrics Collector: success
		Mar 04, 2016 4:23:42 PM com.couchbase.client.core.env.DefaultCoreEnvironment$4 call
		INFO: Shutdown Netty: success
   
   Successful execution of the supplied SQL command results in a JSON like the below in the stdout:
   
		{
		  someEntity: {
		   "business": "GROCERY",
		   "serviceCode": "ONE_HOUR",
		   "locationId": "6473",
		   "locationType": "STORE",
		   "_class": "com.tesco.transport.shipping.model.entities.SlotConfiguration",
		   "weekNumber": 2
		  }
		}
    
###Known issues

#### Accessing remote Couchbase cluster/node is returning an error
It is a known issue that we cannot access remote Couchbase nodes/clusters from our local machine via this tool. If we do that then it results in the below error:

		WARNING: An exception was thrown by com.couchbase.client.core.endpoint.AbstractEndpoint$2.operationComplete()
		rx.exceptions.OnErrorNotImplementedException: connection timed out: [public host ip][port]
		.
		.
		.
		Caused by: java.util.concurrent.RejectedExecutionException: Task java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask@3fd372cf 
		rejected from java.util.concurrent.ScheduledThreadPoolExecutor@33b5f576[Terminated, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 190]
		.
		.
		.
		Mar 04, 2016 4:31:08 PM com.couchbase.client.core.endpoint.AbstractEndpoint$2 operationComplete
		WARNING: [null][KeyValueEndpoint]: Socket connect took longer than specified timeout.

**Solution:** We need to be at the sub-net as the Couchbase nodes/clusters in order to be able to send it queries and get back a response. 
And use the private IP assigned to the respective couchbase instances. Log on to the jumpbox by ssh-ing into it and run the same command.  


Note to fellow developers
-------------------------
- Modify the utility program to add more features to it
- Create a Windows equivalent of the distribution bash file
- Feel free to replace the bash script with an equivalent maven release/distribution plugin
- Update the bash script


###<i>Have fun using it.</i>
