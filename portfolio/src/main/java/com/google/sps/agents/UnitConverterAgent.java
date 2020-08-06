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
import com.google.protobuf.Value;
import java.util.Map;

/** Unit Converter Agent */
public class UnitConverterAgent implements Agent {
  private final String intentName;
  private String unitFrom;
  private String unitTo;
  private Double amount;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private String searchText = "";
  private String baseURL;
  private String endURL;

  /**
   * Unit Converter agent constructor that uses intent and parameter to determnine fulfillment for
   * user request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public UnitConverterAgent(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  /**
   * Method that handles parameter assignment for fulfillment text and display based on the user's
   * input intent and extracted parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters) {
    unitFrom = parameters.get("unit-from").getStringValue();
    unitTo = parameters.get("unit-to").getStringValue();
    amount = parameters.get("amount").getNumberValue();
    fulfillment = "Redirecting for conversion";
    baseURL = "http://www.google.com/search?q=";

    searchText += "Convert";
    if (amount > 0.0) {
      searchText += " " + String.valueOf(amount);
    }
    if (!unitFrom.equals("")) {
      searchText += " " + unitFrom;
    }
    if (!unitTo.equals("")) {
      if (!unitFrom.equals("")) {
        searchText += " to";
      }
      searchText += " " + unitTo;
    }
    String[] individualWords = searchText.split(" ");
    endURL = String.join("+", individualWords);
    redirect = baseURL + endURL;
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
