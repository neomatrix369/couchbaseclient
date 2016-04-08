package com.tesco.couchbase.client;

import static com.couchbase.client.java.CouchbaseCluster.create;
import static com.couchbase.client.java.query.N1qlQuery.simple;
import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.util.Arrays.asList;

import java.io.PrintStream;
import java.util.List;

import org.json.JSONObject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

public class BetterCBQ {

  private static final String ERROR_MESSAGE_FORMAT = "Error occurred: %s. Check the log file for further details.";
  private static final String COUCHBASE_CLIENT_PROGRAM_NAME = "couchbaseClient";

  @Parameter(required=true, names={"--host", "-h"}, description = "name or IP address (port number is optional) of the Couchbase cluster / node")
  private static String couchBaseHost;

  @Parameter(required=true, names={"--bucket", "-b"}, description = "name of the bucket on the Couchbase cluster / node")
  private static String bucketName;

  @Parameter(required=true, names={"--query", "-q"}, description = "Couchbase-compliant N1QL query surrounded by single or double quotes")
  private static String n1qlQuery;

  public static void main(String[] args) throws Exception {
    runBetterCBQ(args);
  }

  private static void runBetterCBQ(String[] args) throws Exception {
    final BetterCBQ betterCBQ = new BetterCBQ();
    JCommander jCommander;
    try {
      jCommander = new JCommander(betterCBQ, args);
      jCommander.setProgramName(COUCHBASE_CLIENT_PROGRAM_NAME);
      betterCBQ.execute(couchBaseHost, bucketName, n1qlQuery);
    } catch (ParameterException parameterException) {
      err.println(parameterException.getMessage());
      out.println(betterCBQ.getUsageText());
      System.exit(-1);
    } catch (Exception allOtherExceptions) {
      err.println(format(ERROR_MESSAGE_FORMAT, allOtherExceptions.getMessage()));
      out.println(format(ERROR_MESSAGE_FORMAT, allOtherExceptions.getMessage()));
      System.exit(-1);
    }
  }

  public void execute(String couchBaseHost, String bucketName, String queryString) throws Exception {
    List<String> nodes = asList(couchBaseHost);
    Cluster cluster = null;
    Bucket bucket = null;

    boolean errorOccurred = false;
    try {
      cluster = create(nodes);
      bucket = cluster.openBucket(bucketName);
      N1qlQueryResult queryResult = bucket.query(simple(queryString));
      errorOccurred = displayQueryResults(queryString, queryResult);
    } finally {
      if (bucket != null) {
        bucket.close();
      }
      if (cluster != null) {
          cluster.disconnect();
      }
      if (errorOccurred) {
          exit(-1);
      }
    }
  }

  private boolean displayQueryResults(String queryString, N1qlQueryResult queryResult) {
    boolean errorOccurred = false;
    if (queryResult.finalSuccess()) {
      for (N1qlQueryRow eachRow : queryResult.allRows()) {
        out.println(convertToPrettyJsonString(eachRow));
      }
    } else {
      errorOccurred = true;
      printErrorMessageTo(out, queryString, queryResult);
      printErrorMessageTo(err, queryString, queryResult);
    }
    return errorOccurred;
  }

  private static void printErrorMessageTo(
      PrintStream output, String queryString, N1qlQueryResult queryResult) {
    output.println("Query did not execute successfully, due to one or more errors.");
    output.format("Requested query: '%s'%n", queryString);
    output.println(queryResult.errors());
    output.println("Check the log file for further details.");
  }

  private static String convertToPrettyJsonString(N1qlQueryRow eachRow) {
    String jsonAsString = eachRow.value().toString();
    JSONObject json = new JSONObject(jsonAsString);
    return json.toString(2);
  }

  private String getUsageText() {
    return
        format(
        "usage: %s [required parameters] \n" +
            "  required parameters:\n" +
            "    --host, -h    - name or IP address (port number is optional) of the Couchbase cluster / node\n" +
            "    --bucket, -b  - name of the bucket on the Couchbase cluster / node\n" +
            "    --query, -q   - Couchbase-compliant N1QL query surrounded by single or double quotes\n" +
            "\n" +
            "  examples:\n" +
            "    ./runCouchbaseClient.sh --host 192.168.99.100:8091 --bucket sapi --query \"select * from sapi limit 5\" \n" +
            "\n" +
            "  or \n" +
            "\n" +
            "    ./runCouchbaseClient.sh --host 172.31.29.132 --bucket Transport --query 'select * from Transport limit 5'\n" +
            "\n" +
            "  or \n" +
            "\n" +
            "./runCouchbaseClient.sh -h 172.31.29.132 -b Transport -q \"select * from Transport limit 5\" > queryResults.log\n" +
            "\n",
            COUCHBASE_CLIENT_PROGRAM_NAME, COUCHBASE_CLIENT_PROGRAM_NAME);
  }
}
