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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecommendationUtils {

  private static Logger log = LoggerFactory.getLogger(RecommendationUtils.class);

  /**
   * Makes recommendations based on the user's past history of list items. Will only make
   * recommendations if the user has at least 3 lists of the same name. Recommendations are made
   * based on the top 3 items that the user has placed on at least 50% of previous lists.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Database entity to retrieve data from
   * @param stemmedListName Name of the list we are providing item recommendations for.
   * @return String containing the fulfillment response to the user
   */
  public static List<Pair<String, Double>> makePastRecommendations(
      String userID, DatastoreService datastore, String stemmedListName)
      throws EntityNotFoundException, IllegalStateException {
    Entity entity = datastore.get(KeyFactory.createKey("Frac-" + stemmedListName, userID));
    if ((long) entity.getProperty("count") < 3) {
      throw new IllegalStateException("Not enough past lists to make recommendations.");
    }
    return getSortedListItems(userID, datastore, entity);
  }

  /**
   * Finds items to recommend to the current user based on interests of the current user in relation
   * to other users. If the current user does not have any history of interests in the database,
   * method will throw EntityNotFoundException.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Database service instance
   * @param stemmedListName Name of the list we are providing recommendations for.
   * @param stemmedExistingItems List of stemmed strings containing all items already in the user's
   *     list (so should not be recommended)
   * @return String containing up to 3 items to recommend to the user.
   */
  public static List<Pair<String, Double>> makeUserRecommendations(
      String userID, DatastoreService datastore, String stemmedListName)
      throws IllegalStateException, EntityNotFoundException {
    log.info("entered method for making user recommendations");
    Entity entity =
        datastore.get(KeyFactory.createKey("UserPredictions-" + stemmedListName, userID));
    log.info("found entity: " + entity);
    return getSortedListItems(userID, datastore, entity);
  }

  /**
   * Retrieves sorted prediction items and values values from datastore for the given user.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Datastore instance to used to retrieve user's stem conversions.
   * @param entity Item prediction entity corresponding to the current user
   */
  private static List<Pair<String, Double>> getSortedListItems(
      String userID, DatastoreService datastore, Entity entity)
      throws IllegalStateException, EntityNotFoundException {
    PriorityQueue<Pair<String, Double>> pq =
        new PriorityQueue<>(
            new Comparator<Pair<String, Double>>() {
              @Override
              public int compare(Pair<String, Double> p1, Pair<String, Double> p2) {
                return p2.getValue().compareTo(p1.getValue());
              }
            });
    for (String item : entity.getProperties().keySet()) {
      if (DatabaseUtils.AGG_ENTITY_ID_PROPERTIES.contains(item)) {
        continue;
      }
      pq.add(
          new Pair<String, Double>(
              StemUtils.unstem(userID, datastore, item), (Double) entity.getProperty(item)));
    }
    if (pq.isEmpty()) {
      throw new IllegalStateException("No items in PQ");
    }
    List<Pair<String, Double>> sortedItems = new ArrayList<>();
    while (!pq.isEmpty()) {
      sortedItems.add(pq.poll());
    }
    return sortedItems;
  }

  /**
   * Calculates updated past recommendation rates.
   *
   * @param datastore Database entity to retrieve data from
   * @param stemmedListName The name of the list to store aggregation information for.
   */
  public static void updateUserRecommendations(DatastoreService datastore, String stemmedListName)
      throws EntityNotFoundException, IllegalStateException {
    Set<String> uniqueItems = getUniqueItems(datastore, stemmedListName);
    List<Entity> allUserEntities = getAllEntities(datastore, "Frac-" + stemmedListName);
    if (allUserEntities.size() < 4) {
      throw new IllegalStateException(
          "Cannot make recommendations when there are less than 3 other users.");
    }
    Recommender rec =
        new Recommender(
            (int) Math.ceil(Math.sqrt(Math.min(allUserEntities.size(), uniqueItems.size()))));
    rec.makeRecommendations(datastore, stemmedListName, allUserEntities, uniqueItems);
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
    return new HashSet<String>((List<String>) setItems);
  }

  /**
   * Returns a list of all entities in the fractional aggregation database for a given list.
   *
   * @param datastore DatastoreService instance
   * @param category String representing category to fetch from datastore
   * @return List of entities containing the fractional aggregate properties for each user
   */
  private static List<Entity> getAllEntities(DatastoreService datastore, String category) {
    Query query = new Query(category).addSort("userID", SortDirection.ASCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Fetches all lists created by the current user with the given stemmed List name. List is
   * returned with the most recently created first.
   *
   * @param datastore Database instance
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
}
