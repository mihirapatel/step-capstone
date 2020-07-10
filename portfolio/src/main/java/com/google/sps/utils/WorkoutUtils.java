package com.google.sps.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkoutUtils {

  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = new JSONObject(jsonText);
      return json;
    } finally {
      is.close();
    }
  }

  public static JSONObject getJSONObject(
      String workoutLength, String workoutType, String youtubeChannel, int numVideos)
      throws IOException, JSONException {
    String URL = "https://www.googleapis.com/youtube/v3/search?part=snippet";
    String maxResults = "maxResults=" + String.valueOf(numVideos);
    String q = "q=" + String.join("+", workoutLength, workoutType, youtubeChannel);
    String type = "type=video";
    String key = "key=AIzaSyBMMs48WLeVlD0aX2QPhpaiiIGsV_ntutA";
    URL = String.join("&", URL, maxResults, q, type, key);
    System.out.println(URL);
    JSONObject json = readJsonFromUrl(URL);
    return json;
  }
}
