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

package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.users.UserService;
import com.google.protobuf.Value;
import com.google.sps.utils.UserUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Name Agent */
public class NameAgent implements Agent {

  private static Logger log = LoggerFactory.getLogger(NameAgent.class);

  private String intentName;
  String outputText;
  String userID;
  String userDisplayName;
  DatastoreService datastore;
  UserService userService;

  /**
   * Name agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   * @param userService UserService instance to access userID and other user info.
   * @param datastore DatastoreService instance used to access past comments from the user's
   *     database.
   */
  public NameAgent(
      String intentName,
      Map<String, Value> parameters,
      UserService userService,
      DatastoreService datastore) {
    this.intentName = intentName;
    this.datastore = datastore;
    this.userService = userService;
    if (!userService.isUserLoggedIn()) {
      outputText = "Please login to modify your name.";
    } else {
      userID = userService.getCurrentUser().getUserId();
      setParameters(parameters);
    }
  }

  /**
   * Method that handles parameter assignment for fulfillment text and display based on the user's
   * input intent and extracted parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters) {
    String nameType = parameters.get("type").getStringValue();
    String name = null;
    nameType = nameType.equals("") ? "first name" : nameType;
    name = getSpecificName(parameters, nameType);
    if (name.equals("")) {
      outputText = "I'm sorry, I didn't catch the name. Can you repeat that?";
    } else {
      UserUtils.saveName(userID, datastore, nameType, name);
      outputText = "Changing your " + nameType + " to be " + name + ".";
      userDisplayName = UserUtils.getDisplayName(userService, datastore);
    }
  }

  /**
   * Retrieves the user's input name, which can appear in any one of the name categories for
   * given-name, last-name, or nick-name depending one which intent dialogflow picks up.
   *
   * @param parameters Map containing the detected entities in the user's intent.
   * @param nameType The type of name that the user wants to change (first, last, nickname)
   */
  private String getSpecificName(Map<String, Value> parameters, String nameType) {
    String name = parameters.get("given-name").getStringValue();
    if (!name.equals("")) {
      return name;
    }
    if (nameType.equals("last name")) {
      return parameters.get("last-name").getStringValue();
    } else if (nameType.equals("nickname")) {
      return parameters.get("nick-name").getStringValue();
    }
    return "";
  }

  @Override
  public String getOutput() {
    return outputText;
  }

  @Override
  public String getDisplay() {
    return userDisplayName;
  }

  @Override
  public String getRedirect() {
    return null;
  }
}
