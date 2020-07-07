package com.google.sps.utils;

import com.google.gson.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.apache.http.*;
import org.apache.http.client.fluent.Request;

public class CivicUtils {

  public static void getCurl() throws Exception {
    // The fluent API relieves the user from having to deal with manual
    // deallocation of system resources at the cost of having to buffer
    // response content in memory in some cases.
    String apiKey =
        new String(
            Files.readAllBytes(
                Paths.get(CivicUtils.class.getResource("/files/apikey.txt").getFile())));
    String address = String.join("%20", "1263 Pacific Ave. Kansas City KS".split(" "));
    String electionId = "2000";
    // 1263%20Pacific%20Ave.%20Kansas%20City%20KS
    String electionCurl =
        "https://www.googleapis.com/civicinfo/v2/voterinfo?key="
            + apiKey
            + "&address="
            + address
            + "&electionId="
            + electionId;

    HashMap<String, Object> responseMap =
        new Gson()
            .fromJson(
                Request.Get(electionCurl).execute().returnContent().toString(), HashMap.class);
    System.out.println(responseMap);
  }
}
