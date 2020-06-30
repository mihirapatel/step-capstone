package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.gson.Gson;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import com.google.sps.data.Output;
import com.google.sps.agents.Agent;
import com.google.sps.utils.LocationUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
/**
 * Maps Agent
 */
public class Maps implements Agent {
    
  private final String intentName;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private ArrayList<String> locationWords;
  private String locationFormatted;
  private Location location;

  public Maps(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override 
  public void setParameters(Map<String, Value> parameters) {
    locationFormatted = LocationUtils.getFormattedAddress("location", parameters);
    locationWords = LocationUtils.getLocationParameters("location", parameters);
    if(intentName.contains("search")) {
        mapsSearch(parameters);
    }
  }

  @Override
  public String getOutput() {
    return fulfillment;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  @Override
  public String getRedirect() {
    return redirect;
  }

  private void mapsSearch(Map<String, Value> parameters) {
    location = new Location(locationFormatted); 
    fulfillment = "Here is the map for: " + locationFormatted;

    Place place = new Place(location.getLng(), location.getLat());
    display = place.toString();
  }

  class Place {
    String attractionQuery;
    int limit = -1;
    double lng;
    double lat;

    Place(double longitude, double latitude) {
      lng = longitude;
      lat = latitude;
    }

    Place(String query, double longitude, double latitude) {
      attractionQuery = query;
      lng = longitude;
      lat = latitude;
    }

    Place(String query, double longitude, double latitude, int limit) {
      this(query, longitude, latitude);
      this.limit = limit;
    }

    public String toString() {
      return new Gson().toJson(this);
    }
   
  }
}

