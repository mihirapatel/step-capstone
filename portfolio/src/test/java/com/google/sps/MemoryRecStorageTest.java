package com.google.sps.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.protobuf.*;
import com.google.sps.data.Output;
import com.google.sps.servlets.TestHelper;
import com.google.sps.utils.*;
import com.google.sps.utils.MemoryUtils;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoryRecStorageTest {

  private static Logger log = LoggerFactory.getLogger(MemoryRecStorageTest.class);
  private TestHelper tester;

  /**
   * Tests list aggregation: 1) creates a new list for user 1, 2) updates the list for user 1, 3)
   * creates a new list for user 1 of the same name, 4) updates that list, 5) creates a list for
   * user 2, 6) creates a differently named list for user 2, 7) check for correct aggregate numbers
   */
  @Test
  public void testListAggregation() throws Exception {

    // 1) creates a new list for user 1

    tester =
        new TestHelper(
            "Start a grocery list with apples, bananas, and ice cream.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"apples, bananas, and ice cream\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");

    Output output = tester.getOutput();
    assertEquals("Created! Anything else you would like to add?", output.getFulfillmentText());

    List<String> expectedItems = Arrays.asList("apples", "bananas", "ice cream");
    checkDatabaseItems(1, "grocery", expectedItems);
    checkAggregate("groceri", 1, expectedItems, Arrays.asList(1, 1, 1));
    checkFracAggregate("groceri", 1, expectedItems, Arrays.asList(1.0, 1.0, 1.0));

    // 2) updates list for user 1

    tester.setParameters(
        "Start a grocery list with pineapple and apple.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"pineapple and apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - add");

    output = tester.getOutput();
    assertEquals("Updated!", output.getFulfillmentText());

    checkDatabaseItems(
        1, "grocery", Arrays.asList("apples", "bananas", "ice cream", "pineapple", "apple"));
    checkAggregate(
        "groceri",
        1,
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(2, 1, 1, 1));
    checkFracAggregate(
        "groceri",
        1,
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(2.0, 1.0, 1.0, 1.0));

    // 3) creates a new list for user 1 of the same name

    tester.setParameters(
        "Start a grocery list with pineapple and apple.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"pineapple and apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");

    output = tester.getOutput();
    assertEquals("Created! Anything else you would like to add?", output.getFulfillmentText());

    checkDatabaseItems(2, "grocery", Arrays.asList("pineapple", "apple"));
    checkAggregate(
        "groceri",
        1,
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(3, 1, 1, 2));
    checkFracAggregate(
        "groceri",
        1,
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(1.5, 0.5, 0.5, 1.0));

    // 4) updates that list

    tester.setParameters(
        "Start a grocery list with chocolate and ice cream.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"chocolate and ice cream\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - add");

    output = tester.getOutput();
    assertEquals("Updated!", output.getFulfillmentText());

    checkDatabaseItems(2, "grocery", Arrays.asList("pineapple", "apple", "chocolate", "ice cream"));
    checkAggregate(
        "groceri",
        1,
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(3, 1, 2, 2, 1));
    checkFracAggregate(
        "groceri",
        1,
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(1.5, 0.5, 1.0, 1.0, 0.5));

    // 5) creates a list for user 2

    tester.setParameters(
        "Start a Groceries list with chocolate and ice cream.",
        "{\"list-name\":\"Groceries\", "
            + "\"list-objects\":\"chocolate and ice cream\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    tester.setUser("test@example.com", "2");

    output = tester.getOutput();
    assertEquals("Created! Anything else you would like to add?", output.getFulfillmentText());

    checkDatabaseItems(1, "2", "Groceries", Arrays.asList("chocolate", "ice cream"));
    checkAggregate("groceri", "2", 1, Arrays.asList("ice cream", "chocolate"), Arrays.asList(1, 1));
    checkFracAggregate(
        "groceri", "2", 1, Arrays.asList("ice cream", "chocolate"), Arrays.asList(1.0, 1.0));

    // 5) creates a different list for user 2

    tester.setParameters(
        "Start a shopping list with books and chocolate.",
        "{\"list-name\":\"shopping\", "
            + "\"list-objects\":\"books and chocolate\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    tester.setUser("test@example.com", "2");

    output = tester.getOutput();
    assertEquals("Created! Anything else you would like to add?", output.getFulfillmentText());

    checkDatabaseItems(2, "2", "shopping", Arrays.asList("books", "chocolate"));
    checkAggregate("groceri", "2", 1, Arrays.asList("ice cream", "chocolate"), Arrays.asList(1, 1));
    checkFracAggregate(
        "groceri", "2", 1, Arrays.asList("ice cream", "chocolate"), Arrays.asList(1.0, 1.0));
    checkAggregate("shop", "2", 1, Arrays.asList("books", "chocolate"), Arrays.asList(1, 1));
    checkFracAggregate(
        "shop", "2", 1, Arrays.asList("books", "chocolate"), Arrays.asList(1.0, 1.0));
  }

  private void checkDatabaseItems(int size, String listName, List<String> expectedItems) {
    checkDatabaseItems(size, "1", listName, expectedItems);
  }

  private void checkDatabaseItems(
      int size, String userID, String listName, List<String> expectedItems) {
    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List", userID);
    assertEquals(size, databaseQuery.size());
    Entity entity = databaseQuery.get(0);
    assertEquals(listName, (String) entity.getProperty("listName"));
    ArrayList<String> items = (ArrayList<String>) entity.getProperty("items");
    for (int i = 0; i < items.size(); i++) {
      assertEquals(expectedItems.get(i), items.get(i));
    }
  }

  private void checkAggregate(
      String fetchName, int size, List<String> expectedItems, List<Integer> expectedCounts) {
    checkAggregate(fetchName, "1", size, expectedItems, expectedCounts);
  }

  private void checkAggregate(
      String fetchName,
      String userID,
      int size,
      List<String> expectedItems,
      List<Integer> expectedCounts) {
    Filter filter =
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate("userID", FilterOperator.EQUAL, userID),
                new FilterPredicate("listName", FilterOperator.EQUAL, fetchName)));
    List<Entity> databaseQuery = tester.fetchDatastoreEntities(fetchName, filter);
    assertEquals(size, databaseQuery.size());
    Entity entity = databaseQuery.get(0);
    for (int i = 0; i < expectedItems.size(); i++) {
      assertEquals(
          (long) expectedCounts.get(i),
          (long) entity.getProperty(MemoryUtils.stemmed(expectedItems.get(i))));
    }
  }

  private void checkFracAggregate(
      String fetchName, int size, List<String> expectedItems, List<Double> expectedCounts) {
    checkFracAggregate(fetchName, "1", size, expectedItems, expectedCounts);
  }

  private void checkFracAggregate(
      String fetchName,
      String userID,
      int size,
      List<String> expectedItems,
      List<Double> expectedCounts) {
    Filter filter =
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate("userID", FilterOperator.EQUAL, userID),
                new FilterPredicate("listName", FilterOperator.EQUAL, fetchName)));
    List<Entity> databaseQuery = tester.fetchDatastoreEntities("Frac-" + fetchName, filter);
    assertEquals(size, databaseQuery.size());
    Entity entity = databaseQuery.get(0);
    for (int i = 0; i < expectedItems.size(); i++) {
      assertEquals(
          expectedCounts.get(i),
          (double) entity.getProperty(MemoryUtils.stemmed(expectedItems.get(i))),
          0.001);
    }
  }
}
