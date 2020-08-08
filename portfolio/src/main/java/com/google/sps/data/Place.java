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

package com.google.sps.data;

import com.google.gson.Gson;

// Place class for Maps agent

public final class Place {
  private String attractionQuery = null;
  private int limit = -1;
  private double lng;
  private double lat;

  /**
   * Place constructor for maps search intent display
   *
   * @param longitude Longitudinal value of the place
   * @param latitude Latitudinal value of the place
   */
  public Place(double longitude, double latitude) {
    lng = longitude;
    lat = latitude;
  }

  /**
   * Place constructor for maps find intent display
   *
   * @param query String containing the search string for map location type
   * @param longitude Longitudinal value of the place
   * @param latitude Latitudinal value of the place
   */
  public Place(String query, double longitude, double latitude) {
    this(longitude, latitude);
    attractionQuery = query;
  }

  /**
   * Place constructor for maps find intent display with limit for number of output places
   *
   * @param limit Max number of places to display.
   * @param query String containing the search string for map location type
   * @param longitude Longitudinal value of the place
   * @param latitude Latitudinal value of the place
   */
  public Place(String query, double longitude, double latitude, int limit) {
    this(query, longitude, latitude);
    this.limit = limit;
  }

  /** Converts conversation output object to JSON string form. */
  public String toString() {
    return new Gson().toJson(this);
  }
}
