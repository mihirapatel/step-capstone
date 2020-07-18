package com.google.sps.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.data.Pair;
import java.text.ParseException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryUtils {

  private static Logger log = LoggerFactory.getLogger(MemoryUtils.class);
  private static final List<String> AGG_ENTITY_ID_PROPERTIES =
      Arrays.asList("userID", "timestamp", "count", "listName");

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
    saveAggregateData(datastore, userID, listName, items, true);
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
    log.info("Making new list");
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
    if (existingList.isEmpty()) {
      addListItems(datastore, userID, items, listName);
      saveAggregateData(datastore, userID, listName, items, true);
      return false;
    }
    saveAggregateData(datastore, userID, listName, items, false);
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
    log.info("ITEMS SHOULD BE EMPTY: " + items);
    Entity entity = new Entity("List");
    entity.setProperty("listName", listName);
    entity.setProperty("userID", userID);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("items", items);
    log.info("List entity: " + entity);
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

  /**
   * Asynchronously stores aggregated data in the background.
   *
   * @param userID The ID corresponding to user who made the comment
   * @param datastore Database instance.
   * @param listName The name of the list being created.
   * @param items List of strings containing items to add to list
   * @param timeString Timestamp to assign to the comment.
   */
  private static void saveAggregateData(
      DatastoreService datastore,
      String userID,
      String listName,
      List<String> items,
      boolean newList) {
    final DatastoreService datastoreService = datastore;
    final String userId = userID;
    final String name = listName;
    final List<String> listItems = items;
    final boolean isNewList = newList;
    // new Thread(
    //         () -> {
    saveAggregateListData(
        datastoreService, userId, StemUtils.stemmed(name).toLowerCase(), listItems, isNewList);
    //     })
    // .start();
  }

  /**
   * Stores the integer aggregate count of number of times user has placed a given item in a list.
   *
   * @param keyName The name of the entity key to store that data in.
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param items List of strings containing items to add to list
   */
  private static void saveAggregateListData(
      DatastoreService datastore,
      String userID,
      String listName,
      List<String> items,
      boolean newList) {
    Entity entity;
    try {
      entity = datastore.get(KeyFactory.createKey(listName, userID));
      for (String item : items) {
        String stemmedItem = StemUtils.stemmed(item);
        StemUtils.saveStemData(datastore, userID, item);
        long prevValue =
            entity.getProperty(stemmedItem) == null ? 0 : (long) entity.getProperty(stemmedItem);
        entity.setProperty(stemmedItem, prevValue + 1);
      }
    } catch (EntityNotFoundException e) {
      entity = new Entity(listName, userID);
      entity.setProperty("userID", userID);
      for (String item : items) {
        String stemmedItem = StemUtils.stemmed(item);
        StemUtils.saveStemData(datastore, userID, item);
        long value = 1;
        entity.setProperty(stemmedItem, value);
      }
    }
    entity.setProperty("timestamp", System.currentTimeMillis());
    if (newList) {
      Object countObject = entity.getProperty("count");
      long count = countObject == null ? 1 : ((long) countObject) + 1;
      entity.setProperty("count", count);
    }
    entity.setProperty("listName", listName);
    log.info("Aggregation List entity: " + entity);
    datastore.put(entity);
    updateFractionalAggregation(datastore, userID, entity);
  }

  /**
   * Stores the fractional integer aggregate count of number of times user has placed a given item
   * in a list.
   *
   * @param keyName The name of the entity key to store that data in.
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param entity Original entity reference.
   */
  private static void updateFractionalAggregation(
      DatastoreService datastore, String userID, Entity entity) {
    Entity fracEntity = new Entity("Frac-" + (String) entity.getProperty("listName"), userID);
    Long total = (Long) entity.getProperty("count");
    for (String item : entity.getProperties().keySet()) {
      if (AGG_ENTITY_ID_PROPERTIES.contains(item)) {
        continue;
      }
      fracEntity.setProperty(item, ((Long) entity.getProperty(item)) / total.doubleValue());
    }
    for (String name : AGG_ENTITY_ID_PROPERTIES) {
      fracEntity.setProperty(name, entity.getProperty(name));
    }
    log.info("FRACTIONAL LIST ENTITY: " + fracEntity);
    datastore.put(fracEntity);
  }

  public static String makePastRecommendations(
      String userID, DatastoreService datastore, String listName) throws EntityNotFoundException {
    String defaultResponse =
        "Created! What are some items to add to your new " + listName + " list?";
    Entity entity;
    try {
      entity = datastore.get(KeyFactory.createKey("Frac-" + StemUtils.stemmed(listName), userID));
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      return defaultResponse;
    }
    log.info("Past recommendation entity: " + entity);
    if ((long) entity.getProperty("count") < 3) {
      return defaultResponse;
    }
    PriorityQueue pq =
        new PriorityQueue(
            3,
            new Comparator<Pair<String, Double>>() {
              @Override
              public int compare(Pair<String, Double> p1, Pair<String, Double> p2) {
                return p2.getValue().compareTo(p1.getValue());
              }
            });
    for (String item : entity.getProperties().keySet()) {
      if (AGG_ENTITY_ID_PROPERTIES.contains(item)) {
        continue;
      }
      Double itemFreq = (Double) entity.getProperty(item);
      log.info("trying item: " + item + "; with priority: " + itemFreq);
      if (itemFreq > 0.501) {
        log.info("ADDED item: " + item + "; with priority: " + itemFreq);
        pq.add(new Pair<String, Double>(item, itemFreq));
        log.info("PQ SIZE: " + pq.size());
      }
    }
    if (pq.isEmpty()) {
      return defaultResponse;
    }
    String suggestedItems = getSuggestedItems(userID, datastore, pq);
    return "Created! Based on your previous lists, would you like to add " + suggestedItems + "?";
  }

  private static String getSuggestedItems(
      String userID, DatastoreService datastore, PriorityQueue<Pair<String, Double>> pq)
      throws EntityNotFoundException {
    if (pq.size() == 1) {
      Pair<String, Double> pair = (Pair<String, Double>) pq.poll();
      String item = StemUtils.unstem(userID, datastore, (String) pair.getKey());
      return item;
    }
    Object[] items = new Object[pq.size()];
    log.info("PQ FOR LOOP SIZE: " + pq.size());
    int size = pq.size();
    for (int i = 0; i < size; i++) {
      log.info("Above threshold items: " + pq.peek().getKey() + pq.peek().getValue());
      items[i] = StemUtils.unstem(userID, datastore, ((Pair<String, Double>) pq.poll()).getKey());
    }
    //   Object[] items = pq.stream().map(Errors.rethrow().wrap(e -> StemUtils.unstem(userID,
    // datastore, ((Pair<String, Double>) e).getKey()))).toArray();
    if (size == 2) {
      return String.format("%s and %s", items[0], items[1]);
    } else {
      return String.format("%s, %s, and %s", items[0], items[1], items[2]);
    }
  }
}
