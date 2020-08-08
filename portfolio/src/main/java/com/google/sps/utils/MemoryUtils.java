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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.log.InvalidRequestException;
import com.google.gson.Gson;
import com.google.protobuf.Value;
import com.google.sps.agents.MemoryAgent;
import com.google.sps.data.Pair;
import com.google.sps.data.RecommendationsClient;
import com.google.sps.servlets.BookAgentServlet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class MemoryUtils {

  private static Logger log = LoggerFactory.getLogger(MemoryUtils.class);
  public static final List<String> AGG_ENTITY_ID_PROPERTIES =
      Arrays.asList("userID", "timestamp", "count", "listName");

  /**
   * Saves comment information into comment history database if the user is logged in.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to retrieve stored comment information
   * @param userComment The comment written by the user.
   * @param assistantComment The fulfillment comment returned by the assistant.
   */
  public static void saveComment(
      String userID, DatastoreService datastore, String userComment, String assistantComment) {
    if (userID != null) {
      makeCommentEntity(userID, datastore, userComment, true);
      makeCommentEntity(userID, datastore, assistantComment, false);
    }
  }

  /**
   * Creates a comment entity and stores it in the given user's database
   *
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to store new comments
   * @param comment String comment to be stored.
   * @param isUser Boolean indicating whether the comment was said by user or assistant.
   */
  public static void makeCommentEntity(
      String userID, DatastoreService datastore, String comment, boolean isUser) {
    makeCommentEntity(userID, datastore, comment, isUser, System.currentTimeMillis());
  }

  /**
   * Creates a comment entity with a given timestamp and stores it in the given user's database.
   * Should only be called for testing purposes.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to store new comments
   * @param comment String comment to be stored.
   * @param isUser Boolean indicating whether the comment was said by user or assistant.
   * @param timeMillis Timestamp to assign to the comment.
   */
  public static void makeCommentEntity(
      String userID, DatastoreService datastore, String comment, boolean isUser, long timeMillis) {
    Entity entity = new Entity("CommentHistory");
    entity.setProperty("userID", userID);
    entity.setProperty("isUser", isUser);
    entity.setProperty("comment", comment);
    entity.setProperty("errorResponse", !isUser && comment.equals(AgentUtils.DEFAULT_FALLBACK));
    entity.setProperty("timestamp", timeMillis);
    datastore.put(entity);
    log.info(new Gson().toJson(entity));
  }

  /**
   * Retrieves a list of (Entity, List<Entity>)-pairs where the Entity (pair key) is a datastore
   * entity with a comment containing the desired keyword and the List<Entity> (pair value) is a
   * list of datastore entities containing comments that are around the chosen Entity.
   *
   * @param datastore Datastore instance to used to retrieve past comment history
   * @param userID String containing current user's unique ID
   * @param keyword The fulfillment comment returned by the assistant.
   * @return List of pairs where key corresponds to identified entity with keyword and value is a
   *     list of surrounding entities
   */
  public static List<Pair<Entity, List<Entity>>> getKeywordCommentEntities(
      DatastoreService datastore, String userID, String keyword) {
    Filter currentUserFilter = new FilterPredicate("userID", FilterOperator.EQUAL, userID);
    return getCommentListHelper(datastore, currentUserFilter, keyword);
  }

  /**
   * Retrieves a list of (Entity, List<Entity>)-pairs where the Entity (pair key) is a datastore
   * entity within the time range [startTime, endTime] with a comment containing the desired keyword
   * and the List<Entity> (pair value) is a list of datastore entities containing comments that are
   * around the chosen Entity.
   *
   * @param datastore Datastore instance to used to retrieve past comment history
   * @param userID String containing current user's unique ID
   * @param keyword The fulfillment comment returned by the assistant.
   * @param startTime Start time of the period to query for.
   * @param endTime End time of the period to query for.
   * @return List of pairs where key corresponds to identified entity with keyword and value is a
   *     list of surrounding entities
   */
  public static List<Pair<Entity, List<Entity>>> getKeywordCommentEntitiesWithTime(
      DatastoreService datastore, String userID, String keyword, long startTime, long endTime) {
    Filter currentUserFilter = getDurationFilter(userID, startTime, endTime);
    return getCommentListHelper(datastore, currentUserFilter, keyword);
  }

  /**
   * Helper function to retrieve a list of (Entity, List<Entity>)-pairs where the Entity (pair key)
   * is a datastore entity with the desired keyword passing the provided filter and the List<Entity>
   * (pair value) is a list of datastore comment entities that are around the chosen Entity.
   *
   * @param datastore Datastore instance to used to retrieve past comment history
   * @param queryFilter Filter for valid comment entities to be retrieved from databaser
   * @param keyword The fulfillment comment returned by the assistant.
   * @return List of pairs where key corresponds to identified entity with keyword and value is a
   *     list of surrounding entities
   */
  private static List<Pair<Entity, List<Entity>>> getCommentListHelper(
      DatastoreService datastore, Filter queryFilter, String keyword) {
    Query query =
        new Query("CommentHistory")
            .setFilter(queryFilter)
            .addSort("timestamp", SortDirection.ASCENDING);

    List<Pair<Entity, List<Entity>>> keywordEntities = new ArrayList<>();
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    for (int i = 0; i < results.size(); i++) {
      Entity entity = results.get(i);
      String comment = (String) entity.getProperty("comment");
      if (comment.toLowerCase().contains(keyword)) {
        keywordEntities.add(new Pair(entity, getSurroundingConversation(results, i)));
      }
    }
    return keywordEntities;
  }

  /**
   * Given a list of entities and an index representing the identified keyword comment entity in the
   * results list, returns a lit of up to 7 comment entities that neighbor the keyword comment.
   *
   * @param results List of comment entities that forms an entire conversation
   * @param index Index of the identified comment in results for which we want to form a list of
   *     surrounding comment entities
   * @param List of up to 7 comment entities around the provided indexed entity
   */
  private static List<Entity> getSurroundingConversation(List<Entity> results, int index) {
    List<Entity> surroundingEntities = new ArrayList<>();
    for (int i = Math.max(index - 6, 0); i < Math.min(index + 7, results.size()); i++) {
      surroundingEntities.add(results.get(i));
    }
    return surroundingEntities;
  }

  /**
   * Retrieves a list of entities within the specified time range.
   *
   * @param datastore Datastore instance to used to retrive past comment history
   * @param userID String containing current user's unique ID
   * @param startTime A long indicating the end of time range (represented in ms after 1970)
   * @param endTime A long indicating the end of time range (represented in ms after 1970)
   * @return List of comment entities forming the conversation within provided timeframe
   */
  public static List<Entity> getTimePeriodCommentEntities(
      DatastoreService datastore, String userID, long startTime, long endTime) {
    Filter currentUserFilter = getDurationFilter(userID, startTime, endTime);
    Query query =
        new Query("CommentHistory")
            .setFilter(currentUserFilter)
            .addSort("timestamp", SortDirection.ASCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Creates a duration filter from the given user ID and start and end times.
   *
   * @param userID String containing current user's unique ID
   * @param startTime A long indicating the end of time range (represented in ms after 1970)
   * @param endTime A long indicating the end of time range (represented in ms after 1970)
   * @return A filter with the given input constraints
   */
  private static Filter getDurationFilter(String userID, long startTime, long endTime) {
    return new CompositeFilter(
        CompositeFilterOperator.AND,
        Arrays.asList(
            new FilterPredicate("userID", FilterOperator.EQUAL, userID),
            new FilterPredicate("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, startTime),
            new FilterPredicate("timestamp", FilterOperator.LESS_THAN_OR_EQUAL, endTime)));
  }

  /**
   * Retrieves all past lists of the given user according to the input parameters
   *
   * @param datastore Datastore instance to used to retrieve past lists
   * @param userID String containing current user's unique ID
   * @param parameters Map containing the detected entities in the user's intent.
   * @return List of List-entities matching the user's input parameters
   */
  public static List<Entity> getPastUserLists(
      DatastoreService datastore, String userID, Map<String, Value> parameters) {
    Filter filter = makeFilters(parameters, userID, true);
    List<Entity> listQuery = pastListHelper(datastore, filter);
    if (listQuery.size() == 0) {
      filter = makeFilters(parameters, userID, false);
      listQuery = pastListHelper(datastore, filter);
    }
    String maxValue = parameters.get("number").getStringValue();
    if (!maxValue.equals("-1")) {
      return listQuery.subList(
          0, Math.min(listQuery.size(), (int) parameters.get("number").getNumberValue()));
    }
    log.info("past list query: " + listQuery);
    return listQuery;
  }

  /**
   * Creates a list query filter according to the available constraints provided in the input
   * parameters
   *
   * @param userID String containing current user's unique ID
   * @param parameters Map containing the detected entities in the user's intent.
   * @param tryName Boolean indicating whether or not to add the list name as a filter constraint
   */
  private static Filter makeFilters(Map<String, Value> parameters, String userID, boolean tryName)
      throws InvalidRequestException {
    List<Filter> filters = new ArrayList<>();
    filters.add(new FilterPredicate("userID", FilterOperator.EQUAL, userID));
    String listNameValue = parameters.get("list-name").getStringValue();
    listNameValue = MemoryAgent.cleanName(listNameValue);
    if (tryName && !listNameValue.isEmpty()) {
      filters.add(
          new FilterPredicate(
              "stemmedListName", FilterOperator.EQUAL, StemUtils.stemmed(listNameValue)));
    }
    Value durationValue = parameters.get("date-time-enhanced");
    if (durationValue.hasStructValue()) {
      try {
        Pair<Long, Long> timeRange = TimeUtils.getTimeRange(durationValue);
        filters.add(
            new FilterPredicate(
                "timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, timeRange.getKey()));
        filters.add(
            new FilterPredicate(
                "timestamp", FilterOperator.LESS_THAN_OR_EQUAL, timeRange.getValue()));
      } catch (ParseException e) {
        log.error("Parse error in date-time parameter", e);
        throw new InvalidRequestException("Parse error in date-time parameter");
      }
    }
    if (filters.size() == 1) {
      return filters.get(0);
    } else {
      return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }
  }

  /**
   * Helper method that implements a simple query for all list items matching the filter
   * constraints.
   *
   * @param datastore Datastore instance to used to retrieve past lists
   */
  private static List<Entity> pastListHelper(DatastoreService datastore, Filter filter) {
    Query query =
        new Query("List").setFilter(filter).addSort("timestamp", SortDirection.DESCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Creates an entity object for the new list if non exists. If a list already exists of the same
   * name, change the previous list to be named "<list> (timestamp)" to differentiate archived
   * lists.
   *
   * @param listName The name of the list being created.
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to store new lists
   * @param items List of strings containing items to add to list
   * @param recommender Recommendations API client for recommendation services.
   */
  public static void allocateList(
      String listName,
      String userID,
      DatastoreService datastore,
      ArrayList<String> items,
      RecommendationsClient recommender) {
    String stemmedListName = StemUtils.stemmed(listName);
    List<Entity> existingList = fetchExistingListQuery(datastore, userID, stemmedListName);
    if (!existingList.isEmpty()) {
      Entity existingEntity = existingList.get(0);
      long timestamp = (long) existingEntity.getProperty("timestamp");
      try {
        String timeString = TimeUtils.secondsToDateString(timestamp);
        existingEntity.setProperty(
            "listName",
            ((String) existingEntity.getProperty("listName")) + " (" + timeString + ")");
      } catch (ParseException e) {
        existingEntity.setProperty(
            "listName",
            ((String) existingEntity.getProperty("listName"))
                + " ("
                + String.valueOf(timestamp)
                + ")");
      }
      datastore.put(existingEntity);
      log.info(new Gson().toJson(existingEntity));
    }
    addListItems(datastore, userID, items, listName, recommender);
  }

  /**
   * Add items to the existing list in the user's database. If successful (list is found and new
   * items are added), returns true. Else, return false if no existing list was found and adds items
   * to a brand new list.
   *
   * @param listName The name of the list being created.
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to retrieve past lists
   * @param items List of strings containing items to add to list
   * @param recommender Recommendations API client for recommendation services.
   */
  public static boolean addToList(
      String listName,
      String userID,
      DatastoreService datastore,
      List<String> items,
      RecommendationsClient recommender)
      throws InvalidRequestException {
    String stemmedListName = StemUtils.stemmed(listName);
    List<Entity> existingList = fetchExistingListQuery(datastore, userID, stemmedListName);
    if (existingList.isEmpty()) {
      addListItems(datastore, userID, items, listName, recommender);
      return false;
    }
    Entity existingEntity = existingList.get(0);
    ArrayList<String> existingItems = (ArrayList<String>) existingEntity.getProperty("items");
    if (existingItems != null) {
      recommender.saveAggregateListData(stemmedListName, items, false, true);
      existingItems.addAll(items);
      existingEntity.setProperty("items", existingItems);
    } else {
      existingEntity.setProperty("items", items);
      recommender.saveAggregateListData(stemmedListName, items, true, true);
    }
    datastore.put(existingEntity);
    log.info(new Gson().toJson(existingEntity));
    return true;
  }

  /**
   * Creates a comment entity with a given timestamp and stores it in the given user's database.
   * Should only be called for testing purposes.
   *
   * @param datastore Datastore instance to used to store new list entities
   * @param userID String containing current user's unique ID
   * @param items List of strings containing items to add to list
   * @param listName The name of the list being created.
   * @param timeString Timestamp to assign to the comment.
   */
  public static void makeListEntity(
      DatastoreService datastore,
      String userID,
      List<String> items,
      String listName,
      long timestamp) {
    Entity entity = new Entity("List");
    entity.setProperty("listName", listName);
    entity.setProperty("stemmedListName", StemUtils.stemmed(listName));
    entity.setProperty("userID", userID);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("items", items);
    datastore.put(entity);
    log.info(new Gson().toJson(entity));
  }

  /**
   * Adds the provided list items into the user's list database.
   *
   * @param datastore Datastore instance to used to store new list entities
   * @param userID String containing current user's unique ID
   * @param items List of strings containing items to add to list
   * @param listName The name of the list being created.
   * @param recommender Recommendations API client for recommendation services.
   */
  private static void addListItems(
      DatastoreService datastore,
      String userID,
      List<String> items,
      String listName,
      RecommendationsClient recommender)
      throws InvalidRequestException {
    makeListEntity(datastore, userID, items, listName, System.currentTimeMillis());
    if (items != null && items.size() > 0) {
      recommender.saveAggregateListData(StemUtils.stemmed(listName), items, true, true);
    }
  }

  /**
   * Fetches all lists created by the current user with the given stemmed List name. List is
   * returned with the most recently created first.
   *
   * @param datastore Datastore instance to used to retrieve past lists
   * @param userID String containing current user's unique ID
   * @param stemmedListName stemmed name of the list to query for
   * @return A list of all past lists of the same stemmed name for the given user with the most
   *     recent first.
   */
  private static List<Entity> fetchExistingListQuery(
      DatastoreService datastore, String userID, String stemmedListName) {
    Filter filter =
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate("userID", FilterOperator.EQUAL, userID),
                new FilterPredicate("stemmedListName", FilterOperator.EQUAL, stemmedListName)));
    Query query =
        new Query("List").setFilter(filter).addSort("timestamp", SortDirection.DESCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Stores the integer aggregate count of number of times user has placed a given item in a list.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
   * @param stemmedListName The stemmed name of the list to store aggregation information for.
   * @param items List of strings containing items that were newly added.
   * @param newList Indicates whether the list is a new list (true) or updating existing (false)
   */
  public static void saveAggregateListData(
      DatastoreService datastore,
      String userID,
      String stemmedListName,
      List<String> items,
      boolean newList)
      throws InvalidRequestException {
    log.info("making storeInfo api request");
    RestTemplate restTemplate = new RestTemplate();
    String urlString = "https://arliu-step-2020-3.wl.r.appspot.com/storeInfo?userID=" + userID +
    "&stemmedListName=" + stemmedListName + "&newList=" + newList;
    HttpEntity<List<String>> entity = new HttpEntity<>(items);
    log.info("http entity: " + entity.getBody());
    ResponseEntity<Void> result = restTemplate.exchange(urlString, HttpMethod.POST, entity,
    Void.class);
    if (result.getStatusCode() != HttpStatus.OK) {
      throw new InvalidRequestException("Error sending info to recommendations API.");
    }
    log.info("storeInfo success");
  }

  /**
   * Makes recommendations based on the user's past history of list items. Will only make
   * recommendations if the user has at least 3 lists of the same name. Recommendations are made
   * based on the top 3 items that the user has placed on at least 50% of previous lists.
   *
   * @param userID String containing current user's unique ID
   * @param listName Name of the list we are providing item recommendations for.
   * @param recommender Recommendations API client for recommendation services.
   * @return String containing the suggested list items to recommend to the user
   */
  public static String makePastRecommendations(
      String userID, String listName, RecommendationsClient recommender)
      throws EntityNotFoundException, IllegalStateException, URISyntaxException {
    List<Pair<String, Double>> itemPairs =
        recommender.getPastRecommendations(StemUtils.stemmed(listName));
    List<String> formattedResult = filterTopResults(itemPairs);
    return getSuggestedItems(formattedResult);
  }

  /**
   * Finds items to recommend to the current user based on interests of the current user in relation
   * to other users. If the current user does not have any history of interests in the database,
   * method will throw EntityNotFoundException.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to retrieve current list items
   * @param listName Name of the list we are providing recommendations for.
   * @param recommender Recommendations API client for recommendation services.
   * @return String containing up to 3 items to recommend to the user.
   */
  public static String makeUserRecommendations(
      String userID, DatastoreService datastore, String listName, RecommendationsClient recommender)
      throws IllegalStateException, EntityNotFoundException, URISyntaxException {
    String stemmedListName = StemUtils.stemmed(listName);
    List<String> stemmedCurrentListItems = getCurrentItems(userID, datastore, stemmedListName);

    List<Pair<String, Double>> itemPairs = recommender.getUserRecommendations(stemmedListName);
    List<String> formattedResult = filterTopResults(itemPairs, stemmedCurrentListItems);
    return getSuggestedItems(formattedResult);
  }

  /**
   * Calls recommendations API to get any possible list item recommendations for the user. Throws
   * URISyntaxException if there is an error in URI creation. Otherwise, if no item suggestions
   * exist, returns an empty list.
   *
   * @param methodName String name of the type of recommendation requested (pastUser or generalUser)
   * @param userID String representing the ID of the user giving recommendations for
   * @param stemmedListName Stemmed name of the list we are providing recommendations for.
   * @return String containing up to 3 items to recommend to the user.
   */
  private static List<Pair<String, Double>> callRecommendationsAPI(
      String methodName, String userID, String stemmedListName) throws URISyntaxException {
    log.info("making pastUserRecs api request");
    RestTemplate restTemplate = new RestTemplate();
    String urlString = "https://arliu-step-2020-3.wl.r.appspot.com/" + methodName + "?userID=" +
    userID + "&stemmedListName=" + stemmedListName;
    URI uri = new URI(urlString);
    ResponseEntity<List> result = restTemplate.getForEntity(uri, List.class);
    if (result.getStatusCode() != HttpStatus.OK) {
      throw new InvalidRequestException("Error sending info to recommendations API.");
    }
    log.info("pastUserRecs success");
    List<LinkedHashMap<String, Double>> resultList = result.getBody();
    Gson gson = new Gson();
    List<Pair<String, Double>> formattedList = resultList.stream().map(e ->
    makePair(e)).collect(Collectors.toList());
    return formattedList;
  }

  private static Pair<String, Double> makePair(LinkedHashMap e) {
    String key = (String) e.get("key");
    double value = (double) e.get("value");
    return new Pair<>(key, value);
  }

  /**
   * Creates a formatted string of suggested items based on the elements in the PQ.
   *
   * @param items List of strings containing items to add to list
   * @return String containing a formatted list of items to recommend to the user
   */
  private static String getSuggestedItems(List<String> items) throws IllegalStateException {
    if (items == null || items.isEmpty()) {
      throw new IllegalStateException("No recommendations are available");
    }
    if (items.size() == 1) {
      return items.get(0);
    }
    if (items.size() == 2) {
      return String.format("%s and %s", items.get(0), items.get(1));
    } else {
      return String.format("%s, %s, and %s", items.get(0), items.get(1), items.get(2));
    }
  }

  /**
   * Creates a list of up to 3 items that contain the highest point value that are not in the
   * existing items list for general user recommendations.
   *
   * @param items List of strings containing items to add to list
   * @param existingItems List of items already in the user's current list
   * @return Up to 3 top items above the given 0.4 point threshold that are not in the current list
   */
  private static List<String> filterTopResults(
      List<Pair<String, Double>> items, List<String> existingItems) {
    List<String> filteredUserInterest =
        items.stream()
            .filter(
                e ->
                    (!existingItems.contains(StemUtils.stemmed(e.getKey()))
                        && (e.getValue() > 0.4)))
            .map(e -> e.getKey())
            .collect(Collectors.toList());
    return filteredUserInterest.subList(0, Math.min(3, filteredUserInterest.size()));
  }

  /**
   * Creates a list of up to 3 items that contain the highest point value above the 0.49 threshold
   *
   * @param items List of strings containing items to add to list
   * @return Up to 3 top items above the given 0.49 point threshold
   */
  private static List<String> filterTopResults(List<Pair<String, Double>> items) {
    List<String> filteredUserInterest =
        items.stream()
            .filter(e -> (e.getValue() > 0.49))
            .map(e -> e.getKey())
            .collect(Collectors.toList());
    return filteredUserInterest.subList(0, Math.min(3, filteredUserInterest.size()));
  }

  /**
   * Returns a list of all items currently in the user's list.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to retrieve current list items
   * @param stemmedListName stemmed name of the list to find items for
   * @return List of strings containing stemmed names of all items in the current list
   */
  private static List<String> getCurrentItems(
      String userID, DatastoreService datastore, String stemmedListName)
      throws IllegalStateException {
    List<Entity> allPastLists = fetchExistingListQuery(datastore, userID, stemmedListName);
    if (allPastLists.isEmpty()) {
      throw new IllegalStateException(
          "No past lists exist of the given stemmed name \""
              + stemmedListName
              + "\" for user "
              + userID);
    }
    Entity currentList = allPastLists.get(0);
    List<String> listItems = (List<String>) currentList.getProperty("items");
    if (listItems == null) {
      throw new IllegalStateException(
          "No items in the current list of stemmed name \""
              + stemmedListName
              + "\" for user "
              + userID);
    }
    return StemUtils.stemmedList(listItems);
  }

  /**
   * Retrieves the recommendations made in the most recent valid past assistant comment.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to store new list entities
   * @return List of items that were previously recommended.
   */
  public static List<String> getRecommendations(String userID, DatastoreService datastore)
      throws IllegalStateException {
    Filter queryFilter =
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate("userID", FilterOperator.EQUAL, userID),
                new FilterPredicate("errorResponse", FilterOperator.EQUAL, false),
                new FilterPredicate("isUser", FilterOperator.EQUAL, false)));
    Query query =
        new Query("CommentHistory")
            .setFilter(queryFilter)
            .addSort("timestamp", SortDirection.DESCENDING);
    Entity entity = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults()).get(0);
    String lastComment = (String) entity.getProperty("comment");

    Pattern pattern =
        Pattern.compile("(would you like to add|might be interested in adding) (.*?)(\\?| to)");
    Matcher matcher = pattern.matcher(lastComment);
    if (matcher.find()) {
      return MemoryAgent.unpackObjects(matcher.group(2));
    }
    throw new IllegalStateException("Most recent valid response was not a recommendation");
  }

  /**
   * Calls recommender API to record negative feedback to list item recommendations.
   *
   * @param userID String containing current user's unique ID
   * @param listName Name of the list we are providing recommendations for.
   * @param items List of strings containing items to add to list
   */
  public static void provideNegativeFeedback(
      RecommendationsClient recommender, String listName, List<String> items) {
    recommender.saveAggregateListData(StemUtils.stemmed(listName), items, false, false);
  }

  /**
   * Method that prepopulates database with default entities recorded in resource files
   *
   * @param datastore Datastore instance to used to store new list entities
   */
  public static void seedDatabase(DatastoreService datastore) {
    URL url = MemoryUtils.class.getResource("/dbEntities");
    String path = url.getPath();
    File[] allDBFiles = new File(path).listFiles();
    Arrays.sort(allDBFiles);
    Gson gson = new Gson();
    boolean checkExisting = true;
    for (File file : allDBFiles) {
      log.info("file: " + file.getName());
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
          log.info("line: " + line);
          char firstCh = line.charAt(0);
          if (firstCh == '#') {
            continue;
          } else if (firstCh != '{') {
            break;
          }
          Entity e = gson.fromJson(line, Entity.class);
          Map<String, Value> keyMap =
              BookAgentServlet.stringToMap(line).get("key").getStructValue().getFieldsMap();
          Entity entity =
              new Entity(
                  (String) keyMap.get("kind").getStringValue(), keyMap.get("id").getStringValue());
          entity.setPropertiesFrom(e);
          entity.setProperty("timestamp", Long.parseLong((String) entity.getProperty("timestamp")));
          if (checkExisting) {
            checkExisting = false;
            try {
              Entity existing = datastore.get(entity.getKey());
              return;
            } catch (EntityNotFoundException exception) {
              datastore.put(entity);
            }
          } else {
            datastore.put(entity);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
