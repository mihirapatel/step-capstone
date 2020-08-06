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
import java.util.ArrayList;

/** WorkoutPlan class with all necessary workout plan information */
public class WorkoutPlan implements Serializable {

  private String userId;
  private String workoutPlanName;
  private ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist;
  private String playlistId;
  private int workoutPlanId;
  private String dateCreated;
  private int numWorkoutDaysCompleted;
  private int planLength;
  private static final long serialVersionUID = -7602557355975608053L;

  /**
   * WorkoutPlan constructor to use when user is not logged in
   *
   * @param workoutPlanPlaylist ArrayList<ArrayList<YouTubeVideo>> list of workout videos split into
   *     chunks
   * @param playlistId String playlistId to link workout plan to correct YouTube playlist
   */
  public WorkoutPlan(ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist, String playlistId) {
    this.workoutPlanPlaylist = workoutPlanPlaylist;
    this.playlistId = playlistId;
  }

  /**
   * WorkoutPlan constructor to use when user is logged in
   *
   * @param userId String userId of user that generated workout plan
   * @param workoutPlanName String of workout plan name
   * @param workoutPlanPlaylist ArrayList<ArrayList<YouTubeVideo>> list of workout videos split into
   *     chunks
   * @param workoutPlanId int workoutPlanId to keep track of diffent workout plans created by user
   * @param dateCreated String of date workout plan was created
   * @param planLength int of workout plan length (in days)
   */
  public WorkoutPlan(
      String userId,
      String workoutPlanName,
      ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist,
      int workoutPlanId,
      String dateCreated,
      int planLength) {
    this.userId = userId;
    this.workoutPlanName = workoutPlanName;
    this.workoutPlanPlaylist = workoutPlanPlaylist;
    this.workoutPlanId = workoutPlanId;
    this.dateCreated = dateCreated;
    this.planLength = planLength;
  }

  /** Get Methods */
  public String getUserId() {
    return this.userId;
  }

  public String getWorkoutPlanName() {
    return this.workoutPlanName;
  }

  public ArrayList<ArrayList<YouTubeVideo>> getWorkoutPlanPlaylist() {
    return this.workoutPlanPlaylist;
  }

  public String getPlaylistId() {
    return this.playlistId;
  }

  public int getWorkoutPlanId() {
    return this.workoutPlanId;
  }

  public String getDateCreated() {
    return this.dateCreated;
  }

  public int getNumWorkoutDaysCompleted() {
    return this.numWorkoutDaysCompleted;
  }

  /** Set Methods */
  public void setNumWorkoutDaysCompleted(int numWorkoutDaysCompleted) {
    this.numWorkoutDaysCompleted = numWorkoutDaysCompleted;
  }

  /** Convert WorkoutPlan into Gson string */
  public String toGson() {
    return new Gson().toJson(this);
  }
}
