package com.google.sps.data;

import com.google.gson.Gson;

// Video class for Workout agent

public final class Video {
  private String channelTitle;
  private String title;
  private String description;
  private String thumbnail;
  private String videoURL = "https://www.youtube.com/watch?v=";
  private String channelURL = "https://www.youtube.com/channel/";

  public Video(
      String channelTitle,
      String title,
      String description,
      String thumbnail,
      String videoId,
      String channelId) {
    this.channelTitle = channelTitle;
    this.title = title;
    this.description = description;
    this.thumbnail = thumbnail;
    videoURL += videoId;
    channelURL += channelId;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
