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
import com.google.appengine.api.log.InvalidRequestException;
import com.google.protobuf.Value;
import com.google.sps.data.Pair;
import com.google.sps.data.Recommender;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryUtils {

  private static Logger log = LoggerFactory.getLogger(MemoryUtils.class);
  public static final List<String> AGG_ENTITY_ID_PROPERTIES =
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

  public static List<Entity> getPastUserLists(DatastoreService datastore, String userID, Map<String, Value> parameters) {
      Filter filter = makeFilters(parameters, userID, true);
      List<Entity> listQuery = pastListHelper(datastore, filter);
      if (listQuery.size() == 0) {
          filter = makeFilters(parameters, userID, false);
          listQuery = pastListHelper(datastore, filter);
      }
      String maxValue = parameters.get("number").getStringValue();
      if (!maxValue.equals("-1")) {
          return listQuery.subList(0, Math.min(listQuery.size(), (int) parameters.get("number").getNumberValue()));
      }
      return listQuery;
  }

  private static Filter makeFilters(Map<String, Value> parameters, String userID, boolean tryName) throws InvalidRequestException {
      List<Filter> filters = new ArrayList<>();
      filters.add(new FilterPredicate("userID", FilterOperator.EQUAL, userID));
      String listNameValue = parameters.get("list-name").getStringValue();
      if (tryName && !listNameValue.isEmpty()) {
          filters.add(new FilterPredicate("stemmedListName", FilterOperator.EQUAL, StemUtils.stemmed(listNameValue)));
      }
      Value durationValue = parameters.get("date-time-enhanced");
      if (durationValue.hasStructValue()) {
          try {
              Pair<Long, Long> timeRange = TimeUtils.getTimeRange(durationValue);
              filters.add(new FilterPredicate("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, timeRange.getKey()));
              filters.add(new FilterPredicate("timestamp", FilterOperator.LESS_THAN_OR_EQUAL, timeRange.getValue()));
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

  private static List<Entity> pastListHelper(DatastoreService datastore, Filter filter) {
      Query query =
        new Query("List")
            .setFilter(filter)
            .addSort("timestamp", SortDirection.DESCENDING);
      return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
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
    String stemmedListName = StemUtils.stemmed(listName);
    List<Entity> existingList =
        fetchExistingListQuery(datastore, userID, stemmedListName);
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
      if (items.size() > 0) {
          decreaseFracEntityWeights(datastore, userID, stemmedListName);
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
    String stemmedListName = StemUtils.stemmed(listName);
    List<Entity> existingList =
        fetchExistingListQuery(datastore, userID, stemmedListName);
    if (existingList.isEmpty()) {
      addListItems(datastore, userID, items, listName);
      return false;
    }
    Entity existingEntity = existingList.get(0);
    ArrayList<String> existingItems = (ArrayList<String>) existingEntity.getProperty("items");
    if (existingItems == null) {
      existingItems = new ArrayList<>();
      decreaseFracEntityWeights(datastore, userID, stemmedListName);
    }
    existingItems.addAll(items);
    existingEntity.setProperty("items", existingItems);
    saveAggregateListData(datastore, userID, listName, items, false);
    datastore.put(existingEntity);
    return true;
  }

  /**
   * Creates a comment entity with a given timestamp and stores it in the given user's database.
   * Should only be called for testing purposes.
   *
   * @param datastore Database instance.
   * @param userID The ID corresponding to user who made the comment
   * @param items List of strings containing items to add to list
   * @param listName The name of the list being created.
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
    entity.setProperty("stemmedListName", StemUtils.stemmed(listName));
    entity.setProperty("userID", userID);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("items", items);
    datastore.put(entity);
  }

  private static void addListItems(
      DatastoreService datastore, String userID, ArrayList<String> items, String listName) {
    makeListEntity(datastore, userID, items, listName, System.currentTimeMillis());
    if (items != null && items.size() > 0) {
        saveAggregateListData(datastore, userID, listName, items, true);
    }
  }

  /**
   * Fetches all lists created by the current user with the given stemmed List name. List is
   * returned with the most recently created first.
   *
   * @param datastore Database instance
   * @param userID current user's ID
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
    ;
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Stores the integer aggregate count of number of times user has placed a given item in a list.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
   * @param listName The name of the list to store aggregation information for.
   * @param items List of strings containing items that were newly added.
   * @param newList Indicates whether the list is a new list (true) or updating existing (false)
   */
  public static void saveAggregateListData(
      DatastoreService datastore,
      String userID,
      String listName,
      List<String> items,
      boolean newList) {
    String stemmedListName = StemUtils.stemmed(listName);
    Entity aggregateEntity;
    try {
      aggregateEntity = datastore.get(KeyFactory.createKey(stemmedListName, userID));
    } catch (EntityNotFoundException e) {
      aggregateEntity = new Entity(stemmedListName, userID);
      aggregateEntity.setProperty("userID", userID);
    }
    if (items != null) {
      for (String item : items) {
        String stemmedItem = StemUtils.stemmed(item);
        StemUtils.saveStemData(datastore, userID, item);
        long prevValue =
            aggregateEntity.getProperty(stemmedItem) == null ? 0 : (long) aggregateEntity.getProperty(stemmedItem);
        aggregateEntity.setProperty(stemmedItem, prevValue + 1);
      }
      updateUniqueProperties(datastore, stemmedListName, StemUtils.stemmedList(items));
    }
    aggregateEntity.setProperty("timestamp", System.currentTimeMillis());
    long incrementCount = 0;
    if (newList) {
      incrementCount = 1;
    }
    Object countObject = aggregateEntity.getProperty("count");
    long count = countObject == null ? 0 + incrementCount : ((long) countObject) + incrementCount;
    aggregateEntity.setProperty("count", count);
    aggregateEntity.setProperty("listName", stemmedListName);
    datastore.put(aggregateEntity);
    updateFractionalAggregation(datastore, userID, stemmedListName, items, aggregateEntity, count == 1);
  }

  /**
   * Records all unique property items for a given list name across all users.
   *
   * @param datastore Database entity to retrieve data from
   * @param stemmedListName Stemmed name of the list for which we are recording unique items
   * @param items Newly added items being determined for uniqueness
   */
  private static void updateUniqueProperties(
      DatastoreService datastore, String stemmedListName, List<String> items) {
    Entity entity;
    Set<String> updatedUniqueItems;
    try {
      entity = datastore.get(KeyFactory.createKey("UniqueItems", stemmedListName));
      updatedUniqueItems = new HashSet<String>((List<String>) entity.getProperty("items"));
      updatedUniqueItems.addAll(items);
    } catch (EntityNotFoundException | NullPointerException e) {
      entity = new Entity("UniqueItems", stemmedListName);
      updatedUniqueItems = new HashSet<String>(items);
    }
    entity.setProperty("items", updatedUniqueItems);
    datastore.put(entity);
  }

  /**
   * Stores the fractional integer aggregate count of number of times user has placed a given item
   * in a list.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
   * @param stemmedListName Stemmed name of the list for which we are recording unique items
   * @param items New items that were added
   * @param aggregateEntity Aggregate entity for reference in creating fractional entity
   * @param firstList Boolean representing true if updating fractions for the first list of a name type
   */
  private static void updateFractionalAggregation(
      DatastoreService datastore, String userID, String stemmedListName, List<String> items, Entity entity, boolean firstList) {
    if (items == null) {
        return;
    }
    List<String> stemmedItems = StemUtils.stemmedList(items);
    Entity fracEntity;
    try {
      fracEntity = datastore.get(KeyFactory.createKey("Frac-" + stemmedListName, userID));
      double incrementValue = firstList ? 1.0 : 0.4;
      for (String stemmedItem : stemmedItems) {
        Double existingRate = (Double)fracEntity.getProperty(stemmedItem);
        if (existingRate == null) {
            fracEntity.setProperty(stemmedItem, incrementValue);
        } else {
            fracEntity.setProperty(stemmedItem, existingRate + incrementValue);
        }
      }
      fracEntity.setProperty("count", entity.getProperty("count"));
    } catch (EntityNotFoundException e) {
      fracEntity = new Entity("Frac-" + stemmedListName, userID);
      for (String name : AGG_ENTITY_ID_PROPERTIES) {
        fracEntity.setProperty(name, entity.getProperty(name));
      }
      for (String stemmedItem : stemmedItems) {
          fracEntity.setProperty(stemmedItem, 1.0);
      }
    }
    log.info("frac entity here" + fracEntity);
    datastore.put(fracEntity);
  }

   /**
   * Retrieves existing fractional entity from datastore and halves all weights to diminish 
   * effects of earlier grocery lists. If no fractional entity of the given name exists,
   * then does nothing.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
   * @param stemmedListName Stemmed name of the list for the fractional entity to be retrieved.
   */
  private static void decreaseFracEntityWeights(DatastoreService datastore, String userID, String stemmedListName) {
      try {
          Entity fracEntity = datastore.get(KeyFactory.createKey("Frac-" + stemmedListName, userID));
      for (String item : fracEntity.getProperties().keySet()) {
        if (AGG_ENTITY_ID_PROPERTIES.contains(item)) {
            continue;
        }
        fracEntity.setProperty(item, ((Double) fracEntity.getProperty(item)) * 0.6);
      }
      log.info("decrease frac entity: " + fracEntity);
      datastore.put(fracEntity);
      } catch (EntityNotFoundException e) {
          return;
      }
  }

  /**
   * Makes recommendations based on the user's past history of list items. Will only make
   * recommendations if the user has at least 3 lists of the same name. Recommendations are made
   * based on the top 3 items that the user has placed on at least 50% of previous lists.
   *
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param listName Name of the list we are providing item recommendations for.
   * @return String containing the fulfillment response to the user
   */
  public static String makePastRecommendations(
      String userID, DatastoreService datastore, String listName) throws EntityNotFoundException, IllegalStateException {
    Entity entity = datastore.get(KeyFactory.createKey("Frac-" + StemUtils.stemmed(listName), userID));
    if ((long) entity.getProperty("count") < 3) {
      throw new IllegalStateException("Not enough past lists to make recommendations.");
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
      if (itemFreq > 0.49) {
        pq.add(new Pair<String, Double>(item, itemFreq));
      }
    }
    if (pq.isEmpty()) {
      throw new IllegalStateException("No items in PQ");
    }
      String suggestedItems = getSuggestedItems(userID, datastore, pq);
      return " Based on your previous lists, would you like to add " + suggestedItems + "?";
  }

  /**
   * Creates a formatted string of suggested items based on the elements in the PQ.
   *
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param pq Priority queue of top elements to recommend to the user
   * @return String containing a formatted list of items to recommend to the user
   */
  private static String getSuggestedItems(
      String userID, DatastoreService datastore, PriorityQueue<Pair<String, Double>> pq)
      throws EntityNotFoundException, IllegalStateException {
    if (pq.isEmpty()) {
      throw new IllegalStateException("No items in PQ to suggest");
    }
    int pqSize = pq.size();
    if (pqSize == 1) {
      Pair<String, Double> pair = (Pair<String, Double>) pq.poll();
      return StemUtils.unstem(userID, datastore, (String) pair.getKey());
    }
    Object[] items = new Object[pqSize];
    for (int i = 0; i < pqSize; i++) {
      items[i] = StemUtils.unstem(userID, datastore, ((Pair<String, Double>) pq.poll()).getKey());
    }
    if (pqSize == 2) {
      return String.format("%s and %s", items[0], items[1]);
    } else {
      return String.format("%s, %s, and %s", items[0], items[1], items[2]);
    }
  }

  /**
   * Finds items to recommend to the current user based on interests of the current user in relation
   * to other users. If the current user does not have any history of interests in the database,
   * method will throw EntityNotFoundException.
   *
   * @param userID String representing the ID of the user giving recommendations for
   * @param datastore Database service instance
   * @param listName Name of the list we are providing recommendations for.
   * @return String containing up to 3 items to recommend to the user.
   */
  public static String makeUserRecommendations(
      String userID, DatastoreService datastore, String listName)
      throws IllegalStateException, EntityNotFoundException {
    String stemmedListName = StemUtils.stemmed(listName);
    Set<String> uniqueItems = getUniqueItems(datastore, stemmedListName);
    List<String> existingItemsInList = getCurrentItems(userID, datastore, stemmedListName);
    Entity userEntity = datastore.get(KeyFactory.createKey("Frac-" + stemmedListName, userID));
    List<Entity> otherUserEntities =
        getAllEntitiesExceptUser(datastore, "Frac-" + stemmedListName, userID);
    if (otherUserEntities.size() < 3) {
      throw new IllegalStateException(
          "Cannot make recommendations when there are less than 3 other users.");
    }
    Recommender rec = new Recommender((int) Math.ceil(Math.sqrt(uniqueItems.size())));
    List<Pair<String, Double>> expectedUserInterest =
        rec.makeRecommendations(userEntity, otherUserEntities, uniqueItems);
    List<Pair<String, Double>> filteredUserInterest =
        expectedUserInterest.stream()
            .filter(e -> (!existingItemsInList.contains(e.getKey())) && (e.getValue() > 0.4))
            .collect(Collectors.toList());
    PriorityQueue pq =
        new PriorityQueue(
            3,
            new Comparator<Pair<String, Double>>() {
              @Override
              public int compare(Pair<String, Double> p1, Pair<String, Double> p2) {
                return p2.getValue().compareTo(p1.getValue());
              }
            });
    for (Pair<String, Double> itemPair : filteredUserInterest) {
      pq.add(itemPair);
    }
    return getSuggestedItems(userID, datastore, pq);
  }

  /**
   * Throws an error if there is no record of unique items in the database.
   *
   * @param datastore Database instance
   * @param stemmedListName Stemmed name of the user's current list
   * @return set of unique items in the given aggregate list database
   */
  private static Set<String> getUniqueItems(DatastoreService datastore, String stemmedListName)
      throws EntityNotFoundException, IllegalStateException {
    Entity entity = datastore.get(KeyFactory.createKey("UniqueItems", stemmedListName));
    Object setItems = entity.getProperty("items");
    if (setItems == null) {
      throw new IllegalStateException("Unique Items database has not been initialized with items.");
    }
    return new HashSet<String>((List<String>) entity.getProperty("items"));
  }

  /**
   * Returns a list of all items currently in the user's list.
   *
   * @param userID String to identify the current user.
   * @param datastore DatastoreService instance
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
   * Returns a list of all entities in the fractional aggregatino database for a given listName.
   *
   * @param datastore DatastoreService instance
   * @param category String representing category to fetch from datastore
   * @param userID String representing the ID of the user we want to filter out of query.
   * @return List of entities containing the fractional aggregate properties for each user
   */
  private static List<Entity> getAllEntitiesExceptUser(
      DatastoreService datastore, String category, String userID) {
    Filter notCurrentUserFilter = new FilterPredicate("userID", FilterOperator.NOT_EQUAL, userID);
    Query query =
        new Query(category)
            .setFilter(notCurrentUserFilter)
            .addSort("userID", SortDirection.ASCENDING)
            .addSort("timestamp", SortDirection.ASCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }
}
