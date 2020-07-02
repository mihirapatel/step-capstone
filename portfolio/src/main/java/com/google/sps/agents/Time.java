package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import com.google.sps.utils.LocationUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Time Agent */
public class Time implements Agent {
  private final String intentName;
  private String output = null;
  private String locationFormatted;
  private String locationDisplay;
  private String locationToFormatted;
  private String locationToDisplay;
  private String locationFromFormatted;
  private String locationFromDisplay;
  private ZonedDateTime timeFrom;

  public Time(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
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

  public ZonedDateTime getCurrentTime(String locationName) {
    ZonedDateTime currentTime = null;
    Location place = new Location(locationName);
    String timeZoneID = place.getTimeZoneID();
    currentTime = ZonedDateTime.now(ZoneId.of(timeZoneID));
    return currentTime;
  }

  public String getZone(String locationName) {
    String timeZone = null;
    Location place = new Location(locationName);
    ZonedDateTime time = getCurrentTime(locationName);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("z");
    timeZone = time.format(formatter);
    return place.getTimeZoneName() + " (" + timeZone + ")";
  }

  public String getTimeDiff(String firstLocation, String secondLocation) {
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

  public ZonedDateTime getTimeIn(String locationIn, ZonedDateTime timeFromObject) {
    ZonedDateTime timeIn = null;
    Location placeTo = new Location(locationIn);
    String timeZoneID = placeTo.getTimeZoneID();
    timeIn = timeFromObject.withZoneSameInstant(ZoneId.of(timeZoneID));
    return timeIn;
  }

  public String getCurrentTimeString(String location) {
    ZonedDateTime time = getCurrentTime(location);
    return zonedTimeToString(time);
  }

  public String zonedTimeToString(ZonedDateTime time) {
    String timeString = "";
    if (time != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
      timeString = time.format(formatter);
    }
    return timeString;
  }

  public static ZonedDateTime getZonedTime(
      String timeName, String locationParameter, Map<String, Value> parameters) {
    LocalDateTime localTime = getTimeParameter(timeName, parameters);
    ZonedDateTime zonedTime = null;
    if (localTime != null) {
      Location place = new Location(locationParameter);
      String timeZoneID = place.getTimeZoneID();
      zonedTime = ZonedDateTime.of(localTime, ZoneId.of(timeZoneID));
    }
    return zonedTime;
  }

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
