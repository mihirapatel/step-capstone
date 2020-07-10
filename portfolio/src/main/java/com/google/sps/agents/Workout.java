package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.maps.errors.ApiException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Video;
import com.google.sps.utils.WorkoutUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workout Agent: finds relevant workout videos through Youtube and schedules workout plans. Also
 * tracks workouts for user.
 */
public class Workout implements Agent {

  private static Logger log = LoggerFactory.getLogger(Workout.class);

  private final String intentName;
  private String output = null;
  private String display = null;
  private String redirect = null;
  private String workoutType = "";
  private String workoutLength = "";
  private String youtubeChannel = "";
  private final int numVideos = 5;
  private String amount = "";
  private String unit = "";
  private Video video;
  private String channelTitle;
  private String title;
  private String description;
  private String thumbnail;
  private String videoId;

  public Workout(String intentName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    if (intentName.contains("find")) {
      workoutFind(parameters);
    } else if (intentName.contains("schedule")) {
      workoutSchedule(parameters);
    }
  }

  @Override
  public String getOutput() {
    return output;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  @Override
  public String getRedirect() {
    return redirect;
  }

  private void workoutFind(Map<String, Value> parameters) throws IOException {
    log.info(String.valueOf(parameters));
    String duration = parameters.get("duration").getStringValue();
    if (!duration.equals("")) {
      Struct durationStruct = parameters.get("duration").getStructValue();
      Map<String, Value> durationMap = durationStruct.getFieldsMap();
      amount = String.valueOf(Math.round(durationMap.get("amount").getNumberValue()));
      unit = durationMap.get("unit").getStringValue();
    }

    workoutLength = amount + " " + unit;
    workoutType = parameters.get("workout-type").getStringValue();
    youtubeChannel = parameters.get("youtube-channel").getStringValue();

    // Set output
    setWorkoutFindOutput();

    // Set display
    setWorkoutFindDisplay();
  }

  private void setWorkoutFindOutput() {
    output = "Here are videos for: " + workoutLength + " " + workoutType + " workouts";
    if (!youtubeChannel.equals("")) {
      output += " from " + youtubeChannel;
    }
  }

  private void setWorkoutFindDisplay() throws IOException {
    // Make API call to WorkoutUtils to get json object of videos
    workoutLength = workoutLength.replaceAll("\\s", "");
    workoutType = workoutType.replaceAll("\\s", "");
    youtubeChannel = youtubeChannel.replaceAll("\\s", "");
    JSONObject json =
        WorkoutUtils.getJSONObject(workoutLength, workoutType, youtubeChannel, numVideos);
    JSONArray videos = json.getJSONArray("items");

    List<Video> videoList = new ArrayList<>();

    for (int i = 0; i < numVideos; i++) {
      String videoString = new Gson().toJson(videos.get(i));
      setVideoParameters(videoString);
      video = new Video(channelTitle, title, description, thumbnail, videoId);
      videoList.add(video);
    }

    display = new Gson().toJson(videoList);
  }

  private void setVideoParameters(String videoString) {
    JSONObject videoJSONObject = new JSONObject(videoString).getJSONObject("map");
    JSONObject id = videoJSONObject.getJSONObject("id").getJSONObject("map");
    videoId = new Gson().toJson(id.get("videoId"));
    JSONObject snippet = videoJSONObject.getJSONObject("snippet").getJSONObject("map");
    description = new Gson().toJson(snippet.get("description"));
    title = new Gson().toJson(snippet.get("title"));
    channelTitle = new Gson().toJson(snippet.get("channelTitle"));
    JSONObject thumbnailJSONObject =
        snippet.getJSONObject("thumbnails").getJSONObject("map").getJSONObject("medium");
    JSONObject thumbnailURL = thumbnailJSONObject.getJSONObject("map");
    thumbnail = new Gson().toJson(thumbnailURL.get("url"));
  }

  private void workoutSchedule(Map<String, Value> parameters) {
    return;
  }
}
