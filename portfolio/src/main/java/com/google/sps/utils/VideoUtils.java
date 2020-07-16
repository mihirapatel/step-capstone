package com.google.sps.utils;

import com.google.gson.Gson;
import com.google.sps.data.YouTubeVideo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoUtils {

  private static String URL;
  private static String maxResults;
  private static String q;
  private static String type;
  private static String key;
  private static final int videosDisplayedTotal = 25;
  private static final int videosDisplayedPerPage = 5;
  private static YouTubeVideo video;
  private static String channelTitle;
  private static String title;
  private static String description;
  private static String thumbnail;
  private static String videoId;
  private static String channelId;
  private static int currentPage = 0;
  private static int totalPages = videosDisplayedTotal / videosDisplayedPerPage;

  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  /**
   * Creates JSON object from url passed in from getJSONObject
   *
   * @param url for YouTube Data API search by keyword
   */
  private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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

  /**
   * Sets YouTube Data API search by keyword parameters, creates URL, and passes URL into
   * readJsonFromURL
   *
   * @param workoutLength for workout video length
   * @param planLength for workout plan length
   * @param workoutType for workout video/playlist muscle/type
   * @param youtubeChannel for workout channel
   * @param numVideosSearched for number of videos to get from search
   * @param searchType type of search on YouTube (video or playlist)
   */
  public static List<YouTubeVideo> getVideoList(
      String workoutLength,
      String workoutType,
      String youtubeChannel,
      int numVideosSearched,
      String searchType)
      throws IOException, JSONException {

    maxResults = setMaxResults(numVideosSearched);
    q = setVideoQ(workoutLength, workoutType, youtubeChannel);
    type = setType(searchType);
    key = setKey();
    URL = setURL(maxResults, q, type, key);
    JSONObject json = readJsonFromUrl(URL);
    List<YouTubeVideo> videoList = createVideoList(json);
    return videoList;
  }

  //   /**
  //    * Sets YouTube Data API search by keyword parameters, creates URL, and passes URL into
  //    * readJsonFromURL
  //    *
  //    * @param workoutLength for workout video length
  //    * @param planLength for workout plan length
  //    * @param workoutType for workout video/playlist muscle/type
  //    * @param youtubeChannel for workout channel
  //    * @param numVideosSearched for number of videos to get from search
  //    * @param numPlaylistsSearched for number of videos to get from search
  //    */
  //   public List<YouTubeVideo> getVideoList(
  //       String workoutLength, String planLength, String workoutType, String youtubeChannel, int
  // numVideosSearched, int numPlaylistsSearched, String searchType)
  //       throws IOException, JSONException {

  //     if (searchType.equals("video")) {
  //       maxResults = setMaxResults(numVideosSearched);
  //       q = setVideoQ(workoutLength, workoutType, youtubeChannel);
  //     } else if (searchType.equals("playlist")) {
  //       maxResults = setMaxResults(numPlaylistsSearched);
  //       q = setPlaylistQ(planLength, workoutType);
  //     }

  //     type = setType(searchType);
  //     key = setKey();
  //     URL = setURL(maxResults, q, type, key);
  //     JSONObject json = readJsonFromUrl(URL);

  //     if (searchType.equals("video")) {
  //         return createVideoList(json);
  //     }
  //     return null;
  //   }

  private static String setMaxResults(int videosDisplayedTotal) {
    return "maxResults=" + String.valueOf(videosDisplayedTotal);
  }

  private static String setVideoQ(String workoutLength, String workoutType, String youtubeChannel) {
    return "q=" + String.join("+", workoutLength, workoutType, youtubeChannel, "workout");
  }

  private static String setPlaylistQ(String planLength, String workoutType) {
    return "q=" + String.join("+", planLength, workoutType, "workout", "challenge");
  }

  private static String setType(String searchType) {
    return "type=" + searchType;
  }

  private static String setKey() throws IOException {
    String apiKey =
        new String(
            Files.readAllBytes(
                Paths.get(VideoUtils.class.getResource("/files/youtubeAPIKey.txt").getFile())));
    return "key=" + apiKey;
  }

  private static String setURL(String maxResults, String q, String type, String key) {
    String baseURL = "https://www.googleapis.com/youtube/v3/search?part=snippet";
    return String.join("&", baseURL, maxResults, q, type, key);
  }

  private static List<YouTubeVideo> createVideoList(JSONObject json) {
    JSONArray videos = json.getJSONArray("items");

    List<YouTubeVideo> videoList = new ArrayList<>();

    for (int index = 0; index < videos.length(); index++) {
      String videoString = new Gson().toJson(videos.get(index));
      setVideoParameters(videoString);
      if (index % videosDisplayedPerPage == 0) {
        currentPage += 1;
      }
      video =
          new YouTubeVideo(
              channelTitle,
              title,
              description,
              thumbnail,
              videoId,
              channelId,
              index,
              videosDisplayedPerPage,
              currentPage,
              totalPages);
      videoList.add(video);
    }

    return videoList;
  }

  /**
   * Sets parameters: channelTitle, title, description, thumbnail, videoId, channelId for YouTube
   * video object
   *
   * @param videoString JSON string of YouTube video from API call
   */
  private static void setVideoParameters(String videoString) {
    JSONObject videoJSONObject = new JSONObject(videoString).getJSONObject("map");
    JSONObject id = videoJSONObject.getJSONObject("id").getJSONObject("map");
    videoId = new Gson().toJson(id.get("videoId"));
    JSONObject snippet = videoJSONObject.getJSONObject("snippet").getJSONObject("map");
    title = new Gson().toJson(snippet.get("title"));
    description = new Gson().toJson(snippet.get("description"));
    channelTitle = new Gson().toJson(snippet.get("channelTitle"));
    channelId = new Gson().toJson(snippet.get("channelId"));
    JSONObject thumbnailJSONObject =
        snippet.getJSONObject("thumbnails").getJSONObject("map").getJSONObject("medium");
    JSONObject thumbnailURL = thumbnailJSONObject.getJSONObject("map");
    thumbnail = new Gson().toJson(thumbnailURL.get("url"));
  }
}
