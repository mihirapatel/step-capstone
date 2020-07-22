package com.google.sps.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;

/** WorkoutPlan class that stores userId and corresponding workout plan */
public class WorkoutPlan implements Serializable {

  private String userId;
  private ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist;

  public WorkoutPlan(String userId, ArrayList<ArrayList<YouTubeVideo>> workoutPlanPlaylist) {
    this.userId = userId;
    this.workoutPlanPlaylist = workoutPlanPlaylist;
  }

  public String getUserId() {
    return this.userId;
  }

  public ArrayList<ArrayList<YouTubeVideo>> getWorkoutPlanPlaylist() {
    return this.workoutPlanPlaylist;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
