package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Output;
import com.google.sps.data.Location;
import com.google.sps.agents.Agent;
import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
 
/**
 * Time Agent
 */
public class Time implements Agent {
    private final String intentName;
    private String output = null;
  	private String location;
    private String locationTo;
    private String locationFrom;
    private String locationOne;
    private String locationTwo;
    private ZonedDateTime timeFrom;
    
    public Time(String intentName, Map<String, Value> parameters) {
        this.intentName = intentName;
        try {
            setParameters(parameters);
        } catch (Exception e) {
            return;
        }
    }

	@Override 
	public void setParameters(Map<String, Value> parameters) {
        if (intentName.equals("get") || intentName.equals("context:time") || intentName.equals("check")){
            this.location = getLocationParameter("location", parameters);

            String currentTime = getCurrentTimeString(location);
            if (!currentTime.isEmpty()) {
                output = "It is " + currentTime + " in " + location + ".";
            }
        }
        else if (intentName.equals("check")) {
            String currentTime = getCurrentTimeString(location);
            if (!currentTime.isEmpty()) {
                output = "In " + location + ", it is currently " + currentTime + ".";
            }
        }
        else if (intentName.contains("convert")) {
            this.locationFrom = getLocationParameter("location-from", parameters);
            this.locationTo = getLocationParameter("location-to", parameters);
            this.timeFrom = getZonedTime("time-from", locationFrom, parameters);

            String timeToString = "";
            String timeFromString = "";
            if (timeFrom != null) {
                // Get time in locationTo based on time given in locationFrom
                timeToString = zonedTimeToString(getTimeIn(locationTo, timeFrom));
                timeFromString = zonedTimeToString(timeFrom);
                output = "It's " + timeToString + " in " + locationTo 
                        + " when it's " + timeFromString + " in " + locationFrom + ".";
            } else {
                // Get current time in 2 different timezones
                timeFromString = getCurrentTimeString(locationFrom);
                timeToString = getCurrentTimeString(locationTo);
                output = "It is currently " + timeFromString + " in " + locationFrom 
                        + " and " + timeToString + " in " + locationTo +".";
            }
            if (timeToString.isEmpty() || timeFromString.isEmpty()) {
                output = null;
            }
        }
        else if (intentName.contains("time_zones")) {
            this.location = getLocationParameter("location", parameters);

            String timezone = getZone(location);
            if (!timezone.isEmpty()) {
                output = "The timezone in "+ location + " is " + timezone + "."; 
            }
        }
        else if (intentName.contains("time_difference")) {
            this.locationOne = getLocationParameter("location-1", parameters);
            this.locationTwo = getLocationParameter("location-2", parameters);

            String timeDiffString = getTimeDiff(locationOne, locationTwo);
            if (! timeDiffString.isEmpty()) {
                output = locationOne + " is " + timeDiffString + locationTwo + "."; 
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
        ZonedDateTime time = getCurrentTime(locationName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("z");
        timeZone = time.format(formatter);
        return timeZone;
    }

    public String getTimeDiff(String firstLocation, String secondLocation) {
        ZonedDateTime firstZonedTime = getCurrentTime(firstLocation);
        ZonedDateTime secondZonedTime = getTimeIn(secondLocation, firstZonedTime);
        LocalDateTime firstLocalTime = firstZonedTime.toLocalDateTime();
        LocalDateTime secondLocalTime = secondZonedTime.toLocalDateTime();

        Duration duration = Duration.between(secondLocalTime, firstLocalTime);
        int hours = (int)duration.toHours();
        int minutes = (int)duration.toMinutes() - (hours * 60);

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
        }
        else if (hours == 0) {
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            timeString = time.format(formatter);
        }
        return timeString;
    }

    public ZonedDateTime getZonedTime(String timeName, String locationParameter, Map<String, Value> parameters) {
        LocalDateTime localTime = getTimeParameter(timeName, parameters);
        ZonedDateTime zonedTime = null;
        if (localTime != null) {
            Location place = new Location(locationParameter);
            String timeZoneID = place.getTimeZoneID();
            zonedTime = ZonedDateTime.of(localTime, ZoneId.of(timeZoneID));
        }
        return zonedTime;
    }

    public LocalDateTime getTimeParameter(String parameterName, Map<String, Value> parameters) {
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

    public String getLocationParameter(String parameterName, Map<String, Value> parameters) {
        Struct locationStruct = parameters.get(parameterName).getStructValue();
        Map<String, Value> location_fields = locationStruct.getFieldsMap();
        String ret = "";

        if (!location_fields.isEmpty()) {
            String island = location_fields.get("island").getStringValue();
            String businessName = location_fields.get("business-name").getStringValue();
            String street = location_fields.get("street-address").getStringValue();
            String city = location_fields.get("city").getStringValue();
            String subAdminArea = location_fields.get("subadmin-area").getStringValue();
            String adminArea = location_fields.get("admin-area").getStringValue();
            String country = location_fields.get("country").getStringValue();
            String zipCode = location_fields.get("zip-code").getStringValue();

            ArrayList<String> location_words = new ArrayList<String>();
            if (!island.isEmpty()) {
                ret = island;
            } else {
                if (!city.isEmpty()) {
                    location_words.add(city);
                }
                if (!subAdminArea.isEmpty()) { 
                    location_words.add(subAdminArea);
                }
                if (!adminArea.isEmpty()) { 
                    location_words.add(adminArea); 
                }
                if (!country.isEmpty()) { 
                    location_words.add(country);
                }
                if (!street.isEmpty()) { 
                    location_words.add(street); 
                }
                if (!zipCode.isEmpty()) { 
                    location_words.add(zipCode); 
                }
                if (!businessName.isEmpty()) {
                    location_words.add(businessName);
                }
                if (location_words.size() > 0) {
                    ret = location_words.get(0);
                    if (ret.startsWith("in ")) {
                        ret = ret.substring(3);
                    }
                } else {
                    ret = "";
                }
            }
        }
        return ret;
    }
}