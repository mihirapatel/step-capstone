package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import com.google.sps.utils.LocationUtils;
import java.util.Map;

/** Maps Agent */
public class Maps implements Agent {

  private final String intentName;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private String locationFormatted;
  private String locationDisplayed;
  private Location location;

  public Maps(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    try {
      setParameters(parameters);
    } catch (Exception e) {
      return;
    }
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    locationFormatted = LocationUtils.getFormattedAddress("location", parameters);
    locationDisplayed = LocationUtils.getDisplayAddress("location", parameters);
    if (intentName.contains("find")) {
      mapsFind(parameters);
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
    return null;
  }

  private void mapsFind(Map<String, Value> parameters) {
    String attraction = parameters.get("place-attraction").getStringValue();
    location = new Location(locationFormatted);
    Place place;
    String limitDisplay = "";
    int limit = (int) parameters.get("number").getNumberValue();
    if (limit > 0) {
      limitDisplay = String.valueOf(limit) + " ";
      place = new Place(attraction, location.getLng(), location.getLat(), limit);
    } else {
      place = new Place(attraction, location.getLng(), location.getLat());
    }

    fulfillment =
        "Here are the top "
            + limitDisplay
            + "results for "
            + attraction
            + " in "
            + locationDisplayed
            + ".";
    display = place.toString();
  }

  class Place {
    String attractionQuery;
    int limit = -1;
    double lng;
    double lat;

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
