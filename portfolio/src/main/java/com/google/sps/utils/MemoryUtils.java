package com.google.sps.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.data.Pair;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryUtils {

  private static Logger log = LoggerFactory.getLogger(MemoryUtils.class);

  /**
   * Saves comment information into comment history database if the user is logged in.
   *
   * @param userID The current logged-in user's ID number
   * @param datastore Datastore instance to retrieve data from
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
   * @param userID The ID corresponding to user who made the comment
   * @param datastore Database instance.
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
   * @param userID The ID corresponding to user who made the comment
   * @param datastore Database instance.
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
    entity.setProperty("timestamp", timeMillis);
    datastore.put(entity);
  }

  /**
   * Retrieves a list of (Entity, List<Entity>)-pairs where the Entity (pair key) is a datastore
   * entity with a comment containing the desired keyword and the List<Entity> (pair value) is a
   * list of datastore entities containing comments that are around the chosen Entity.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
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
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
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

  private static List<Pair<Entity, List<Entity>>> getCommentListHelper(
      DatastoreService datastore, Filter queryFilter, String keyword) {
    Query query = new Query("CommentHistory").setFilter(queryFilter);

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

  private static List<Entity> getSurroundingConversation(List<Entity> results, int index) {
    List<Entity> surroundingEntities = new ArrayList<>();
    for (int i = Math.max(index - 6, 0); i < Math.min(index + 7, results.size()); i++) {
      surroundingEntities.add(results.get(i));
    }
    return surroundingEntities;
  }

  /**
   * Retrieves a list of (Entity, List<Entity>)-pairs where the Entity (pair key) is a datastore
   * entity with a comment containing the desired keyword and the List<Entity> (pair value) is a
   * list of datastore entities containing comments that are around the chosen Entity.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
   * @param startTime A long representing ms since 1970 used to find the comment nearest that time
   * @param endTime A long indicating the end of time range (represented in ms after 1970)
   * @return List of pairs where key corresponds to identified entity with keyword and value is a
   *     list of surrounding entities
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

  private static Filter getDurationFilter(String userID, long startTime, long endTime) {
    return new CompositeFilter(
        CompositeFilterOperator.AND,
        Arrays.asList(
            new FilterPredicate("userID", FilterOperator.EQUAL, userID),
            new FilterPredicate("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, startTime),
            new FilterPredicate("timestamp", FilterOperator.LESS_THAN_OR_EQUAL, endTime)));
  }

  /**
   * Creates an entity object for the new list if non exists. If a list already exists of the same
   * name, change the previous list to be named "<list> (timestamp)" to differentiate archived
   * lists.
   *
   * @param listName The name of the list being created.
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param items List of strings containing items to add to list
   */
  public static void allocateList(
      String listName, String userID, DatastoreService datastore, ArrayList<String> items) {
    List<Entity> existingList = fetchExistingListQuery(datastore, userID, listName);
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
    }
    addListItems(datastore, userID, items, listName);
  }

  /**
   * Add items to the existing list in the user's database. If successful (list is found and new
   * items are added), returns true. Else, return false if no existing list was found and adds items
   * to a brand new list.
   *
   * @param listName The name of the list being created.
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param items List of strings containing items to add to list
   */
  public static boolean addToList(
      String listName, String userID, DatastoreService datastore, ArrayList<String> items) {
    List<Entity> existingList = fetchExistingListQuery(datastore, userID, listName);
    log.info("EXISTING LIST: " + existingList);
    if (existingList.isEmpty()) {
      addListItems(datastore, userID, items, listName);
      return false;
    }
    Entity existingEntity = existingList.get(0);
    ArrayList<String> existingItems = (ArrayList<String>) existingEntity.getProperty("items");
    if (existingItems == null) {
      existingItems = new ArrayList<>();
    }
    existingItems.addAll(items);
    existingEntity.setProperty("items", existingItems);
    datastore.put(existingEntity);
    return true;
  }

  /**
   * Creates a comment entity with a given timestamp and stores it in the given user's database.
   * Should only be called for testing purposes.
   *
   * @param userID The ID corresponding to user who made the comment
   * @param datastore Database instance.
   * @param listName The name of the list being created.
   * @param items List of strings containing items to add to list
   * @param timeString Timestamp to assign to the comment.
   */
  public static void makeListEntity(
      DatastoreService datastore,
      String userID,
      ArrayList<String> items,
      String listName,
      long timestamp) {
    Entity entity = new Entity("List");
    entity.setProperty("listName", listName);
    entity.setProperty("userID", userID);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("items", items);
    datastore.put(entity);
  }

  private static void addListItems(
      DatastoreService datastore, String userID, ArrayList<String> items, String listName) {
    makeListEntity(datastore, userID, items, listName, System.currentTimeMillis());
  }

  private static List<Entity> fetchExistingListQuery(
      DatastoreService datastore, String userID, String listName) {
    Filter filter =
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate("userID", FilterOperator.EQUAL, userID),
                new FilterPredicate("listName", FilterOperator.EQUAL, listName)));
    Query query = new Query("List").setFilter(filter);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }
}
