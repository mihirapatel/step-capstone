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

package com.google.sps.utils;

import com.google.maps.errors.ApiException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class contains methods to parse Dialogflow’s detected location inputs and determine the
 * appropriate formatted string to be sent to location-based APIs or outputted to the user interface
 */
public class LocationUtils {
  /**
   * This function returns a valid formatted address from the Geocoding API based on the user
   * inputted address, and throws an exception otherwise.
   *
   * @param parameterName name of parameter to get address from
   * @param parameters map of parameters detected from Dialogflow
   * @return String formatted address
   */
  public static String getFormattedAddress(String parameterName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    String displayAddress = getDisplayAddress(parameterName, parameters);
    String formattedAddress = "";
    if (!displayAddress.isEmpty()) {
      Location place = Location.create(displayAddress);
      formattedAddress = place.getAddressFormatted();
    }
    return formattedAddress;
  }

  /**
   * This function returns the address detected from Dialogflow based on the user inputted address.
   *
   * @param parameterName name of parameter to get address from
   * @param parameters map of parameters detected from Dialogflow
   * @return String display address
   */
  public static String getDisplayAddress(String parameterName, Map<String, Value> parameters) {
    ArrayList<String> locationNames = getLocationParameters(parameterName, parameters);
    String displayAddress = "";
    if (!locationNames.isEmpty()) {
      for (int i = 0; i < locationNames.size(); ++i) {
        String currentString = locationNames.get(i);
        // Case when Dialogflow detects "in London" instead of London as location
        if (currentString.startsWith("in ")) {
          String newString = currentString.substring(3);
          locationNames.set(i, newString);
        }
      }
      displayAddress = String.join(", ", locationNames);
    }
    return displayAddress;
  }

  /**
   * This function returns a single field from the address detected from Dialogflow based on the
   * user inputted address, which is used for brief responses.
   *
   * @param parameterName name of parameter to get address from
   * @param parameters map of parameters detected from Dialogflow
   * @return String brief address
   */
  public static String getOneFieldAddress(String parameterName, Map<String, Value> parameters) {
    ArrayList<String> locationNames = getLocationParameters(parameterName, parameters);
    String displayAddress = "";
    if (!locationNames.isEmpty()) {
      displayAddress = locationNames.get(0);
      // Case when Dialogflow detects "in London" instead of London as location
      if (displayAddress.startsWith("in ")) {
        return displayAddress.substring(3);
      }
    }
    return displayAddress;
  }

  /**
   * This function returns an ArrayList containing all location fields detected from Dialogflow
   * based on the specified parameter name.
   *
   * @param parameterName name of parameter to get location fields from
   * @param parameters map of parameters detected from Dialogflow
   * @return ArrayList containing all location fields
   */
  public static ArrayList<String> getLocationParameters(
      String parameterName, Map<String, Value> parameters) {
    Struct locationStruct = parameters.get(parameterName).getStructValue();
    Map<String, Value> location_fields = locationStruct.getFieldsMap();
    ArrayList<String> location_words = new ArrayList<String>();

    if (!location_fields.isEmpty()) {
      String island = location_fields.get("island").getStringValue();
      String businessName = location_fields.get("business-name").getStringValue();
      String street = location_fields.get("street-address").getStringValue();
      String city = location_fields.get("city").getStringValue();
      String subAdminArea = location_fields.get("subadmin-area").getStringValue();
      String adminArea = location_fields.get("admin-area").getStringValue();
      String country = location_fields.get("country").getStringValue();
      String zipCode = location_fields.get("zip-code").getStringValue();

      if (!island.isEmpty()) {
        location_words.add(island);
      } else {
        if (!street.isEmpty()) {
          location_words.add(street);
        }
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
        if (!zipCode.isEmpty()) {
          location_words.add(zipCode);
        }
        if (!businessName.isEmpty()) {
          location_words.add(businessName);
        }
      }
    }
    return location_words;
  }
}
