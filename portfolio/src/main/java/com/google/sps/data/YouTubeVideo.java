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
import java.io.Serializable;

/** YouTubeVideo class for workout agent that has all YouTube video information */
public class YouTubeVideo implements Serializable {
  private String userId;
  private String channelTitle;
  private String title;
  private String description;
  private String thumbnail;
  private String videoId;
  private String videoURL = "https://www.youtube.com/watch?v=";
  private String channelURL = "https://www.youtube.com/channel/";
  private int currentIndex;
  private int videosDisplayedPerPage;
  private int currentPage;
  private int totalPages;
  private static final long serialVersionUID = 5716459602340197781L;

  /**
   * YouTubeVideo contructor to use if user is not logged in
   *
   * @param channelTitle String name of channel that posted YouTube video
   * @param title String name of YouTube video
   * @param description String description of YouTube video
   * @param thumbnail String thumbnail URL of YouTube video
   * @param videoId String id of YouTube video
   * @param channelId String id of channel that posted YouTube video
   * @param currentIndex int index of video out of list of all YouTube videos returned
   * @param videosDisplayedPerPage int number of videos displayed per page on assistant display
   * @param currentPage int page that this YouTube video is displayed on
   * @param totalPages int total number of pages to display on assistant display
   */
  public YouTubeVideo(
      String channelTitle,
      String title,
      String description,
      String thumbnail,
      String videoId,
      String channelId,
      int currentIndex,
      int videosDisplayedPerPage,
      int currentPage,
      int totalPages) {
    this.channelTitle = channelTitle;
    this.title = title;
    this.description = description;
    this.thumbnail = thumbnail;
    this.videoId = videoId;
    this.currentIndex = currentIndex;
    this.videosDisplayedPerPage = videosDisplayedPerPage;
    this.currentPage = currentPage;
    this.totalPages = totalPages;
    videoURL += videoId;
    channelURL += channelId;
  }

  /**
   * YouTubeVideo contructor to use if user is logged in
   *
   * @param userId String userId of user using workout agent
   * @param channelTitle String name of channel that posted YouTube video
   * @param title String name of YouTube video
   * @param description String description of YouTube video
   * @param thumbnail String thumbnail URL of YouTube video
   * @param videoId String id of YouTube video
   * @param channelId String id of channel that posted YouTube video
   * @param currentIndex int index of video out of list of all YouTube videos returned
   * @param videosDisplayedPerPage int number of videos displayed per page on assistant display
   * @param currentPage int page that this YouTube video is displayed on
   * @param totalPages int total number of pages to display on assistant display
   */
  public YouTubeVideo(
      String userId,
      String channelTitle,
      String title,
      String description,
      String thumbnail,
      String videoId,
      String channelId,
      int currentIndex,
      int videosDisplayedPerPage,
      int currentPage,
      int totalPages) {
    this.userId = userId;
    this.channelTitle = channelTitle;
    this.title = title;
    this.description = description;
    this.thumbnail = thumbnail;
    this.videoId = videoId;
    this.currentIndex = currentIndex;
    this.videosDisplayedPerPage = videosDisplayedPerPage;
    this.currentPage = currentPage;
    this.totalPages = totalPages;
    videoURL += videoId;
    channelURL += channelId;
  }

  /** Get Methods */
  public String getUserId() {
    return this.userId;
  }

  public String getChannelTitle() {
    return this.channelTitle;
  }

  public String getTitle() {
    return this.title;
  }

  public String getThumbnail() {
    return this.thumbnail;
  }

  public String getVideoId() {
    return this.videoId;
  }

  public String getVideoURL() {
    return this.videoURL;
  }

  public String getChannelURL() {
    return this.channelURL;
  }

  /** Converts YouTubeVideo into Gson string */
  public String toGson() {
    return new Gson().toJson(this);
  }
}
