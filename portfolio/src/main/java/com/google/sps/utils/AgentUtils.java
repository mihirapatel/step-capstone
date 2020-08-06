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

// Imports the Google Cloud client library
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.log.InvalidRequestException;
import com.google.appengine.api.users.UserService;
import com.google.cloud.translate.TranslateException;
import com.google.maps.errors.ApiException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Value;
import com.google.sps.agents.Agent;
import com.google.sps.agents.BooksAgent;
import com.google.sps.agents.CurrencyAgent;
import com.google.sps.agents.DateAgent;
import com.google.sps.agents.LanguageAgent;
import com.google.sps.agents.MapsAgent;
import com.google.sps.agents.MemoryAgent;
import com.google.sps.agents.NameAgent;
import com.google.sps.agents.PresentationAgent;
import com.google.sps.agents.RemindersAgent;
import com.google.sps.agents.TimeAgent;
import com.google.sps.agents.TipAgent;
import com.google.sps.agents.TranslateAgent;
import com.google.sps.agents.UnitConverterAgent;
import com.google.sps.agents.WeatherAgent;
import com.google.sps.agents.WebSearchAgent;
import com.google.sps.agents.WorkoutAgent;
import com.google.sps.data.DialogFlowClient;
import com.google.sps.data.Output;
import com.google.sps.data.RecommendationsClient;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Identifies agent from Dialogflow API Query result and creates Output object */
public class AgentUtils {

  public static String detectedInput;
  private static UserService userService;
  private static DatastoreService datastore;
  private static RecommendationsClient recommender;
  private static Logger log = LoggerFactory.getLogger(AgentUtils.class);
  public static final String DEFAULT_FALLBACK =
      "I'm sorry, I didn't catch that. Can you repeat that?";

  /**
   * Method that creates and returns an Output object which is passed to frontend JS that
   * fulfillment and display info for user requests. If no backend fulfillment is necessary for the
   * given intent, it defaults to an Output object containing the standard Dialogflow response. If
   * the intent cannot be understood, it returns an Output object with a default fulfillment stating
   * that intent was not heard.
   *
   * @param queryResult DialogFlowClient object which contains all attributes of Dialogflow's intent
   *     detection.
   * @param languageCode String containing the language to use for the audio file returned in the
   *     Output object.
   * @param userServiceInput UserService instance to access userID and other user info if necessary.
   * @param datastoreInput DatastoreService instance used to access past comments from the user's
   *     database if necessary.
   * @param sessionID unique sessionID for current session of AIssistant running used to store
   *     BookQuery and book results for users who are not logged in
   * @param recommenderInput Recommendations Client instance for calling recommendations API
   * @return Output object containing all output audio, text, and display information.
   */
  public static Output getOutput(
      DialogFlowClient queryResult,
      String languageCode,
      UserService userServiceInput,
      DatastoreService datastoreInput,
      String sessionID,
      RecommendationsClient recommenderInput) {
    String fulfillment = null;
    String display = null;
    String redirect = null;
    byte[] byteStringToByteArray = null;
    Agent object = null;

    String detectedIntent = queryResult.getIntentName();
    Boolean allParamsPresent = queryResult.getAllRequiredParamsPresent();
    String agentName = getAgentName(detectedIntent);
    String intentName = getIntentName(detectedIntent);
    userService = userServiceInput;
    datastore = datastoreInput;
    recommender = recommenderInput;

    // Retrieve detected input from DialogFlow result.
    detectedInput = queryResult.getQueryText();
    if (detectedInput.equals("")) {
      return null;
    }
    Map<String, Value> parameterMap = queryResult.getParameters();

    // Default fulfillment if all required parameters are not present
    fulfillment = queryResult.getFulfillmentText();

    // Set fulfillment if parameters are present, upon any exceptions return default
    if (allParamsPresent) {
      try {
        object = createAgent(agentName, intentName, detectedInput, parameterMap, sessionID);
        fulfillment = object.getOutput();
        fulfillment = fulfillment == null ? queryResult.getFulfillmentText() : fulfillment;
        display = object.getDisplay();
        redirect = object.getRedirect();
      } catch (IllegalStateException
          | IOException
          | ApiException
          | InterruptedException
          | IllegalArgumentException
          | ArrayIndexOutOfBoundsException
          | NullPointerException
          | TranslateException
          | InvalidRequestException
          | EntityNotFoundException
          | URISyntaxException e) {
        log.info("Error in object creation.");
        e.printStackTrace();
      }
    }
    if (fulfillment.equals("")) {
      fulfillment = DEFAULT_FALLBACK;
    }
    if (userService.isUserLoggedIn()) {
      MemoryUtils.saveComment(
          userService.getCurrentUser().getUserId(), datastore, detectedInput, fulfillment);
    }
    byteStringToByteArray = getByteStringToByteArray(fulfillment, languageCode);
    Output output =
        new Output(
            detectedInput, fulfillment, byteStringToByteArray, display, redirect, detectedIntent);
    return output;
  }

  /**
   * Creates the appropriate agent according to the input agent information.
   *
   * @param agentName Name of the agent to be created
   * @param intentName Intent corresponding to the agent
   * @param queryText Textual user input
   * @param parameterMap Map containing the detected entities in the user's intent
   * @param sessionID The current user's session ID
   * @return Created agent
   */
  private static Agent createAgent(
      String agentName,
      String intentName,
      String queryText,
      Map<String, Value> parameterMap,
      String sessionID)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException, InvalidRequestException, EntityNotFoundException,
          URISyntaxException {
    switch (agentName) {
      case "books":
        return new BooksAgent(
            intentName, queryText, parameterMap, sessionID, userService, datastore);
      case "calculator":
        return new TipAgent(intentName, parameterMap);
      case "currency":
        return new CurrencyAgent(intentName, parameterMap);
      case "date":
        return new DateAgent(intentName, parameterMap);
      case "language":
        return new LanguageAgent(intentName, parameterMap);
      case "maps":
        return new MapsAgent(intentName, parameterMap);
      case "memory":
        return new MemoryAgent(intentName, parameterMap, userService, datastore, recommender);
      case "name":
        return new NameAgent(intentName, parameterMap, userService, datastore);
      case "reminders":
        return new RemindersAgent(intentName, parameterMap);
      case "time":
        return new TimeAgent(intentName, parameterMap);
      case "translate":
        return new TranslateAgent(intentName, parameterMap);
      case "units":
        return new UnitConverterAgent(intentName, parameterMap);
      case "weather":
        return new WeatherAgent(intentName, parameterMap);
      case "web":
        return new WebSearchAgent(intentName, parameterMap);
      case "workout":
        return new WorkoutAgent(intentName, parameterMap, userService, datastore);
      case "presentation":
        return new PresentationAgent();
      default:
        return null;
    }
  }

  /**
   * Retrieves the agent's name from the detected intent from dialogflow
   *
   * @param detectedIntent Full detected intent string
   * @return Name of the agent corresponding to the intent
   */
  private static String getAgentName(String detectedIntent) {
    String[] intentList = detectedIntent.split("\\.", 2);
    return intentList[0];
  }

  /**
   * Retrieves the specific intent name from the detected intent from dialogflow
   *
   * @param detectedIntent Full detected intent string
   * @return Name of the specific intent within the full intent string
   */
  public static String getIntentName(String detectedIntent) {
    String[] intentList = detectedIntent.split("\\.", 2);
    String intentName = detectedIntent;
    if (intentList.length > 1) {
      return intentList[1];
    }
    return intentName;
  }

  /**
   * Retrieves the raw textual user input.
   *
   * @return Textual form of user input to determined by speech to text
   */
  public static String getUserInput() {
    return detectedInput;
  }

  /**
   * Creates audio file byte array for audio output
   *
   * @param fulfillment String containing textual response from assistant
   * @param languageCode Two-letter representation of output audio language
   * @return byte array containing the output audio recording
   */
  public static byte[] getByteStringToByteArray(String fulfillment, String languageCode) {
    byte[] byteArray = null;
    try {
      ByteString audioResponse = SpeechUtils.synthesizeText(fulfillment, languageCode);
      byteArray = audioResponse.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return byteArray;
  }

  /**
   * Converts string corresponding to the dialogue language into its corresponding language code
   *
   * @param language Full extual representation of language string
   * @return Two-letter code representation of input language
   */
  public static String getLanguageCode(String language) {
    if (language == null) {
      return "en-US";
    }

    switch (language) {
      case "Chinese":
        return "zh-CN";
      case "English":
        return "en-US";
      case "French":
        return "fr";
      case "German":
        return "de";
      case "Hindi":
        return "hi";
      case "Italian":
        return "it";
      case "Japanese":
        return "ja";
      case "Korean":
        return "ko";
      case "Portuguese":
        return "pt";
      case "Russian":
        return "ru";
      case "Spanish":
        return "es";
      case "Swedish":
        return "sv";
      default:
        return null;
    }
  }
}
