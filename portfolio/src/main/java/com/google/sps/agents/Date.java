package com.google.sps.agents;

// Imports the Google Cloud client library
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

/** Date Agent */
public class Date implements Agent {
  private final String intentName;
  private String output = null;
  private String locationFormatted;
  private String locationDisplay;
  private ZonedDateTime dateGiven;

  public Date(String intentName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
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
      this.dateGiven = Time.getZonedTime("date-time", locationFormatted, parameters);
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

  public ZonedDateTime getCurrentDate(String locationName)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime currentTime = null;
    Location place = LocationUtils.getLocationObject(locationName);
    String timeZoneID = place.getTimeZoneID();
    currentTime = ZonedDateTime.now(ZoneId.of(timeZoneID));
    return currentTime;
  }

  public String getCurrentDateString(String location)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    ZonedDateTime date = getCurrentDate(location);
    return zonedTimeToString(date);
  }

  public String zonedTimeToString(ZonedDateTime date) {
    String dateString = "";
    if (date != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d uuuu");
      dateString = date.format(formatter);
    }
    return dateString;
  }

  public String getDayOfWeek(ZonedDateTime date) {
    String dateString = "";
    if (date != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");
      dateString = date.format(formatter);
    }
    return dateString;
  }

  public String getDateString(ZonedDateTime date) {
    String dateString = "";
    if (date != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, uuuu");
      dateString = date.format(formatter);
    }
    return dateString;
  }
}
