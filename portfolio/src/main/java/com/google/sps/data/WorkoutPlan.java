package com.google.sps.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;

/** WorkoutPlan class that stores userId and corresponding workout plan */
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

  // 2020-07-27 17:24:59.583:WARN:oejs.HttpChannel:qtp644166178-17:
  // handleException /save-workouts java.io.InvalidClassException: com.google.sps.data.WorkoutPlan;
  // local class incompatible:
  // stream classdesc serialVersionUID = 1554936233711268897, local class serialVersionUID =
  // -7602557355975608053

  /** WorkoutPlan constructor to use when user is not logged in */
  public WorkoutPlan(ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist, String playlistId) {
    this.workoutPlanPlaylist = workoutPlanPlaylist;
    this.playlistId = playlistId;
  }

  /** WorkoutPlan constructor to use when user is logged in */
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

  public String toGson() {
    return new Gson().toJson(this);
  }
}
