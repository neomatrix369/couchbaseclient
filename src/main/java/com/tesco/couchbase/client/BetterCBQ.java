package com.tesco.couchbase.client;

import static com.couchbase.client.java.CouchbaseCluster.create;
import static com.couchbase.client.java.query.N1qlQuery.simple;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

public class BetterCBQ {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    if (args.length == 0) {
      System.out.println(getUsageText());
      System.exit(-1);
    }

    BetterCBQ.execute(args[0], args[1], args[2]);
  }

  private static String getUsageText() {
    return
        "usage: couchbaseClient \"couchBaseHost:[port]\"  \"bucketName\" \"sql query\" \n" +
        "  required parameters:\n" +
        "    couchBaseHost[:port]   - name or IP address (port number is optional) of the Couchbase cluster / node\n" +
        "    bucketName             - name of the bucket on the Couchbase cluster / node\n" +
        "    sql query              - Couchbase-compliant sql query surrounded by single or double quotes\n" +
        "\n" +
        "  examples:\n" +
        "    couchbaseClient 192.168.99.100:8091 sapi \"select * from sapi limit 5\"";
  }

  public static void execute(String couchBaseHost, String bucketName, String queryString)
      throws IOException, ExecutionException, InterruptedException {
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
          System.exit(-1);
      }
    }
  }

  private static boolean displayQueryResults(String queryString, N1qlQueryResult queryResult) {
    boolean errorOccurred = false;
    if (queryResult.finalSuccess()) {
      for (N1qlQueryRow eachRow : queryResult.allRows()) {
        System.out.println(convertToPrettyJsonString(eachRow));
      }
    } else {
      errorOccurred = true;
      System.err.println("Query did not execute successfully, due to one or more errors.");
      System.err.format("Requested query: '%s'%n", queryString);
      System.err.println(queryResult.errors());
    }
    return errorOccurred;
  }

  private static String convertToPrettyJsonString(N1qlQueryRow eachRow) {
    String jsonAsString = eachRow.value().toString();
    JSONObject json = new JSONObject(jsonAsString);
    return json.toString(2);
  }
}
