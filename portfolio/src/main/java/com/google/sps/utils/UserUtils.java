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

package com.google.sps.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.sps.data.Friend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtils {

  private static Logger log = LoggerFactory.getLogger(UserUtils.class);

  /**
   * Returns a string containing the name we should use to address the user. The name returned is
   * determined by info the user has provided in order of: 1. nickname 2. first name (if no
   * nickname) 3. email (if no name is provided) This method assumes that the user is logged in.
   *
   * @param userService UserService instance to access userID and other user info.
   * @param datastore DatastoreService instance used to access book info from database.
   * @return a string containing the name the conversational AI will use to address the user
   */
  public static String getDisplayName(UserService userService, DatastoreService datastore) {
    try {
      Entity entity =
          datastore.get(KeyFactory.createKey("UserInfo", userService.getCurrentUser().getUserId()));
      return getProperName(userService, entity);
    } catch (EntityNotFoundException e) {
      return userService.getCurrentUser().getEmail();
    }
  }

  /**
   * Returns a string containing the name we should use to address the user, when a Friend object
   * containing user information from the Google People API is available. The name returned is
   * determined by info the user has provided in order of: 1. nickname 2. first name (if no
   * nickname) 3. email (if no name is provided) This method assumes that the user is logged in.
   *
   * @param userService UserService instance to access userID and other user info.
   * @param datastore DatastoreService instance used to access book info from database.
   * @param user Friend object containing information about the current user
   * @return a string containing the name the conversational AI will use to address the user
   */
  public static String getDisplayName(
      UserService userService, DatastoreService datastore, Friend user) {
    try {
      Entity entity =
          datastore.get(KeyFactory.createKey("UserInfo", userService.getCurrentUser().getUserId()));
      return getProperName(userService, entity);
    } catch (EntityNotFoundException e) {
      if (user != null && !user.getName().isEmpty()) {
        return user.getName();
      }
      return userService.getCurrentUser().getEmail();
    }
  }

  /**
   * Returns a string url for the profile image associated with the user's gmail account, if any.
   * Otherwise, it returns a generic string url for an andriod icon. This method assumes that the
   * user is logged in.
   *
   * @param userService UserService instance to access userID and other user info.
   * @param datastore DatastoreService instance used to access book info from database.
   * @param user Friend object containing information about the current user
   * @return a string containing the image url for the user's display
   */
  public static String getPhotoUrl(
      UserService userService, DatastoreService datastore, Friend user) {
    if (user != null
        && !user.getPhotoUrl().isEmpty()
        && !user.getPhotoUrl().equals("images/blankAvatar.png")) {
      return user.getPhotoUrl();
    }
    return "images/android.png";
  }

  /**
   * Returns a Friend object containing information about the user from the Google People API, and
   * null otherwise.
   *
   * @param userService UserService instance to access userID and other user info.
   * @param datastore DatastoreService instance used to access book info from database.
   * @return a Friend object containing information about the current user
   */
  public static Friend getUser(UserService userService, DatastoreService datastore) {
    PeopleUtils peopleHelper = new PeopleUtils();
    OAuthHelper oauthHelper = new OAuthHelper();
    Friend user =
        peopleHelper.getUserInfo(
            userService.getCurrentUser().getUserId(), "people/me", oauthHelper);
    return user;
  }

  private static String getProperName(UserService userService, Entity entity) {
    String name = (String) entity.getProperty("nickname");
    if (name == null) {
      name = (String) entity.getProperty("first name");
    }
    if (name == null) {
      name = userService.getCurrentUser().getEmail();
    }
    return name;
  }

  /**
   * Saves name information into user database. This method assumes that the user is logged in or
   * else it will throw an IllegalStateException error.
   *
   * @param nameType String that specifies which part of user's name the given name refers to:
   *     "first name", "middle name", or "last name"
   * @param name String containing the name to be saved
   */
  public static void saveName(
      String userID, DatastoreService datastore, String nameType, String name) {
    Entity entity;
    try {
      entity = datastore.get(KeyFactory.createKey("UserInfo", userID));
    } catch (Exception e) {
      entity = new Entity("UserInfo", userID);
      entity.setProperty("userID", userID);
    }
    entity.setProperty(nameType, name);
    datastore.put(entity);
  }
}
