package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.maps.errors.ApiException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.YouTubeVideo;
import com.google.sps.utils.TimeUtils;
import com.google.sps.utils.VideoUtils;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
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
  private int planLength;
  private String amount = "";
  private String unit = "";
  private static final int videosDisplayedTotal = 25;
  private static final int videosDisplayedPerPage = 5;

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

    output = "Here are videos for: ";
    if (!workoutLength.equals(" ")) {
      output += workoutLength + " ";
    }
    if (!workoutType.equals("")) {
      output += workoutType + " ";
    }
    output += "workouts ";
    if (!youtubeChannel.equals("")) {
      output += "from " + youtubeChannel;
    }
  }

  /**
   * Private setworkoutFindDisplay method, that sets the agent display to JSON string by making YT
   * Data API call from VideoUtils to get passed into workout.js
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

  /**
   * Private workoutPLan method, makes and displays a workout specified by user request. Method sets
   * parameters for planLength and workoutType based on Dialogflow detection and makes calls to set
   * display and set output. parameters map needs to include duration struct to set int planLength
   * and String workoutType
   *
   * @param parameters parameter Map from Dialogflow
   */
  private void workoutPlan(Map<String, Value> parameters) throws IOException {
    log.info(String.valueOf(parameters));

    Struct durationStruct = parameters.get("date-time").getStructValue();
    Map<String, Value> durationMap = durationStruct.getFieldsMap();
    try {
      Date start = TimeUtils.stringToDate(durationMap.get("startDateTime").getStringValue());
      Date end = TimeUtils.stringToDate(durationMap.get("endDateTime").getStringValue());
      // Convert milliseconds to days
      planLength = (int) ((end.getTime() - start.getTime()) / 86400000);

      if (planLength < 1 || planLength > 30) {
        output =
            "Sorry, unable to make a workout plan for less than 1 day or more than 30 days. Please try again.";
      } else {
        // Set output
        setWorkoutPlanOutput();

        // Set display
        setWorkoutPlanDisplay();
      }
    } catch (ParseException e) {
      System.err.println("Unable to parse date format.");
    }
  }

  /**
   * Private setworkoutPLanOutput method, that sets the agent output based on set parameters for
   * planLength and workoutType from workoutPlan method
   */
  private void setWorkoutPlanOutput() {
    output = "Here is your " + planLength + " day " + workoutType + " workout plan:";
  }

  /**
   * Private setworkoutPLanDisplay method, that sets the agent display to JSON string by making YT
   * Data API call from VideoUtils to get passed into workout.js
   */
  private void setWorkoutPlanDisplay() throws IOException {

    // Removing white space so search URL does not have spaces
    workoutType = workoutType.replaceAll("\\s", "");

    // Make API call to WorkoutUtils to get json object of videos
    List<YouTubeVideo> videoList =
        VideoUtils.getPlaylistVideoList(planLength, workoutType, "playlist");

    display = "TODO";
  }
}
