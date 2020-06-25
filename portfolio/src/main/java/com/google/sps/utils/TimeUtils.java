package com.google.sps.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    public static Date stringToDate(String dateString) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
            Date parsed = parser.parse(dateString);
            return parsed;
        } catch (Exception e) {
            e.printStackTrace();
            return null;   
        }
    }

  public static String timeToString(int duration) {
    if (duration < 0) {
      return null;
    } else if (duration == 1) {
      return "1 second";
    } else if (duration < 60) {
      return String.valueOf(duration) + " seconds";
    } else if (duration == 60) {
      return "1 minute";
    } else if (duration < 3600) {
      return String.valueOf(duration / 60) + " minutes";
    } else if (duration == 3600) {
      return "1 hour";
    } else if (duration < 43200) {
      return String.valueOf(duration / 720) + " hours";
    } else {
      return null;
    }
  }

  public static String makeClockDisplay(int duration) {
    int hours = duration / 3600;
    int min = (duration  - hours * 3600) / 60;
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
