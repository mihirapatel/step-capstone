package com.google.sps.recommendations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelper {

  private static Logger log = LoggerFactory.getLogger(TestHelper.class);

  /**
   * Retrieves a list of entity objects for the default user from the given query for testing
   * purposes to ensure that result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @return A list of entity objects returned by datastore.
   */
  public static List<Entity> fetchDatastoreEntities(DatastoreService datastore, String category) {
    return fetchDatastoreEntities(datastore, category, "1");
  }

  /**
   * Retrieves a list of entity objects for all users from the given query for testing purposes to
   * ensure that result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @return A list of entity objects returned by datastore.
   */
  public static List<Entity> fetchDatastoreAllUsers(DatastoreService datastore, String category) {
    Query query = new Query(category);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Retrieves a list of entity objects from the given query for testing purposes to ensure that
   * result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @param userID String representing the user ID number for getting specific info about other
   *     users
   * @return A list of entity objects returned by datastore.
   */
  public static List<Entity> fetchDatastoreEntities(
      DatastoreService datastore, String category, String userID) {
    Filter filter = new FilterPredicate("userID", FilterOperator.EQUAL, userID);
    return fetchDatastoreEntities(datastore, category, filter);
  }

  /**
   * Retrieves a list of entity objects from the given query for testing purposes to ensure that
   * result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @param filter Filter for query results.
   * @return A list of entity objects returned by datastore.
   */
  public static List<Entity> fetchDatastoreEntities(
      DatastoreService datastore, String category, Filter filter) {
    Query query =
        new Query(category).setFilter(filter).addSort("timestamp", SortDirection.DESCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /** Helper method for list database verification. */
  public static void checkDatabaseItems(
      DatastoreService datastore, int size, String listName, List<String> expectedItems) {
    checkDatabaseItems(datastore, size, "1", listName, expectedItems);
  }

  /** Helper method for list database verification. */
  public static void checkDatabaseItems(
      DatastoreService datastore,
      int size,
      String listName,
      List<String> expectedItems,
      boolean isEmpty) {
    checkDatabaseItems(datastore, size, "1", listName, expectedItems);
  }

  /**
   * Methods for database verification. Checks that database entries are equal to expected.
   *
   * @param size Number of items expected in user's list
   * @param userID Current user's ID
   * @param listName Name of the list being checked
   * @param expectedItems List of strings containing the name of all items expected to be in the
   *     user's list database
   */
  public static void checkDatabaseItems(
      DatastoreService datastore,
      int size,
      String userID,
      String listName,
      List<String> expectedItems) {
    List<Entity> databaseQuery = fetchDatastoreEntities(datastore, "List", userID);
    assertEquals(size, databaseQuery.size());
    Entity entity = databaseQuery.get(0);
    assertEquals(listName, (String) entity.getProperty("listName"));
    ArrayList<String> items = (ArrayList<String>) entity.getProperty("items");
    if (expectedItems.isEmpty()) {
      assertNull(items);
      return;
    }
    for (int i = 0; i < items.size(); i++) {
      assertEquals(expectedItems.get(i), items.get(i));
    }
  }

  /** Helper method for aggregate list database verification. */
  public static void checkAggregate(
      DatastoreService datastore,
      String fetchName,
      List<String> expectedItems,
      List<Integer> expectedCounts) {
    checkAggregate(datastore, fetchName, "1", expectedItems, expectedCounts);
  }

  /**
   * Methods for aggregate database verification. Checks that database entries are equal to
   * expected.
   *
   * @param fetchName Category name used to fetch subcategory from datastore
   * @param userID Current user's ID
   * @param expectedItems List of strings containing the name of all items expected to be in the
   *     user's list database
   * @param expectedCount List of integers containing expected counts for each expected item of the
   *     corresponding index.
   */
  public static void checkAggregate(
      DatastoreService datastore,
      String fetchName,
      String userID,
      List<String> expectedItems,
      List<Integer> expectedCounts) {
    Filter filter =
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate("userID", FilterOperator.EQUAL, userID),
                new FilterPredicate("listName", FilterOperator.EQUAL, fetchName)));
    List<Entity> databaseQuery = fetchDatastoreEntities(datastore, fetchName, filter);
    assertEquals(1, databaseQuery.size());
    Entity entity = databaseQuery.get(0);
    for (int i = 0; i < expectedItems.size(); i++) {
      if (expectedCounts.get(i) == 0) {
        assertNull(entity.getProperty(StemUtils.stemmed(expectedItems.get(i))));
        continue;
      }
      assertEquals(
          (long) expectedCounts.get(i),
          (long) entity.getProperty(StemUtils.stemmed(expectedItems.get(i))));
    }
  }

  /** Helper method for fractional aggregate list database verification. */
  public static void checkFracAggregate(
      DatastoreService datastore,
      String fetchName,
      List<String> expectedItems,
      List<Double> expectedCounts) {
    checkFracAggregate(datastore, fetchName, "1", expectedItems, expectedCounts);
  }

  /**
   * Methods for aggregate database verification. Checks that database entries are equal to
   * expected.
   *
   * @param fetchName Category name used to fetch subcategory from datastore
   * @param userID Current user's ID
   * @param expectedItems List of strings containing the name of all items expected to be in the
   *     user's list database
   * @param expectedCount List of doubles containing expected fractional values for each expected
   *     item of the corresponding index.
   */
  public static void checkFracAggregate(
      DatastoreService datastore,
      String fetchName,
      String userID,
      List<String> expectedItems,
      List<Double> expectedCounts) {
    Filter filter =
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate("userID", FilterOperator.EQUAL, userID),
                new FilterPredicate("listName", FilterOperator.EQUAL, fetchName)));
    List<Entity> databaseQuery = fetchDatastoreEntities(datastore, "Frac-" + fetchName, filter);
    assertEquals(1, databaseQuery.size());
    Entity entity = databaseQuery.get(0);
    for (int i = 0; i < expectedItems.size(); i++) {
      double itemFreq =
          entity.getProperty(StemUtils.stemmed(expectedItems.get(i))) == null
              ? 0.0
              : (double) entity.getProperty(StemUtils.stemmed(expectedItems.get(i)));
      assertEquals(expectedCounts.get(i), itemFreq, 0.001);
    }
  }

  /**
   * Populates a user list database with the given items and frequencies out of the total number of
   * lists created.
   *
   * @param datastore Datastore instance
   * @param userID Current user's ID
   * @param size Number of lists of the same name created by the user
   * @param items List of pairs of strings containing the name of all items expected and integer
   *     containing the number of times added to past grocery lists.
   */
  public static void makeUserList(
      DatastoreService datastore, String userID, int size, List<Pair<String, Integer>> items) {
    for (int i = 0; i < size; i++) {
      List<Pair<String, Integer>> itemsList = new ArrayList<>((List<Pair<String, Integer>>) items);
      final int temp = i;
      List<Pair<String, Integer>> filteredPairs =
          itemsList.stream().filter(e -> e.getValue() > temp).collect(Collectors.toList());
      List<String> filteredStrings =
          filteredPairs.stream().map(e -> e.getKey()).collect(Collectors.toList());
      DatabaseUtils.storeUserListInformation(
          datastore, userID, StemUtils.stemmed("grocery"), filteredStrings, true);
    }
  }
}
