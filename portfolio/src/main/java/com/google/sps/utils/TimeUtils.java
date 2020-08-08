/*
 * Copyright 2019 Google Inc.
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

package com.google.sps.utils;

import com.google.protobuf.Value;
import com.google.sps.data.Pair;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtils {

  private static Logger log = LoggerFactory.getLogger(TimeUtils.class);

  /**
   * Converts a string representation ("yyyy-MM-dd'T'HH:mm:ssXXX") of date into a Date object.
   *
   * @param dateString String representation of date in the form "yyyy-MM-dd'T'HH:mm:ssXXX"
   * @param return Date object equivalent to the date represented in the string input.
   */
  public static Date stringToDate(String dateString) throws ParseException {
    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
    Date parsed = parser.parse(dateString);
    return parsed;
  }

  /**
   * Converts a long representing ms since 1970 into a date string.
   *
   * @param milliseconds long representing milliseconds since 1970
   * @param return String representing the input time as a formatted date string.
   */
  public static String secondsToDateString(long milliseconds) throws ParseException {
    Date date = new Date(milliseconds);
    SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH);
    return parser.format(date);
  }

  /**
   * Converts an integer in seconds into a string of hours, minutes, and seconds
   *
   * @param duration An int representing the number of seconds.
   * @param return A string containing the input integer converted into "HH hours, MM minutes, and
   *     SS seconds"
   */
  public static String secondsToHMSString(int duration) throws IllegalArgumentException {
    if (duration < 1) {
      throw new IllegalArgumentException("Duration input cannot be less than 1 second.");
    } else if (duration == 1) {
      return "1 second";
    } else if (duration < 60) {
      return String.valueOf(duration) + " seconds";
    } else if (duration == 60) {
      return "1 minute";
    } else if (duration < 3600) {
      return String.valueOf(duration / 60)
          + " minutes and "
          + String.valueOf(duration % 60)
          + " seconds";
    } else if (duration == 3600) {
      return "1 hour";
    } else {
      String remainder;
      try {
        remainder = secondsToHMSString(duration % 3600);
        if (!remainder.contains("and")) {
          remainder = "and " + remainder;
        } else {
          remainder = " " + remainder;
        }
      } catch (IllegalArgumentException e) {
        remainder = "";
      }
      return String.valueOf(duration / 3600) + " hours" + remainder;
    }
  }

  /**
   * Converts an integer in seconds into a string of "HH:MM:SS"
   *
   * @param duration An int representing the number of seconds.
   * @param return A string containing the input integer converted into "HH:MM:SS"
   */
  public static String makeClockDisplay(int duration) {
    int hours = duration / 3600;
    int min = (duration - hours * 3600) / 60;
    int sec = duration - hours * 3600 - min * 60;
    if (hours == 0) {
      return String.valueOf(min) + ":" + doubleDigitString(sec);
    } else {
      return String.valueOf(hours) + ":" + doubleDigitString(min) + ":" + doubleDigitString(sec);
    }
  }

  /**
   * Converts a single-digit integer into a double-digit number by adding a 0 to the front
   *
   * @param num input integer
   * @return a number that is at least two digits
   */
  public static String doubleDigitString(int num) {
    if (num >= 10) {
      return String.valueOf(num);
    } else {
      return "0" + String.valueOf(num);
    }
  }

  /**
   * Unpacks date object from "date-time-enhanced" entity to retrieve a start and end pair that
   * represents the length of time that the user specified.
   *
   * @param dateObject Value from dialogflow's retrieved parameters
   * @return Pair where key represents start time of duration and value represents end time
   */
  public static Pair<Long, Long> getTimeRange(Value dateObject) throws ParseException {
    String startDateString;
    String endDateString;
    Value dateTimeObject = dateObject.getStructValue().getFieldsMap().get("date-time");
    if (dateTimeObject.hasStructValue()) {
      Map<String, Value> durationMap = dateTimeObject.getStructValue().getFieldsMap();
      if (durationMap.get("date-time") != null) {
        // Case where user specifies a specific date and time (should return a 10 min period
        // centered around the time)
        Date dateTime = stringToDate(durationMap.get("date-time").getStringValue());
        return new Pair(dateTime.getTime() - 300000, dateTime.getTime() + 300000);
      }
      // Case where user specifies a time duration.
      startDateString = durationMap.get("startDate").getStringValue();
      endDateString = durationMap.get("endDate").getStringValue();
    } else {
      // Case where user asks for a date but no time (should return a full day period)
      String dateString = dateTimeObject.getStringValue();
      startDateString = dateString.replaceAll("T([0-9]{2}:){2}[0-9]{2}", "T00:00:00");
      endDateString = dateString.replaceAll("T([0-9]{2}:){2}[0-9]{2}", "T23:59:59");
    }
    Date start = stringToDate(startDateString);
    Date end = stringToDate(endDateString);
    return new Pair(start.getTime(), end.getTime());
  }
}
