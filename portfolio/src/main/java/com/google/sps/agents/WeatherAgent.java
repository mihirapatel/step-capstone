/*
 * Copyright 2019 Google LLC
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

import com.google.maps.errors.ApiException;
import com.google.protobuf.Value;
import com.google.sps.utils.LocationUtils;
import java.io.IOException;
import java.util.Map;

/**
 * Weather Agent handles users' requests for time information. It determines appropriate outputs and
 * display information to send to the user interface based on Dialogflow's detected Weather intents.
 */
public class WeatherAgent implements Agent {
  private final String intentName;
  private String displayAddress;
  private String searchAddress;
  private String output = null;
  private String redirect = null;

  /**
   * Weather agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public WeatherAgent(String intentName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.intentName = intentName;
    setParameters(parameters);
  }

  /**
   * Method that handles parameter assignment for fulfillment text and display based on the user's
   * input intent and extracted parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.displayAddress = LocationUtils.getDisplayAddress("address", parameters);
    this.searchAddress = LocationUtils.getFormattedAddress("address", parameters);
    if (!displayAddress.isEmpty() && !searchAddress.isEmpty()) {
      String baseURL = "http://www.google.com/search?q=weather+in+";
      String[] individualWords = searchAddress.split(" ");
      String endURL = String.join("+", individualWords);
      this.redirect = baseURL + endURL;
      this.output = "Redirecting you to the current forecast in " + displayAddress + ".";
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
    return this.redirect;
  }
}
