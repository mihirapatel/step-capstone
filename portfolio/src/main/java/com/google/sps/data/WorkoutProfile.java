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
import java.util.ArrayList;

/**
 * WorkoutProfile that has information about everything related to the Workout Agent User
 * information: Name, email, id Workout information: Workout plans, workout videos completed,
 * minutes worked out
 */
public class WorkoutProfile {

  private String userId;
  private String userName;
  private String userEmail;
  private ArrayList<WorkoutPlan> workoutPlans = null; // TODO
  private ArrayList<YouTubeVideo> vidoesWatched = null; // TODO

  // User Info Constructor
  public WorkoutProfile(String userId, String userName, String userEmail) {
    this.userId = userId;
    this.userName = userName;
    this.userEmail = userEmail;
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

  public String toGson() {
    return new Gson().toJson(this);
  }
}
