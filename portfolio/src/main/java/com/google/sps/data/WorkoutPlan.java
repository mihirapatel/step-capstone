package com.google.sps.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;

/** WorkoutPlan class that stores userId and corresponding workout plan */
public class WorkoutPlan implements Serializable {

  private String userId;
  private ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist;
  private int workoutPlanId;

  public WorkoutPlan(ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist) {
    this.workoutPlanPlaylist = workoutPlanPlaylist;
  }

  public WorkoutPlan(
      String userId, ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist, int workoutPlanId) {
    this.userId = userId;
    this.workoutPlanPlaylist = workoutPlanPlaylist;
    this.workoutPlanId = workoutPlanId;
  }

  public String getUserId() {
    return this.userId;
  }

  public ArrayList<ArrayList<YouTubeVideo>> getWorkoutPlanPlaylist() {
    return this.workoutPlanPlaylist;
  }

  public int getWorkoutPlanId() {
    return this.workoutPlanId;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
