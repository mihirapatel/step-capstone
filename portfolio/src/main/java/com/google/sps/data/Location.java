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
import com.google.maps.model.LatLng;
import java.util.TimeZone;

/**
 * A Location object contains the following properties: address: user-inputted location coords:
 * coordinates for user-inputted address latCoord: latitude coordinate for location lngCoord:
 * longitude coordinate for location formattedAddress: user-inputted location formatted by Geocoding
 * API timeZoneObj: TimeZone object for corresponding LatLng coords timeZoneID: timezone ID for the
 * location ("America/Los_Angeles") timeZoneName: Standard time zone name for location ("Pacific
 * Standard Time")
 *
 * <p>A Location object is only created by LocationUtils.getLocationObject(), ensuring that an
 * Location object is only created with valid parameters:
 *
 * @param address user-inputted location
 * @param coords coordinates for user-inputted address from Geocoding API
 * @param formattedAddress user-inputted location formatted by Geocoding API
 * @param timezone TimeZone object for corresponding LatLng coords from TimeZone API
 */
public class Location {

  private String address;
  private double latCoord;
  private double lngCoord;
  private LatLng coords;
  private TimeZone timeZoneObj;
  private String timeZoneID;
  private String timeZoneName;
  private String formattedAddress;

  public Location(String address, LatLng coords, String formattedAddress, TimeZone timezone) {
    this.address = address;
    this.coords = coords;
    this.formattedAddress = formattedAddress;
    this.timeZoneObj = timezone;

    this.latCoord = coords.lat;
    this.lngCoord = coords.lng;
    this.timeZoneName = timeZoneObj.getDisplayName();
    this.timeZoneID = timeZoneObj.getID();
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
