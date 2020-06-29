/*
 * Copyright 2018 Google Inc.
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

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.TimeZoneApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TimeZone;

/* Location object  */
public class Location {
  private String address;
  private double latCoord;
  private double lngCoord;
  private LatLng coords;
  private TimeZone timeZoneObj;
  private String timeZoneID;
  private String formattedAddress;

  public Location(String address) {
    this.address = address;
    setProperties();
  }

  public void setProperties() {
    try {
      String apiKey =
          new String(
              Files.readAllBytes(Paths.get(getClass().getResource("/files/apikey.txt").getFile())));
      GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
      setCoordinates(context, address);
      setTimeZone(context, coords);
    } catch (IOException e) {
      System.out.println("API key for maps not found.");
    }
  }

  public void setCoordinates(GeoApiContext context, String address) {
    try {
      GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      this.latCoord = results[0].geometry.location.lat;
      this.lngCoord = results[0].geometry.location.lng;
      this.coords = new LatLng(latCoord, lngCoord);
      this.formattedAddress = results[0].formattedAddress;
    } catch (Exception e) {
      return;
    }
  }

  public void setTimeZone(GeoApiContext context, LatLng location) {
    try {
      TimeZone results = TimeZoneApi.getTimeZone(context, location).await();
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      this.timeZoneObj = results;
      this.timeZoneID = results.getID();
    } catch (Exception e) {
      return;
    }
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

  public TimeZone getTimeZone() {
    return this.timeZoneObj;
  }
}
