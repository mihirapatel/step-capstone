package com.google.sps.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.List;

/** WorkoutPlan class that stores userId and corresponding workout plan */
public class WorkoutPlan implements Serializable {

  private String userId;
  private List<List<YouTubeVideo>> workoutPlanPlaylist;

  public WorkoutPlan(String userId, List<List<YouTubeVideo>> workoutPlanPlaylist) {
    this.userId = userId;
    this.workoutPlanPlaylist = workoutPlanPlaylist;
  }

  public String getUserId() {
    return this.userId;
  }

  public List<List<YouTubeVideo>> getWorkoutPlanPlaylist() {
    return this.workoutPlanPlaylist;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
