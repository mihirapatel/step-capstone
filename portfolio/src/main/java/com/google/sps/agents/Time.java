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
import java.util.ArrayList;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
 
/**
 * Time Agent
 */
public class Time implements Agent {
    private final String intentName;
  	private String location;
    private String locationTo;
    private String locationFrom;
    private String locationOne;
    private String locationTwo;
    private ZonedDateTime timeFrom;
    
    public Time(String intentName, Map<String, Value> parameters) {
      this.intentName = intentName;
      setParameters(parameters);
    }

	@Override 
	public void setParameters(Map<String, Value> parameters) {
        if (intentName.equals("get") || intentName.equals("context:time") || intentName.equals("check")){
            this.location = getLocationParameter("location", parameters);
        }
        if (intentName.contains("convert")){
            this.locationFrom = getLocationParameter("location-from", parameters);
            this.locationTo = getLocationParameter("location-to", parameters);
            this.timeFrom = getZonedTime("time-from", locationFrom, parameters);
        }
        if (intentName.contains("time_zones")){
            this.location = getLocationParameter("location", parameters);
        }
        if (intentName.contains("time_difference")){
            this.locationOne = getLocationParameter("location-1", parameters);
            this.locationTwo = getLocationParameter("location-2", parameters);
        }
	}
	
	@Override
	public String getOutput() {
      String output = "";
      if (intentName.equals("get") || intentName.equals("context:time")){
          String currentTime = getCurrentTimeString(location);
          if (!currentTime.isEmpty()){
            output = "It is " + currentTime + " in " + location + ".";
          }
          else{
            output = "I don't recognize that place. Can you repeat that?";
          }
      }
      if (intentName.equals("check")){
          String currentTime = getCurrentTimeString(location);
          if (!currentTime.isEmpty()){
            output = "In " + location +", it is currently " + currentTime + ".";
          }
          else{
            output = "I don't recognize that place. Can you try again?";
          }
      }
      if (intentName.contains("convert")){
        String timeToString = "";
        String timeFromString = "";
        if (timeFrom != null){
            // Get time based on time to check
            timeToString = zonedTimeToString(getTimeIn(locationTo));
            timeFromString = zonedTimeToString(timeFrom);
            output = "It's "+ timeToString + " in " + locationTo 
                + " when it's " + timeFromString + " in " + locationFrom +".";

        } else {
            // Get current time in 2 diff timezones
            timeFromString = getCurrentTimeString(locationFrom);
            timeToString = getCurrentTimeString(locationTo);
            output = "It is currently "+ timeFromString + " in " + locationFrom 
                + " and " + timeToString + " in " + locationTo +".";
        }
        if (timeToString.isEmpty() || timeFromString.isEmpty()) {
            output = "I didn't catch that. Can you repeat that?";
        }
      }

      if (intentName.contains("time_zones")){
        String timezone = getZone(location);
        if (! timezone.isEmpty()){
            output = "The timezone in "+ location + " is " + timezone + "."; 
        } else {
            output = "I'm not sure where you are talking about. Can you repeat that?";
        }
      }
      if (intentName.contains("time_difference")){
        String timeDiffString = getTimeDiff(locationOne, locationTwo);
        if (! timeDiffString.isEmpty()){
            output = locationOne + " is " + timeDiffString + locationTwo + "."; 
        } else {
            output = "I didn't catch that. Can you repeat that?";
        }
      }
	  return output;
	}

	@Override
	public String getDisplay() {
		return null;
	}

	@Override
	public String getRedirect() {
		return null;
    }

    public ZonedDateTime getCurrentTime(String locationName){
        ZonedDateTime currentTime = null;
        try{
            Location place = new Location(locationName);
            String timeZoneID = place.getTimeZoneID();
            currentTime = ZonedDateTime.now(ZoneId.of(timeZoneID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTime;
    }

    public String getZone(String locationName){
        String timeZone = null;
        try{
            ZonedDateTime time = getCurrentTime(locationName);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("z");
            timeZone = time.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeZone;
    }

    public String getTimeDiff(String firstLocation, String secondLocation){
        // TO DO: fix to calculate difference in days, not just hours
        ZonedDateTime firstTime = getCurrentTime(firstLocation);
        ZonedDateTime secondTime = getCurrentTime(secondLocation);

        int firstHour = firstTime.getHour();
        int secondHour = secondTime.getHour();
        int hourDiff = firstHour - secondHour;

        String ret = "";
        if (hourDiff < 0){
            ret +=  String.valueOf(hourDiff * -1) + " hours behind ";
        }
        else if (hourDiff == 0){
            ret +=  "in the same time zone as ";
        }else{
            ret += String.valueOf(hourDiff) + " hours ahead of ";
        }
        return ret;
    }

    public ZonedDateTime getTimeIn(String locationIn){
        ZonedDateTime timeIn = null;
        try{
            Location placeTo = new Location(locationTo);
            String timeZoneID = placeTo.getTimeZoneID();
            timeIn = timeFrom.withZoneSameInstant(ZoneId.of(timeZoneID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeIn;
    }

    public String getCurrentTimeString(String location){
        ZonedDateTime time = getCurrentTime(location);
        return zonedTimeToString(time);
    }

    public String zonedTimeToString(ZonedDateTime time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String timeString = time.format(formatter);
        return timeString;
    }

    public ZonedDateTime getZonedTime(String timeName, String locationParameter, Map<String, Value> parameters){
        LocalDateTime localTime = getTimeParameter(timeName, parameters);
        ZonedDateTime zonedTime = null;
        if (localTime != null){
           try{
                Location place = new Location(locationParameter);
                String timeZoneID = place.getTimeZoneID();
                zonedTime = ZonedDateTime.of(localTime, ZoneId.of(timeZoneID));
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        return zonedTime;
    }

    public LocalDateTime getTimeParameter(String parameterName, Map<String, Value> parameters){
        String time = parameters.get(parameterName).getStringValue();
        LocalDateTime timeToCheck = null;

        if (! time.isEmpty()){
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

    public String getLocationParameter(String parameterName, Map<String, Value> parameters){
        Struct locationStruct = parameters.get(parameterName).getStructValue();
        Map<String, Value> location_fields = locationStruct.getFieldsMap();
        String ret = "";

        if (!location_fields.isEmpty()){
            String island = location_fields.get("island").getStringValue();
            String street = location_fields.get("street-address").getStringValue();
            String city = location_fields.get("city").getStringValue();
            String subAdminArea = location_fields.get("subadmin-area").getStringValue();
            String adminArea = location_fields.get("admin-area").getStringValue();
            String country = location_fields.get("country").getStringValue();
            String zipCode = location_fields.get("zip-code").getStringValue();

            ArrayList<String> location_words = new ArrayList<String>();
            if (!island.isEmpty()){ret = island;}
            else{
                if (!city.isEmpty()){location_words.add(city);}
                if (!subAdminArea.isEmpty()){location_words.add(subAdminArea);}
                if (!adminArea.isEmpty()){location_words.add(adminArea);}
                if (!country.isEmpty()){location_words.add(country);}
                if (!street.isEmpty()){location_words.add(street);}
                if (!zipCode.isEmpty()){location_words.add(zipCode);}
                if (location_words.size() > 0){
                    ret = location_words.get(0);
                }
                else{
                    ret = "";
                }
            }
        }
        return ret;
    }
}