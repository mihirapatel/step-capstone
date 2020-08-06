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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.log.InvalidRequestException;
import com.google.appengine.api.users.UserService;
import com.google.protobuf.Value;
import com.google.sps.data.ConversationOutput;
import com.google.sps.data.ListDisplay;
import com.google.sps.data.Pair;
import com.google.sps.data.RecommendationsClient;
import com.google.sps.utils.MemoryUtils;
import com.google.sps.utils.TimeUtils;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Memory Agent Handles storing user info from past conversation history to user lists and providing
 * the user with this stored information on request. Only works if the user is logged in.
 * Conversation history is automatically updated every time the user converses with the assistant
 * and storing/updating lists and notes occurs on user voice command. Finally, with sufficient user
 * data history, provides recommendations for additional list items to add to list.
 */
public class MemoryAgent implements Agent {

  private final String intentName;
  private String userID;
  private String fulfillment;
  private String display;
  private String redirect;
  private DatastoreService datastore;
  private UserService userService;
  private RecommendationsClient recommender;
  private String listName;
  private ArrayList<String> items = new ArrayList<>();
  private static Logger log = LoggerFactory.getLogger(MemoryAgent.class);
  private static final Set<String> unnecessaryWords =
      Stream.of(
              "a", "an", "list", "lists", "new", "the", "my", "last", "past", "recent", "some",
              "of")
          .collect(Collectors.toSet());
  private static final Set<String> negativeWords =
      Stream.of("except", "but", "no", "not", "without", "neither", "nor", "none")
          .collect(Collectors.toSet());
  private static final Set<String> generalWords =
      Stream.of("all", "everything", "only", "them", "those", "items", "it")
          .collect(Collectors.toSet());

  /**
   * Memory agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   * @param userService UserService instance to access userID and other user info.
   * @param datastore DatastoreService instance used to access past comments from the user's
   *     database.
   * @param recommender Recommendations API client for recommendation services.
   */
  public MemoryAgent(
      String intentName,
      Map<String, Value> parameters,
      UserService userService,
      DatastoreService datastore,
      RecommendationsClient recommender)
      throws InvalidRequestException, EntityNotFoundException, URISyntaxException {
    this.intentName = intentName;
    this.userService = userService;
    this.datastore = datastore;
    this.recommender = recommender;
    MemoryUtils.seedDatabase(datastore);
    setParameters(parameters);
  }

  /**
   * Method that handles parameter assignment for fulfillment text and display based on the user's
   * input intent and extracted parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters)
      throws InvalidRequestException, EntityNotFoundException, URISyntaxException {
    log.info("parameters: " + parameters);
    if (!userService.isUserLoggedIn()) {
      fulfillment = "Please login to access user history.";
      return;
    }
    userID = userService.getCurrentUser().getUserId();
    if (intentName.contains("keyword")) {
      findKeyword(parameters);
    } else if (intentName.contains("time")) {
      findTimePeriodComments(parameters);
    } else if (intentName.contains("list")) {
      String[] subIntents = intentName.split("-");
      String subListIntent = subIntents[subIntents.length - 1];
      if (subListIntent.contains("show")) {
        showList(parameters);
        return;
      }
      listName = parameters.get("list-name").getStringValue();
      if (listName.isEmpty()) {
        fulfillment = "What would you like to name the list?";
        return;
      }
      recommender.setUserID(userID);
      listName = cleanName(listName);
      if (subListIntent.contains("no")) {
        handleBadRecommendations();
        return;
      } else if (subListIntent.contains("yes")) {
        handleGoodRecommendations(parameters);
        return;
      }
      unpackParameters(parameters);
      if (subListIntent.contains("make")) {
        makeList(parameters);
      } else if (subListIntent.contains("custom") || subListIntent.contains("add")) {
        updateList(items);
      }
    }
  }

  /**
   * Handles request for conversation history search for a key word.
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void findKeyword(Map<String, Value> parameters) throws InvalidRequestException {
    String word = parameters.get("keyword").getStringValue();
    List<Pair<Entity, List<Entity>>> conversationList;
    String timePeriodDisplay = "";
    try {
      Value dateObject = parameters.get("date-time-enhanced");
      if (dateObject != null && dateObject.hasStructValue()) {
        Pair<Long, Long> timeRange = TimeUtils.getTimeRange(dateObject);
        conversationList =
            MemoryUtils.getKeywordCommentEntitiesWithTime(
                datastore, userID, word.toLowerCase(), timeRange.getKey(), timeRange.getValue());
        timePeriodDisplay = " from " + parameters.get("date-time-original").getStringValue();
      } else {
        conversationList =
            MemoryUtils.getKeywordCommentEntities(datastore, userID, word.toLowerCase());
      }
      if (conversationList.isEmpty()) {
        fulfillment = "Sorry, there were no results matching the keyword \"" + word + ".\"";
      } else {
        fulfillment =
            "Here are all the results"
                + timePeriodDisplay
                + " including the keyword \""
                + word
                + ".\"";
        ConversationOutput convoOutput = new ConversationOutput(word, conversationList);
        display = convoOutput.toString();
      }
    } catch (ParseException e) {
      log.error("Parse error in date-time parameter", e);
      throw new InvalidRequestException("Parse error in date-time parameter");
    }
  }

  /**
   * Handles request for conversation history search for a duration.
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void findTimePeriodComments(Map<String, Value> parameters)
      throws InvalidRequestException {
    try {
      Pair<Long, Long> timeRange = TimeUtils.getTimeRange(parameters.get("date-time-enhanced"));
      List<Entity> conversationSnippet =
          MemoryUtils.getTimePeriodCommentEntities(
              datastore, userID, timeRange.getKey(), timeRange.getValue());
      if (conversationSnippet.isEmpty()) {
        fulfillment =
            "Could not find any conversation from "
                + parameters.get("date-time-original").getStringValue()
                + ".";
      } else {
        fulfillment =
            "Here are all the results from "
                + parameters.get("date-time-original").getStringValue()
                + ".";
        ConversationOutput convoOutput = new ConversationOutput(conversationSnippet);
        display = convoOutput.toString();
      }
    } catch (ParseException e) {
      log.error("Parse error in date-time parameter", e);
      throw new InvalidRequestException("Parse error in date-time parameter");
    }
  }

  /**
   * Handles request for creating a new list that is stored in datastore.
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void makeList(Map<String, Value> parameters)
      throws EntityNotFoundException, URISyntaxException {
    MemoryUtils.allocateList(listName, userID, datastore, items, recommender);
    fulfillment = "Created!";
    if (items.isEmpty()) {
      try {
        String suggestedItems = MemoryUtils.makePastRecommendations(userID, listName, recommender);
        fulfillment +=
            " Based on your previous "
                + listName
                + " lists, would you like to add "
                + suggestedItems
                + "?";
      } catch (EntityNotFoundException | IllegalStateException e) {
        log.error("User recommendation error", e);
        fulfillment += " What are some items to add to your new " + listName + " list?";
      }
      return;
    }
    makeMoreRecommendations();
  }

  /**
   * Handles request for showing the user's stored lists.
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void showList(Map<String, Value> parameters) throws InvalidRequestException {
    List<Entity> pastLists = MemoryUtils.getPastUserLists(datastore, userID, parameters);
    if (pastLists.isEmpty()) {
      fulfillment = "Sorry, no lists were found.";
      return;
    }
    String listInput = parameters.get("list-object").getStringValue();
    if (listInput.charAt(listInput.length() - 1) == 's') {
      List<ListDisplay> allLists = new ArrayList<>();
      for (Entity e : pastLists) {
        allLists.add(entityToListDisplay(e));
      }
      display = (new ListDisplay(allLists)).toString();
      fulfillment = "Here are all the found lists.";
    } else {
      Entity mostRecentList = pastLists.get(0);
      display = (entityToListDisplay(mostRecentList)).toString();
      fulfillment =
          "Here is your most recent "
              + ((String) mostRecentList.getProperty("listName"))
              + " list.";
    }
  }

  /**
   * Converts the given database entity into a ListDisplay object for display output.
   *
   * @param e Entity instance to be converted.
   * @return ListDisplay object that is returned to frontend javascript for display
   */
  private ListDisplay entityToListDisplay(Entity e) {
    return new ListDisplay(
        (String) e.getProperty("listName"), (List<String>) e.getProperty("items"));
  }

  /**
   * Convenience method for updating an existing list with new items. If list doesn't exist, creates
   * a brand new list in datastore.
   *
   * @param itemsToAdd List of strings to add to list.
   */
  private void updateList(List<String> itemsToAdd)
      throws EntityNotFoundException, URISyntaxException {
    updateList(itemsToAdd, true);
  }

  /**
   * Updates an existing list with new items. If list doesn't exist, creates a brand new list in
   * datastore. Creates more recommendations if requested.
   *
   * @param itemsToAdd List of strings to add to list.
   * @param moreRecs Boolean indicating whether or not more recommendations should be made.
   */
  private void updateList(List<String> itemsToAdd, boolean moreRecs)
      throws EntityNotFoundException, URISyntaxException {
    boolean listExists =
        MemoryUtils.addToList(listName, userID, datastore, itemsToAdd, recommender);
    if (!listExists) {
      fulfillment =
          "Your "
              + listName
              + " list has not been created yet, so a new list was created with those items.";
      return;
    }
    fulfillment = "Updated!";
    if (moreRecs) {
      makeMoreRecommendations();
    }
  }

  /**
   * Makes recommendations for items to add to a list when a list is partially populated.
   * Recommendations are made by finding expected item interest among other users with the same list
   * type and recommending those that align most closely with the current user based on other user's
   * trends.
   */
  private void makeMoreRecommendations() throws URISyntaxException {
    try {
      String suggestedItems =
          MemoryUtils.makeUserRecommendations(userID, datastore, listName, recommender);
      fulfillment +=
          " Based on your list item preferences, you might be interested in adding "
              + suggestedItems
              + " to your "
              + listName
              + " list.";
    } catch (IllegalStateException | EntityNotFoundException e) {
      log.error("User recommendation error", e);
    }
  }

  /**
   * Converts a string of list items into a list of strings representing each item and stores it
   * into the items instance variable.
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void unpackParameters(Map<String, Value> parameters) {
    String listObjects = parameters.get("list-objects").getStringValue();
    if (listObjects == null || listObjects.isEmpty()) {
      return;
    }
    items = unpackObjects(listObjects);
  }

  /**
   * Extracts each item in a string of items.
   *
   * @param allItemsString String containing a grammatical list of items.
   * @return List of strings where each element is an item
   */
  public static ArrayList<String> unpackObjects(String allItemsString) {
    String[] commaSplit = allItemsString.split(",[\\s]*[and]*[\\s]+");
    if (commaSplit.length > 0) {
      String[] finalSplit = commaSplit[commaSplit.length - 1].split("[\\s]+and[\\s]+");
      ArrayList<String> listItems = new ArrayList<>(Arrays.asList(commaSplit));
      listItems.remove(commaSplit.length - 1);
      listItems.addAll(new ArrayList<String>(Arrays.asList(finalSplit)));
      return listItems;
    }
    return new ArrayList<>();
  }

  /*
   * Removes any filler words that were picked up in list name detection.
   * ex: "the Monday grocery list" will remove "the" and "list"
   *
   * @param listName name of the list detected by dialogflow
   * @return cleaned version of the list name without extra words
   */
  public static String cleanName(String listName) {
    return cleanStringEndpoints(unnecessaryWords, listName);
  }

  /**
   * Removes any filler words that were picked up in any general string based on the set of unwanted
   * strings to filter out of the string endpoints.
   *
   * @param unwantedStrings set of strings to be removed from start and end of string to clean
   * @param stringToClean The string to be cleaned
   * @return cleaned version of the input string without the unwanted words
   */
  public static String cleanStringEndpoints(Set<String> unwantedStrings, String stringToClean) {
    String[] listWords = stringToClean.split("\\s+");
    int start = 0;
    int end = listWords.length - 1;
    // Remove unnecessary words in the beginning
    while (start < listWords.length && unwantedStrings.contains(listWords[start])) {
      start++;
    }
    // Remove unnecessary words from the end
    while (end >= 0 && unwantedStrings.contains(listWords[end])) {
      end--;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++) {
      sb.append(listWords[i] + " ");
    }
    sb.append(listWords[end]);
    return sb.toString();
  }

  /**
   * Removes any filler words at the beginning and end of a string array.
   *
   * @param unwantedStrings set of strings to be removed from start and end of string to clean
   * @param listWords Array of strings to be cleaned
   * @return cleaned version of the list name without extra words
   */
  public static String cleanStringArrayEndpoints(Set<String> unwantedStrings, String[] listWords) {
    int start = 0;
    int end = listWords.length - 1;
    // Remove unnecessary words in the beginning
    while (unwantedStrings.contains(listWords[start])) {
      start++;
    }
    // Remove unnecessary words from the end
    while (unwantedStrings.contains(listWords[end])) {
      end--;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++) {
      sb.append(listWords[i] + " ");
    }
    sb.append(listWords[end]);
    return sb.toString();
  }

  /**
   * Convenience method for handling negative feedback to system recommendations for cases when the
   * user rejects all previously recommended items.
   */
  private void handleBadRecommendations() throws URISyntaxException {
    handleBadRecommendations(MemoryUtils.getRecommendations(userID, datastore));
  }

  /**
   * Handles response for recommended items that were not wanted.
   *
   * @param unwantedItems List of recommended items that were rejected by user
   */
  private void handleBadRecommendations(List<String> unwantedItems) throws URISyntaxException {
    MemoryUtils.provideNegativeFeedback(recommender, listName, unwantedItems);
    fulfillment = "Your preferences are noted.";
  }

  /**
   * Handles response to positive feedback for recommended items.
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  private void handleGoodRecommendations(Map<String, Value> parameters)
      throws EntityNotFoundException, URISyntaxException {
    String listObjects = parameters.get("yes-objects").getStringValue();
    if (listObjects == null || listObjects.isEmpty()) {
      List<String> recommendedItems = MemoryUtils.getRecommendations(userID, datastore);
      updateList(recommendedItems);
      return;
    }
    String[] listWords = listObjects.split("\\s+");
    List<String> addObjects = new ArrayList<>();
    int addObjectEndIndex = getAddObjects(listWords, addObjects);
    List<String> removeObjects = getRemoveObjects(listWords, addObjectEndIndex);
    if (addObjects.isEmpty()) {
      addObjects = MemoryUtils.getRecommendations(userID, datastore);
      for (String removeItem : removeObjects) {
        addObjects.remove(removeItem);
      }
    }
    updateList(addObjects, removeObjects.isEmpty());
  }

  /**
   * Populates the list of objects to add (second argument) with all items that the user chooses to
   * add (first argument) and then returns the index of the first negative word (or end of the
   * string if no negative word exists).
   *
   * @param listWords String array of words to extract adding items
   * @param addObject Empty arraylist to be filled with items to be added
   * @return int representing the index of the end of adding items
   */
  private int getAddObjects(String[] listWords, List<String> addObject) {
    int start = 0;
    // Remove unnecessary words in the beginning
    while (start < listWords.length
        && (unnecessaryWords.contains(listWords[start])
            || generalWords.contains(listWords[start]))) {
      start++;
    }
    int end = start;
    // Find when statement turns negative to symbolize items that aren't wanted
    while (end < listWords.length && (!negativeWords.contains(listWords[end]))) {
      end++;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++) {
      sb.append(listWords[i] + " ");
    }
    addObject = unpackObjects(sb.toString());
    return end;
  }

  /**
   * Retrieves a list of items that were not wanted among multiple recommended items where the
   * startIndex indicates the beginning of unwanted items in the array of words.
   *
   * @param listWords Users input string represented as an array of words
   * @param startIndex Index for listWords corresponding to the beginning of all unwanted items
   * @return List of items that are not wanted in the list
   */
  private List<String> getRemoveObjects(String[] listWords, int startIndex) {
    if (startIndex == listWords.length) {
      return new ArrayList<>();
    }
    Set<String> unwantedStrings = new HashSet<>(unnecessaryWords);
    unwantedStrings.addAll(negativeWords);
    String cleanedItems =
        cleanStringArrayEndpoints(
            unwantedStrings, Arrays.copyOfRange(listWords, startIndex, listWords.length));
    List<String> unwantedItems = unpackObjects(cleanedItems);
    MemoryUtils.provideNegativeFeedback(recommender, listName, unwantedItems);
    return unwantedItems;
  }

  @Override
  public String getOutput() {
    return fulfillment;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  @Override
  public String getRedirect() {
    return redirect;
  }
}
