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

/** WorkoutProfile that has information user using WorkoutAgent: name, email, id */
public class WorkoutProfile {

  private String userId;
  private String userName;
  private String userEmail;

  /**
   * WorkoutProfile contructor to store user information
   *
   * @param userId String userId of current user using workout agent
   * @param userName String userName of current user using workout agent
   * @param userEmail String userEmail of current user using workout agent
   */
  public WorkoutProfile(String userId, String userName, String userEmail) {
    this.userId = userId;
    this.userName = userName;
    this.userEmail = userEmail;
  }

  /** Get Methods */
  public String getUserId() {
    return this.userId;
  }

  public String getUserName() {
    return this.userName;
  }

  public String getUserEmail() {
    return this.userEmail;
  }

  /** Convert WorkoutProfile into Gson string */
  public String toGson() {
    return new Gson().toJson(this);
  }
}
