package com.google.sps.data;

import com.google.gson.Gson;

// Video class for Workout agent

public final class Video {
  private String channelTitle;
  private String title;
  private String description;
  private String thumbnail;

  public Video(String channelTitle, String title, String description, String thumbnail) {
    this.channelTitle = channelTitle;
    this.title = title;
    this.description = description;
    this.thumbnail = thumbnail;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
