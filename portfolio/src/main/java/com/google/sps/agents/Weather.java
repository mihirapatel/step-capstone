package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.agents.Agent;
import com.google.sps.data.Location;
import com.google.sps.data.Output;
import com.google.sps.utils.LocationUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
 
/**
 * Weather Agent
 */
public class Weather implements Agent {
    private final String intentName;
    private String displayAddress;
    private String searchAddress;
    private String output = null;
    private String redirect = null;
    
    public Weather(String intentName, Map<String, Value> parameters) {
        this.intentName = intentName;
        try {
            setParameters(parameters);
        } catch (Exception e) {
            return;
        }
    }

	@Override 
	public void setParameters(Map<String, Value> parameters) {
        this.displayAddress = LocationUtils.getDisplayAddress("address", parameters);
        this.searchAddress = LocationUtils.getFormattedAddress("address", parameters);
        System.out.println(displayAddress);
        System.out.println(searchAddress);
        if (!displayAddress.isEmpty() && !searchAddress.isEmpty()){
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
/*
    public String getFormattedAddress(String parameterName, Map<String, Value> parameters) {
        ArrayList<String> locationNames = getLocationParameters(parameterName, parameters);
        String formattedAddress = "";
        if (locationNames.size() > 0){
            String fullAddress = String.join(",", locationNames);
            Location place = new Location(fullAddress);
            formattedAddress = place.getAddressFormatted();
        }
        return formattedAddress;
    }

    public String getDisplayAddress(String parameterName, Map<String, Value> parameters) {
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

    public ArrayList<String> getLocationParameters(String parameterName, Map<String, Value> parameters) {
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
    }*/
}
