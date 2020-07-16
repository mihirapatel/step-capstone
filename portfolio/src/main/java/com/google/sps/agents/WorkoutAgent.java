package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.maps.errors.ApiException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.YouTubeVideo;
import com.google.sps.utils.VideoUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workout Agent: finds relevant workout videos through Youtube and schedules workout plans. Also
 * tracks workouts for user.
 */
public class WorkoutAgent implements Agent {

  private static Logger log = LoggerFactory.getLogger(WorkoutAgent.class);

  private final String intentName;
  private String output = null;
  private String display = null;
  private String redirect = null;
  private String workoutType = "";
  private String workoutLength = "";
  private String youtubeChannel = "";
  private final int videosDisplayedTotal = 25;
  private final int videosDisplayedPerPage = 5;
  private String amount = "";
  private String unit = "";
  private YouTubeVideo video;
  private String channelTitle;
  private String title;
  private String description;
  private String thumbnail;
  private String videoId;
  private String channelId;
  private int currentPage = 0;
  private int totalPages = videosDisplayedTotal / videosDisplayedPerPage;

  public WorkoutAgent(String intentName, Map<String, Value> parameters)
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
    } else if (intentName.contains("plan")) {
      workoutPlan(parameters);
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

  /**
   * Private workoutFind method, that displays workouts specified by user request. Method sets
   * parameters for workoutLength, workoutType, and youtubeChannel based on Dialogflow detection and
   * makes calls to set display and set output. parameters map needs to include duration struct to
   * set String workoutLength, String workoutType, and String youtubeChannel
   *
   * @param parameters parameter Map from Dialogflow
   */
  private void workoutFind(Map<String, Value> parameters) throws IOException {
    log.info(String.valueOf(parameters));

    if (parameters.get("duration").hasStructValue()) {
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

  /**
   * Private setworkoutFindOutput method, that sets the agent output based on set parameters for
   * workoutLength, workoutType, and youtubeChannel from workoutFind method
   */
  private void setWorkoutFindOutput() {
    output = "Here are videos for: " + workoutLength + " " + workoutType + " workouts";
    if (!youtubeChannel.equals("")) {
      output += " from " + youtubeChannel;
    }
  }

  /**
   * Private setworkoutFindDisplay method, that sets the agent display to JSON string by makifn YT
   * Data API call from WorkoutUtils to get passed into workout.js
   */
  private void setWorkoutFindDisplay() throws IOException {

    // Removing white space so search URL does not have spaces
    workoutLength = workoutLength.replaceAll("\\s", "");
    workoutType = workoutType.replaceAll("\\s", "");
    youtubeChannel = youtubeChannel.replaceAll("\\s", "");

    // Make API call to WorkoutUtils to get json object of videos
    List<YouTubeVideo> videoList =
        VideoUtils.getVideoList(
            workoutLength, workoutType, youtubeChannel, videosDisplayedTotal, "video");

    display = new Gson().toJson(videoList);
  }

  /** @param parameters parameter Map from Dialogflow */
  private void workoutPlan(Map<String, Value> parameters) throws UnsupportedOperationException {
    return;
  }
}
