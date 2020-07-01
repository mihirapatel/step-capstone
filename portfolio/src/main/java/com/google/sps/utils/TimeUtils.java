package com.google.sps.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

  public static Date stringToDate(String dateString) throws ParseException {
    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
    Date parsed = parser.parse(dateString);
    return parsed;
  }

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

  public static String doubleDigitString(int num) {
    if (num >= 10) {
      return String.valueOf(num);
    } else {
      return "0" + String.valueOf(num);
    }
  }
}
