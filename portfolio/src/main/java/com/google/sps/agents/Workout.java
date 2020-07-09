package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.maps.errors.ApiException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Video;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
  private String output = null;
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

    // Set output
    output = "Here are videos for:" + workoutLength + " " + workoutType + " workout";
    if (!youtubeChannel.equals("")) {
      output += " from " + youtubeChannel;
    }

    // TODO: make API call to WorkoutUtils to get json object of videos

    // Set display
    Video video1 =
        new Video(
            "MadFit",
            "15 min FULL BODY Fat Burn HIIT Workout (No Equipment)",
            "An equipment free, beginner friendly, full body, fat burning HIIT style workout to get you nice and sweaty!",
            "https://i.ytimg.com/vi/dxA21IeBB8o/hqdefault.jpg",
            "dxA21IeBB8o");
    Video video2 =
        new Video(
            "MadFit",
            "FULL BODY FAT BURNING HIIT (15 min At Home Workout)",
            "An intense full body, fat burning, HIIT style, at home workout! No equipment needed, but the use one 2 light dumbbells will increase the intensity.",
            "https://i.ytimg.com/vi/JMtE0rl21Fg/hqdefault.jpg",
            "JMtE0rl21Fg");
    Video video3 =
        new Video(
            "MadFit",
            "FULL BODY FAT BURN HIIT (At Home No Equipment)",
            "An intense full body, fat burning, HIIT style, at home workout! No equipment needed.",
            "https://i.ytimg.com/vi/Hc7V7MJCTc8/hqdefault.jpg",
            "Hc7V7MJCTc8");

    List<Video> videoList = new ArrayList<>();
    videoList.add(video1);
    videoList.add(video2);
    videoList.add(video3);
    display = new Gson().toJson(videoList);
  }

  private void workoutSchedule(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    return;
  }
}
