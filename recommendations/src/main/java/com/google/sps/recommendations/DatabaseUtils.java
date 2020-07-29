package com.google.sps.recommendations;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUtils {

  private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
  public static final List<String> AGG_ENTITY_ID_PROPERTIES =
      Arrays.asList("userID", "timestamp", "count", "listName");

  /**
   * Stores the integer aggregate count of number of times user has placed a given item in a list.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
   * @param stemmedListName The name of the list to store aggregation information for.
   * @param items List of strings containing items that were newly added.
   * @param newList Indicates whether the list is a new list (true) or updating existing (false)
   */
  public static void storeUserListInformation(
      DatastoreService datastore,
      String userID,
      String stemmedListName,
      List<String> items,
      boolean newList) {
    if (items == null || items.isEmpty()) {
      return; // Do not store aggregate info for lists with no items.
    }
    Entity aggregateEntity;
    try {
      aggregateEntity = datastore.get(KeyFactory.createKey(stemmedListName, userID));
    } catch (EntityNotFoundException e) {
      aggregateEntity = new Entity(stemmedListName, userID);
      aggregateEntity.setProperty("userID", userID);
    }
    for (String item : items) {
      String stemmedItem = StemUtils.stemmed(item);
      StemUtils.saveStemData(datastore, userID, item);
      long prevValue =
          aggregateEntity.getProperty(stemmedItem) == null
              ? 0
              : (long) aggregateEntity.getProperty(stemmedItem);
      aggregateEntity.setProperty(stemmedItem, prevValue + 1);
    }
    updateUniqueProperties(datastore, stemmedListName, StemUtils.stemmedList(items));
    aggregateEntity.setProperty("timestamp", System.currentTimeMillis());
    long incrementCount = 0;
    if (newList) {
      incrementCount = 1;
      decreaseFracEntityWeights(datastore, userID, stemmedListName);
    }
    Object countObject = aggregateEntity.getProperty("count");
    long count = countObject == null ? 0 + incrementCount : ((long) countObject) + incrementCount;
    aggregateEntity.setProperty("count", count);
    aggregateEntity.setProperty("listName", stemmedListName);
    datastore.put(aggregateEntity);
    updateFractionalAggregation(
        datastore, userID, stemmedListName, items, aggregateEntity, count == 1);
    try {
      RecommendationUtils.updateUserRecommendations(datastore, userID, stemmedListName, items);

    } catch (EntityNotFoundException | IllegalStateException e) {
      log.error("Recommendation error: " + e);
    }
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
   * @param firstList Boolean representing true if updating fractions for the first list of a name
   *     type
   */
  private static void updateFractionalAggregation(
      DatastoreService datastore,
      String userID,
      String stemmedListName,
      List<String> items,
      Entity entity,
      boolean firstList) {
    if (items == null) {
      return;
    }
    List<String> stemmedItems = StemUtils.stemmedList(items);
    Entity fracEntity;
    try {
      fracEntity = datastore.get(KeyFactory.createKey("Frac-" + stemmedListName, userID));
      double incrementValue = firstList ? 1.0 : 0.4;
      for (String stemmedItem : stemmedItems) {
        Double existingRate = (Double) fracEntity.getProperty(stemmedItem);
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
   * Retrieves existing fractional entity from datastore and halves all weights to diminish effects
   * of earlier grocery lists. If no fractional entity of the given name exists, then does nothing.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID The logged-in user's ID
   * @param stemmedListName Stemmed name of the list for the fractional entity to be retrieved.
   */
  private static void decreaseFracEntityWeights(
      DatastoreService datastore, String userID, String stemmedListName) {
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
}
