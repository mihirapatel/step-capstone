package com.google.sps.utils;

// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Parses parameter locations to full name and display name
 */
public class LocationUtils {
    public static String getFormattedAddress(String parameterName, Map<String, Value> parameters) {
        String displayAddress = getDisplayAddress(parameterName, parameters);
        String formattedAddress = "";
        if (!displayAddress.isEmpty()){
            Location place = new Location(displayAddress);
            formattedAddress = place.getAddressFormatted();
        }
        return formattedAddress;
    }

    public static String getDisplayAddress(String parameterName, Map<String, Value> parameters) {
        ArrayList<String> locationNames = getLocationParameters(parameterName, parameters);
        String displayAddress = "";
        if (locationNames.size() > 0){
            for (int i = 0; i < locationNames.size(); ++i){
                String currentString = locationNames.get(i);
                if (currentString.startsWith("in ")){
                    String newString = currentString.substring(3);
                    locationNames.set(i, newString);
                }
            }
            displayAddress = String.join(", ", locationNames);
        }
        return displayAddress;
    }

    public static String getOneFieldAddress(String parameterName, Map<String, Value> parameters) {
        ArrayList<String> locationNames = getLocationParameters(parameterName, parameters);
        String displayAddress = "";
        if (locationNames.size() > 0){
            displayAddress = locationNames.get(0);
            if (displayAddress.startsWith("in ")) {
                displayAddress = displayAddress.substring(3);
            }
        }
        return displayAddress;
    }

    public static ArrayList<String> getLocationParameters(String parameterName, Map<String, Value> parameters) {
        Struct locationStruct = parameters.get(parameterName).getStructValue();
        Map<String, Value> locationFields = locationStruct.getFieldsMap();
        ArrayList<String> locationWords = new ArrayList<String>();

        if (!locationFields.isEmpty()) {
            String island = locationFields.get("island").getStringValue();
            String businessName = locationFields.get("business-name").getStringValue();
            String street = locationFields.get("street-address").getStringValue();
            String city = locationFields.get("city").getStringValue();
            String county = locationFields.get("subadmin-area").getStringValue();
            String state = locationFields.get("admin-area").getStringValue();
            String country = locationFields.get("country").getStringValue();
            String zipCode = locationFields.get("zip-code").getStringValue();

            if (!island.isEmpty()) {
                locationWords.add(island); 
            } else {
                if (!street.isEmpty()) { 
                    locationWords.add(street); 
                }
                if (!city.isEmpty()) {
                    locationWords.add(city);
                }
                if (!county.isEmpty()) { 
                    locationWords.add(county);
                }
                if (!state.isEmpty()) { 
                    locationWords.add(state); 
                }
                if (!country.isEmpty()) { 
                    locationWords.add(country);
                }
                if (!zipCode.isEmpty()) { 
                    locationWords.add(zipCode); 
                }
                if (!businessName.isEmpty()) {
                    locationWords.add(businessName);
                }
            }
        }
        return locationWords;
    }
}