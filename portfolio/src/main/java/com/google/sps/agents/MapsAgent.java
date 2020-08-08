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

package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.maps.errors.ApiException;
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import com.google.sps.data.Place;
import com.google.sps.utils.LocationUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Maps Agent */
public class MapsAgent implements Agent {

  private static Logger log = LoggerFactory.getLogger(MapsAgent.class);

  private final String intentName;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private ArrayList<String> locationWords;
  private String locationFormatted;
  private Location location;

  /**
   * Maps agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public MapsAgent(String intentName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.intentName = intentName;
    setParameters(parameters);
  }

  /**
   * Properly populates all private instance variables to determine fulfillment and display output
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    locationFormatted = LocationUtils.getFormattedAddress("location", parameters);
    locationWords = LocationUtils.getLocationParameters("location", parameters);
    if (intentName.contains("search")) {
      mapsSearch(parameters);
    } else if (intentName.contains("find")) {
      mapsFind(parameters);
    }
  }

  /**
   * Handles maps search intent to find the user's desired place
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void mapsSearch(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    location = Location.create(locationFormatted);
    fulfillment = "Here is the map for: " + locationFormatted;
    Place place = new Place(location.getLng(), location.getLat());
    display = place.toString();
  }

  /**
   * Handles maps find intent which finds places according to the identified location parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void mapsFind(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    String attraction = parameters.get("place-attraction").getStringValue();
    location = Location.create(locationFormatted);
    Place place;
    String limitDisplay = "";
    if (parameters.get("number").getStringValue().equals("-1")) {
      place = new Place(attraction, location.getLng(), location.getLat());
    } else {
      int limit = (int) parameters.get("number").getNumberValue();
      if (limit > 0 && limit < 20) {
        limitDisplay = String.valueOf(limit) + " ";
        place = new Place(attraction, location.getLng(), location.getLat(), limit);
      } else {
        fulfillment =
            "Invalid input for number of "
                + attraction
                + ". Please try again with a positive integer between 1 and 20.";
        return;
      }
    }
    fulfillment =
        "Here are the top "
            + limitDisplay
            + "results for "
            + attraction
            + " in "
            + locationFormatted
            + ".";
    display = place.toString();
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
}
