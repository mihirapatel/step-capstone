/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.agents;

import com.google.maps.errors.ApiException;
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import com.google.sps.utils.LocationUtils;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Time Agent handles users' requests for time information. It determines appropriate outputs and
 * display information to send to the user interface based on Dialogflow's detected Time intents.
 */
public class TimeAgent implements Agent {
  private final String intentName;
  private String output = null;
  private String locationFormatted;
  private String locationDisplay;
  private String locationToFormatted;
  private String locationToDisplay;
  private String locationFromFormatted;
  private String locationFromDisplay;
  private ZonedDateTime timeFrom;

  /**
   * Time agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public TimeAgent(String intentName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.intentName = intentName;
    setParameters(parameters);
  }

  /**
   * Method that handles parameter assignment for fulfillment text and display based on the user's
   * input intent and extracted parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    if (intentName.equals("get") || intentName.equals("context:time")) {
      this.locationFormatted = LocationUtils.getFormattedAddress("location", parameters);
      this.locationDisplay = LocationUtils.getDisplayAddress("location", parameters);

      String currentTime = getCurrentTimeString(locationFormatted);
      if (!currentTime.isEmpty()) {
        output = "It is " + currentTime + " in " + locationDisplay + ".";
      }
    } else if (intentName.equals("check")) {
      this.locationFormatted = LocationUtils.getFormattedAddress("location", parameters);
      this.locationDisplay = LocationUtils.getDisplayAddress("location", parameters);

      String currentTime = getCurrentTimeString(locationFormatted);
      if (!currentTime.isEmpty()) {
        output = "In " + locationDisplay + ", it is currently " + currentTime + ".";
      }
    } else if (intentName.contains("convert")) {
      this.locationFromFormatted = LocationUtils.getFormattedAddress("location-from", parameters);
      this.locationFromDisplay = LocationUtils.getDisplayAddress("location-from", parameters);
      this.locationToFormatted = LocationUtils.getFormattedAddress("location-to", parameters);
      this.locationToDisplay = LocationUtils.getDisplayAddress("location-to", parameters);
      this.timeFrom = getZonedTime("time-from", locationFromFormatted, parameters);

      String timeToString = "";
      String timeFromString = "";
      if (timeFrom != null) {
        // Get time in locationTo based on time given in locationFrom
        timeToString = zonedTimeToString(getTimeIn(locationToFormatted, timeFrom));
        timeFromString = zonedTimeToString(timeFrom);
        output =
            "It's "
                + timeToString
                + " in "
                + locationToDisplay
                + " when it's "
                + timeFromString
                + " in "
                + locationFromDisplay
                + ".";
      } else {
        // Get current time in 2 different timezones
        timeFromString = getCurrentTimeString(locationFromFormatted);
        timeToString = getCurrentTimeString(locationToFormatted);
        output =
            "It is currently "
                + timeFromString
                + " in "
                + locationFromDisplay
                + " and "
                + timeToString
                + " in "
                + locationToDisplay
                + ".";
      }
      if (timeToString.isEmpty() || timeFromString.isEmpty()) {
        output = null;
      }
    } else if (intentName.contains("time_zones")) {
      this.locationFormatted = LocationUtils.getFormattedAddress("location", parameters);
      this.locationDisplay = LocationUtils.getDisplayAddress("location", parameters);

      String timezone = getZone(locationFormatted);
      if (!timezone.isEmpty()) {
        output = "The timezone in " + locationDisplay + " is " + timezone + ".";
      }
    } else if (intentName.contains("time_difference")) {
      this.locationFromFormatted = LocationUtils.getFormattedAddress("location-1", parameters);
      this.locationFromDisplay = LocationUtils.getDisplayAddress("location-1", parameters);
      this.locationToFormatted = LocationUtils.getFormattedAddress("location-2", parameters);
      this.locationToDisplay = LocationUtils.getDisplayAddress("location-2", parameters);

      String timeDiffString = getTimeDiff(locationFromFormatted, locationToFormatted);
      if (!timeDiffString.isEmpty()) {
        output = locationFromDisplay + " is " + timeDiffString + locationToDisplay + ".";
      }
    }
  }

  @Override
  public String getOutput() {
    return this.output;
  }

  @Override
  public String getDisplay() {
    return null;
  }

  @Override
  public String getRedirect() {
    return null;
  }

  /**
   * Gets a ZonedDateTime object of the current time based on a location.
   *
   * @param locationName name of location
   * @return ZonedDateTime of current time in location
   */
  public ZonedDateTime getCurrentTime(String locationName)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime currentTime = null;
    Location place = Location.create(locationName);
    String timeZoneID = place.getTimeZoneID();
    currentTime = ZonedDateTime.now(ZoneId.of(timeZoneID));
    return currentTime;
  }

  /**
   * Gets a time zone name based on a location, formatted: Pacific Standard Time (PST).
   *
   * @param locationName name of location
   * @return String time zone name
   */
  public String getZone(String locationName)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    String timeZone = null;
    Location place = Location.create(locationName);
    ZonedDateTime time = getCurrentTime(locationName);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("z");
    timeZone = time.format(formatter);
    return place.getTimeZoneName() + " (" + timeZone + ")";
  }

  /**
   * Gets a string identifying the time difference between two locations.
   *
   * @param locationName name of location
   * @param secondLocation name of second location
   * @return String indicating time difference
   */
  public String getTimeDiff(String firstLocation, String secondLocation)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime firstZonedTime = getCurrentTime(firstLocation);
    ZonedDateTime secondZonedTime = getTimeIn(secondLocation, firstZonedTime);
    LocalDateTime firstLocalTime = firstZonedTime.toLocalDateTime();
    LocalDateTime secondLocalTime = secondZonedTime.toLocalDateTime();

    Duration duration = Duration.between(secondLocalTime, firstLocalTime);
    int hours = (int) duration.toHours();
    int minutes = (int) duration.toMinutes() - (hours * 60);

    String hourString = String.valueOf(Math.abs(hours));
    String timeString = "";
    if (Math.abs(hours) == 1) {
      timeString += hourString + " hour";
    } else {
      timeString += hourString + " hours";
    }
    if (minutes != 0) {
      String minuteString = String.valueOf(Math.abs(minutes));
      timeString += " and " + minuteString + " minutes";
    }

    String ret = "";
    if (hours < 0) {
      ret += timeString + " behind ";
    } else if (hours == 0) {
      ret += "in the same time zone as ";
    } else {
      ret += timeString + " ahead of ";
    }
    return ret;
  }

  /**
   * Gets a ZonedDateTime object of the time in the specified location when it is the time indicated
   * in the timeFromObject.
   *
   * @param locationIn name of location
   * @param timeFromObject object containing time and zone to convert to
   * @return ZonedDateTime of time conversion
   */
  public ZonedDateTime getTimeIn(String locationIn, ZonedDateTime timeFromObject)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime timeIn = null;
    Location placeTo = Location.create(locationIn);
    String timeZoneID = placeTo.getTimeZoneID();
    timeIn = timeFromObject.withZoneSameInstant(ZoneId.of(timeZoneID));
    return timeIn;
  }

  /**
   * Gets a String of the current time in the specified location.
   *
   * @param location name of location
   * @return String specifying current time in location
   */
  public String getCurrentTimeString(String location)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime time = getCurrentTime(location);
    return zonedTimeToString(time);
  }

  /**
   * Gets a String of the time of the ZonedDateTime object.
   *
   * @param time object containing time to retrieve String of
   * @return String specifying the ZonedDateTime
   */
  public String zonedTimeToString(ZonedDateTime time) {
    String timeString = "";
    if (time != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
      timeString = time.format(formatter);
    }
    return timeString;
  }

  /**
   * Gets a ZonedDateTime object of the time specified by the location parameter when it is the time
   * specified by the timeName and parameters.
   *
   * @param timeName time name to get time parameter of
   * @param locationParameter location to calculate time conversion
   * @param parameters map of parameters
   * @return ZonedDateTime of local time in location
   */
  public static ZonedDateTime getZonedTime(
      String timeName, String locationParameter, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    LocalDateTime localTime = getTimeParameter(timeName, parameters);
    ZonedDateTime zonedTime = null;
    if (localTime != null) {
      Location place = Location.create(locationParameter);
      String timeZoneID = place.getTimeZoneID();
      zonedTime = ZonedDateTime.of(localTime, ZoneId.of(timeZoneID));
    }
    return zonedTime;
  }

  /**
   * Retrieves time parameter of the specified parameterName from the map parameters.
   *
   * @param timeName name of time parameter to get
   * @param parameters map of parameters
   * @return LocalDateTime object from parameters
   */
  public static LocalDateTime getTimeParameter(
      String parameterName, Map<String, Value> parameters) {
    String time = parameters.get(parameterName).getStringValue();
    LocalDateTime timeToCheck = null;

    if (!time.isEmpty()) {
      // Parses time string: "2020-06-24T16:25:00-04:00"
      String[] dateComponents = time.split("T")[0].split("\\-");
      int year = Integer.parseInt(dateComponents[0]);
      int month = Integer.parseInt(dateComponents[1]);
      int day = Integer.parseInt(dateComponents[2]);

      String[] timeComponents = time.split("T")[1].split("\\-")[0].split("\\:");
      int hour = Integer.parseInt(timeComponents[0]);
      int minute = Integer.parseInt(timeComponents[1]);
      timeToCheck = LocalDateTime.of(year, month, day, hour, minute);
    }
    return timeToCheck;
  }
}
