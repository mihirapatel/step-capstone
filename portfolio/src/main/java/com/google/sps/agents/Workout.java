package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.maps.errors.ApiException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workout Agent: finds relevant workout videos through Youtube and schedules workout plans. Also
 * tracks workouts for user.
 */
public class Workout implements Agent {

  private static Logger log = LoggerFactory.getLogger(Workout.class);

  private final String intentName;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private String workoutType;
  private String workoutLength;
  private String youtubeChannel;

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
    log.info(String.valueOf(parameters));
    if (intentName.contains("find")) {
      workoutFind(parameters);
    } else if (intentName.contains("schedule")) {
      workoutSchedule(parameters);
    }
  }

  @Override
  public String getOutput() {
    return fulfillment;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  @Override
  public String getRedirect() {
    return redirect;
  }

  private void workoutFind(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    Struct durationStruct = parameters.get("duration").getStructValue();
    Map<String, Value> durationMap = durationStruct.getFieldsMap();
    int amount = (int) durationMap.get("amount").getNumberValue();
    String unit = durationMap.get("unit").getStringValue();

    workoutLength = " " + String.valueOf(amount) + " " + unit;
    workoutType = parameters.get("workout-type").getStringValue();
    youtubeChannel = parameters.get("youtube-channel").getStringValue();

    fulfillment = "Here are videos for:" + workoutLength + " " + workoutType + " workout";
    if (!youtubeChannel.equals("")) {
      fulfillment += " from " + youtubeChannel;
    }
  }

  private void workoutFind(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    return;
  }
}
