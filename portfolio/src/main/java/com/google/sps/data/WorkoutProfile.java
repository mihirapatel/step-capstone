package com.google.sps.data;

import com.google.gson.Gson;
import java.util.ArrayList;

/**
 * WorkoutProfile that has information about everything related to the Workout Agent User
 * information: Name, email Workout information: Workout plans, workout videos completed, minutes
 * worked out
 */
public class WorkoutProfile {

  private String userId;
  private String userName = ""; // TODO
  private String userEmail = ""; // TODO
  private ArrayList<WorkoutPlan> workoutPlans;
  private ArrayList<YouTubeVideo> vidoesWatched = null; // TODO

  public WorkoutProfile(String userId, ArrayList<WorkoutPlan> workoutPlans) {
    this.userId = userId;
    this.workoutPlans = workoutPlans;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getUserName() {
    return this.userName;
  }

  public String getUserEmail() {
    return this.userEmail;
  }

  public ArrayList<WorkoutPlan> getWorkouts() {
    return this.workoutPlans;
  }

  public ArrayList<YouTubeVideo> getVideosWatched() {
    return this.vidoesWatched;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
