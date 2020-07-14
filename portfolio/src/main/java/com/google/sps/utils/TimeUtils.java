package com.google.sps.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

  /**
   * Converts a string representation ("yyyy-MM-dd'T'HH:mm:ssXXX") of date into a Date object.
   *
   * @param dateString String representation of date in the form "yyyy-MM-dd'T'HH:mm:ssXXX"
   * @param return Date object equivalent to the date represented in the string input.
   */
  public static Date stringToDate(String dateString) throws ParseException {
    dateString = dateString.replaceAll("[0-9]{2}:[0-9]{2}$", "00:00");
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
}
