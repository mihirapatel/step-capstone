package com.google.sps.data;

import com.google.gson.Gson;

// Place class for Maps agent

public final class Place {
    private String attractionQuery = null;
    private int limit = -1;
    private double lng;
    private double lat;

    public Place(double longitude, double latitude) {
        lng = longitude;
        lat = latitude;
    }

    public Place(String query, double longitude, double latitude) {
        this(longitude, latitude);
        attractionQuery = query;
        
    }

    public Place(String query, double longitude, double latitude, int limit) {
        this(query, longitude, latitude);
        this.limit = limit;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}