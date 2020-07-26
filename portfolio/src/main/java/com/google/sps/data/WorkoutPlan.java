package com.google.sps.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;

/** WorkoutPlan class that stores userId and corresponding workout plan */
public class WorkoutPlan implements Serializable {

  private String userId;
  private String workoutPlanName;
  private ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist;
  private int workoutPlanId;
  private String dateCreated;
  private static final long serialVersionUID = -7602557355975608053L;

  /** WorkoutPlan constructor to use when user is not logged in */
  public WorkoutPlan(ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist) {
    this.workoutPlanPlaylist = workoutPlanPlaylist;
  }

  /** WorkoutPlan constructor to use when user is logged in */
  public WorkoutPlan(
      String userId,
      String workoutPlanName,
      ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist,
      int workoutPlanId,
      String dateCreated) {
    this.userId = userId;
    this.workoutPlanName = workoutPlanName;
    this.workoutPlanPlaylist = workoutPlanPlaylist;
    this.workoutPlanId = workoutPlanId;
    this.dateCreated = dateCreated;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getWorkoutPlanName() {
    return this.workoutPlanName;
  }

  public ArrayList<ArrayList<YouTubeVideo>> getWorkoutPlanPlaylist() {
    return this.workoutPlanPlaylist;
  }

  public int getWorkoutPlanId() {
    return this.workoutPlanId;
  }

  public String getDateCreated() {
    return this.dateCreated;
  }

  public String toGson() {
    return new Gson().toJson(this);
  }
}
