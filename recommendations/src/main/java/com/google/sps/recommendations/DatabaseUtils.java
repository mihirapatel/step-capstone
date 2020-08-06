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

package com.google.sps.recommendations;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
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
   * @param userID String containing current user's unique ID
   * @param stemmedListName The name of the list to store aggregation information for.
   * @param items List of strings containing items to add to list
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
              : ((Number) aggregateEntity.getProperty(stemmedItem)).longValue();
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
    log.info("created aggregate entity: " + aggregateEntity);
    datastore.put(aggregateEntity);
    updateFractionalAggregation(datastore, userID, stemmedListName, items, count, count == 1, true);
  }

  /**
   * Stores the fractional integer aggregate count of number of times user has placed a given item
   * in a list.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID String containing current user's unique ID
   * @param stemmedListName Stemmed name of the list for which we are recording unique items
   * @param items List of strings containing items to add to list
   * @param aggregateEntity Aggregate entity for reference in creating fractional entity
   * @param firstList Boolean representing true if updating fractions for the first list of a name
   *     type
   * @param positiveFeedback Boolean indicating if items are being added to list or rejected
   *     recommendations.
   */
  public static void updateFractionalAggregation(
      DatastoreService datastore,
      String userID,
      String stemmedListName,
      List<String> items,
      long listCount,
      boolean firstList,
      boolean positiveFeedback) {
    if (items == null) {
      return;
    }
    List<String> stemmedItems = StemUtils.stemmedList(items);
    Entity fracEntity;
    try {
      fracEntity = datastore.get(KeyFactory.createKey("Frac-" + stemmedListName, userID));
      double incrementValue = positiveFeedback ? (firstList ? 1.0 : 0.4) : -1.0;
      for (String stemmedItem : stemmedItems) {
        Double existingRate = (Double) fracEntity.getProperty(stemmedItem);
        if (existingRate == null) {
          fracEntity.setProperty(stemmedItem, incrementValue);
        } else {
          fracEntity.setProperty(stemmedItem, existingRate + incrementValue);
        }
      }
    } catch (EntityNotFoundException e) {
      fracEntity = new Entity("Frac-" + stemmedListName, userID);
      for (String stemmedItem : stemmedItems) {
        fracEntity.setProperty(stemmedItem, 1.0);
      }
      fracEntity.setProperty("userID", userID);
      fracEntity.setProperty("listName", stemmedListName);
      fracEntity.setProperty("timestamp", System.currentTimeMillis());
    }
    fracEntity.setProperty("count", listCount);
    log.info("frac entity here" + fracEntity);
    datastore.put(fracEntity);
    try {
      RecommendationUtils.updateUserRecommendations(datastore, stemmedListName);

    } catch (EntityNotFoundException | IllegalStateException e) {
      log.error("Recommendation error: " + e);
    }
  }

  /**
   * Retrieves existing fractional entity from datastore and halves all weights to diminish effects
   * of earlier grocery lists. If no fractional entity of the given name exists, then does nothing.
   *
   * @param datastore Database entity to retrieve data from
   * @param userID String containing current user's unique ID
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
   * @param items List of strings containing items to add to list
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
   * Resets the database to the initial demo status. Only resets categories of: type, frac-type, and
   * uniqueItems
   *
   * @param datastore DatastoreService instance to be prepopulated with default values
   */
  public static void resetDatabase(DatastoreService datastore) throws IllegalStateException {
    URL url = DatabaseUtils.class.getResource("/dbEntities");
    String path = url.getPath();
    File[] allDBFiles = new File(path).listFiles();
    Arrays.sort(allDBFiles);
    Gson gson = new Gson();
    boolean checkExisting = true;
    for (File file : allDBFiles) {
      String fileName = file.getName();
      log.info("file: " + fileName);
      String categoryName = fileName.split("\\.")[0];
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
          Map<String, Value> keyMap = stringToMap(line).get("key").getStructValue().getFieldsMap();
          String kind = (String) keyMap.get("kind").getStringValue();
          boolean uniqueItems = kind.equals("UniqueItems");
          String id = uniqueItems ? categoryName : (String) e.getProperty("userID");
          Entity entity = new Entity(kind, id);
          entity.setPropertiesFrom(e);
          if (!uniqueItems) {
            entity.setProperty(
                "timestamp", Long.parseLong((String) entity.getProperty("timestamp")));
            entity.setProperty(
                "count", Double.valueOf((double) entity.getProperty("count")).longValue());
          }
          if (checkExisting) {
            checkExisting = false;
            try {
              Entity existing = datastore.get(entity.getKey());
              throw new IllegalStateException(
                  "Cannot reset database when there are existing entities that will be overridden.");
            } catch (EntityNotFoundException exception) {
              datastore.put(entity);
            }
          } else {
            datastore.put(entity);
          }
        }
      } catch (IOException e) {
        log.error("Error trying to read file: " + e);
        continue;
      }
      try {
        RecommendationUtils.updateUserRecommendations(datastore, categoryName);
      } catch (EntityNotFoundException | IllegalStateException e) {
        log.error("Recommendation error: " + e);
      }
    }
  }

  /**
   * Converts a json string into a Map object
   *
   * @param json json string
   * @return Map<String, Value>
   */
  public static Map<String, Value> stringToMap(String json) throws InvalidProtocolBufferException {
    JSONObject jsonObject = new JSONObject(json);
    Builder structBuilder = Struct.newBuilder();
    JsonFormat.parser().merge(jsonObject.toString(), structBuilder);
    Struct struct = structBuilder.build();
    return struct.getFieldsMap();
  }
}
