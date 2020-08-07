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

package com.google.sps.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.TimeZoneApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TimeZone;

/**
 * A Location object contains the following properties: address: user-inputted location coords:
 * coordinates for user-inputted address latCoord: latitude coordinate for location lngCoord:
 * longitude coordinate for location formattedAddress: user-inputted location formatted by Geocoding
 * API timeZoneObj: TimeZone object for corresponding LatLng coords timeZoneID: timezone ID for the
 * location ("America/Los_Angeles") timeZoneName: Standard time zone name for location ("Pacific
 * Standard Time").
 *
 * <p>A Location object is only created by create() function, ensuring that an Location object is
 * only created with valid parameters and all Location objects are valid.
 */
public class Location {

  private String address;
  private String formattedAddress;
  private double latCoord;
  private double lngCoord;
  private LatLng coords;
  private TimeZone timeZoneObj;
  private String timeZoneID;
  private String timeZoneName;

  /**
   * Creates a Location object, or throws exception if any parameters for Location are invalid.
   *
   * @param address user-inputted location
   * @return Location object
   */
  public static Location create(String address)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    LatLng coords = getCoordinates(address);
    String formattedAddress = getFullAddress(address);
    TimeZone timeZoneObj = getTimeZoneFromAPI(coords);
    Location location = new Location(address, coords, formattedAddress, timeZoneObj);
    return location;
  }

  /**
   * Private Location constructor, can only be called by create()
   *
   * @param address user-inputted location
   * @param coords coordinates for user-inputted address from Geocoding API
   * @param formattedAddress user-inputted location formatted by Geocoding API
   * @param timezone TimeZone object for corresponding LatLng coords from TimeZone API
   */
  private Location(String address, LatLng coords, String formattedAddress, TimeZone timezone) {
    this.address = address;
    this.coords = coords;
    this.formattedAddress = formattedAddress;
    this.timeZoneObj = timezone;

    this.latCoord = coords.lat;
    this.lngCoord = coords.lng;
    this.timeZoneName = timeZoneObj.getDisplayName();
    this.timeZoneID = timeZoneObj.getID();
  }

  /**
   * This function returns a valid GeoApiContext to make calls to Geocoding and Timezone API, and
   * throws an exception otherwise.
   *
   * @return GeoApiContext
   */
  public static GeoApiContext getGeoApiContext()
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    String apiKey =
        new String(
            Files.readAllBytes(
                Paths.get(Location.class.getResource("/files/apikey.txt").getFile())));
    GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
    return context;
  }

  /**
   * This function returns valid LatLng coordinates from the Geocoding API based on the user
   * inputted address, and throws an exception otherwise.
   *
   * @param address user-inputted location string
   * @return LatLng object
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
   * This function returns a valid full address from the Geocoding API based on the user inputted
   * address, and throws an exception otherwise.
   *
   * @param address user-inputted location string
   * @return String formatted address
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
   * This function returns a valid TimeZone object from the Timezone API based on the LatLng
   * coordinates determined from the Geocoding API, and throws an exception otherwise.
   *
   * @param location LatLng object
   * @return TimeZone object
   */
  public static TimeZone getTimeZoneFromAPI(LatLng location)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    GeoApiContext context = getGeoApiContext();
    TimeZone results = TimeZoneApi.getTimeZone(context, location).await();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TimeZone timeZoneObject = results;
    return timeZoneObject;
  }

  public String getAddress() {
    return this.address;
  }

  public String getAddressFormatted() {
    return this.formattedAddress;
  }

  public Double getLat() {
    return this.latCoord;
  }

  public Double getLng() {
    return this.lngCoord;
  }

  public LatLng getCoords() {
    return this.coords;
  }

  public String getTimeZoneID() {
    return this.timeZoneID;
  }

  public String getTimeZoneName() {
    return this.timeZoneName;
  }

  public TimeZone getTimeZone() {
    return this.timeZoneObj;
  }
}
