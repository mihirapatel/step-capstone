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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Date Agent handles users' requests for date information. It determines appropriate outputs and
 * display information to send to the user interface based on Dialogflow's detected Date intents.
 */
public class DateAgent implements Agent {
  private final String intentName;
  private String output = null;
  private String locationFormatted;
  private String locationDisplay;
  private ZonedDateTime dateGiven;

  /**
   * Date agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public DateAgent(String intentName, Map<String, Value> parameters)
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
    if (intentName.equals("get") || intentName.equals("context:date")) {
      this.locationFormatted = LocationUtils.getFormattedAddress("location", parameters);
      this.locationDisplay = LocationUtils.getDisplayAddress("location", parameters);

      String currentDay = getCurrentDateString(locationFormatted);
      if (!currentDay.isEmpty()) {
        output = "It is " + currentDay + " in " + locationDisplay + ".";
      }
    } else if (intentName.contains("day-of-date")) {
      this.locationFormatted = "United States";
      this.dateGiven = TimeAgent.getZonedTime("date-time", locationFormatted, parameters);
      LocalDateTime givenDate = dateGiven.toLocalDateTime();
      LocalDateTime currentDate = getCurrentDate(locationFormatted).toLocalDateTime();

      if (dateGiven != null) {
        String dateString = getDateString(dateGiven);
        String dayOfWeek = getDayOfWeek(dateGiven);
        String verb = " was a ";
        if (currentDate.isBefore(givenDate)) {
          verb = " is a ";
        }
        output = dateString + verb + dayOfWeek + ".";
      }
    }
  }

  public ZonedDateTime getCurrentDate(String locationName)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime currentTime = null;
    Location place = Location.create(locationName);
    String timeZoneID = place.getTimeZoneID();
    currentTime = ZonedDateTime.now(ZoneId.of(timeZoneID));
    return currentTime;
  }

  /**
   * Gets a String of the current date in the specified location.
   *
   * @param location name of location
   * @return String specifying current date in location
   */
  public String getCurrentDateString(String location)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime date = getCurrentDate(location);
    return zonedTimeToString(date);
  }

  /**
   * Gets a String of the date of the ZonedDateTime object.
   *
   * @param time object containing date to retrieve String of
   * @return String specifying the ZonedDateTime date
   */
  public String zonedTimeToString(ZonedDateTime date) {
    String dateString = "";
    if (date != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d uuuu");
      dateString = date.format(formatter);
    }
    return dateString;
  }

  /**
   * Gets the day of the week from a ZonedDateTime object.
   *
   * @param date object containing the date
   * @return String specifying the weekday of the date
   */
  public String getDayOfWeek(ZonedDateTime date) {
    String dateString = "";
    if (date != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");
      dateString = date.format(formatter);
    }
    return dateString;
  }

  /**
   * Gets a String of the date of the ZonedDateTime object.
   *
   * @param date object containing date to retrieve String of
   * @return String specifying the ZonedDateTime
   */
  public String getDateString(ZonedDateTime date) {
    String dateString = "";
    if (date != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, uuuu");
      dateString = date.format(formatter);
    }
    return dateString;
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
}
