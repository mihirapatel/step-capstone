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
