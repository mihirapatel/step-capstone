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
import com.google.sps.utils.AgentUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Currency Agent */
public class CurrencyAgent implements Agent {

  private static Logger log = LoggerFactory.getLogger(CurrencyAgent.class);

  private final String intentName;
  private String userInput;
  private String currencyFrom;
  private String currencyTo;
  private Double amount;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private String searchText = "";
  private String searchParameters = "";
  private String baseURL;
  private String endURL;

  /**
   * Currency agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public CurrencyAgent(String intentName, Map<String, Value> parameters) {
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
    currencyFrom = parameters.get("currency-from").getStringValue();
    currencyTo = parameters.get("currency-to").getStringValue();
    amount = parameters.get("amount").getNumberValue();
    userInput = AgentUtils.getUserInput().toLowerCase();
    baseURL = "http://www.google.com/search?q=";

    // Searching for exchange rate
    if (userInput.contains("exchange")) {
      fulfillment = "Redirecting for exchange rate";
      searchText += "Exchange rate";

      if (amount > 0.0) {
        searchParameters += " for " + String.valueOf(amount);
      }
      if (!currencyFrom.equals("")) {
        searchParameters += " " + currencyFrom;
      }
      if (!currencyTo.equals("")) {
        if (!currencyFrom.equals("")) {
          searchParameters += " to";
        }
        searchParameters += " " + currencyTo;
      }

      searchText += searchParameters;
      String[] individualWords = searchText.split(" ");
      endURL = String.join("+", individualWords);

      // Searching for conversion
    } else {
      fulfillment = "Redirecting for conversion";
      searchText += "Convert";

      if (amount > 0.0) {
        searchParameters += " " + String.valueOf(amount);
      }
      if (!currencyFrom.equals("")) {
        searchParameters += " " + currencyFrom;
      }
      if (!currencyTo.equals("")) {
        if (!currencyFrom.equals("")) {
          searchParameters += " to";
        }
        searchParameters += " " + currencyTo;
      }
      if (searchParameters.equals("")) {
        searchParameters += " currency";
      }

      searchText += searchParameters;
      String[] individualWords = searchText.split(" ");
      endURL = String.join("+", individualWords);
    }

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
