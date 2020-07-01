package com.google.sps.utils;

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.TimeZoneApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Location;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;

public class LocationUtils {

  /** Returns location object, or throws exception if any parameters are invalid */
  public static Location getLocationObject(String address)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    LatLng coords = getCoordinates(address);
    String formattedAddress = getFullAddress(address);
    TimeZone timeZoneObj = getTimeZone(coords);
    Location location = new Location(address, coords, formattedAddress, timeZoneObj);
    return location;
  }

  /** Returns a GeoApiContext and throws an exception otherwise */
  public static GeoApiContext getGeoApiContext()
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    String apiKey =
        new String(
            Files.readAllBytes(
                Paths.get(LocationUtils.class.getResource("/files/apikey.txt").getFile())));
    GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
    return context;
  }

  /**
   * Returns coordinates from Geocoding API from address input, and throws an exception otherwise
   */
  public static LatLng getCoordinates(String address)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    GeoApiContext context = getGeoApiContext();
    GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Double latCoord = results[0].geometry.location.lat;
    Double lngCoord = results[0].geometry.location.lng;
    LatLng coords = new LatLng(latCoord, lngCoord);
    return coords;
  }

  /**
   * Returns a full address from Geocoding API from address input and throws an exception otherwise
   */
  public static String getFullAddress(String address)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    GeoApiContext context = getGeoApiContext();
    GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String formattedAddress = results[0].formattedAddress;
    return formattedAddress;
  }

  /**
   * Returns TimeZone object from Timezone API from LatLng location and throws an exception
   * otherwise
   */
  public static TimeZone getTimeZone(LatLng location)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    GeoApiContext context = getGeoApiContext();
    TimeZone results = TimeZoneApi.getTimeZone(context, location).await();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TimeZone timeZoneObject = results;
    return timeZoneObject;
  }

  /**
   * Returns formatted address String from a Dialogflow parameter and throws an exception otherwise
   */
  public static String getFormattedAddress(String parameterName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    String displayAddress = getDisplayAddress(parameterName, parameters);
    String formattedAddress = "";
    if (!displayAddress.isEmpty()) {
      Location place = getLocationObject(displayAddress);
      formattedAddress = place.getAddressFormatted();
    }
    return formattedAddress;
  }

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
