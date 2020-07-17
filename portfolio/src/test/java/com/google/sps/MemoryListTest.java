package com.google.sps.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.Entity;
import com.google.protobuf.*;
import com.google.sps.data.Output;
import com.google.sps.servlets.TestHelper;
import com.google.sps.utils.MemoryUtils;
import com.google.sps.utils.TimeUtils;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoryListTest {

  private static Logger log = LoggerFactory.getLogger(MemoryListTest.class);

  /**
   * Tests the scenario where user makes a new list that is not currently in the datastore and does
   * not specify objects to add.
   */
  @Test
  public void testMakeListEmpty() throws Exception {
    TestHelper tester =
        new TestHelper(
            "Start a grocery list.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");

    Output output = tester.getOutput();
    assertEquals(
        "Created! What are some items to add to your new grocery list?",
        output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(1, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    assertNull(entity.getProperty("items"));
  }

  /**
   * Tests the scenario where user makes a new list that is not currently in the datastore and DOES
   * specify objects to add.
   */
  @Test
  public void testMakeListWithObjectsEmpty() throws Exception {
    TestHelper tester =
        new TestHelper(
            "Start a grocery list with apples, bananas, and ice cream.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"apples, bananas, and ice cream\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");

    Output output = tester.getOutput();
    assertEquals("Created! Anything else you would like to add?", output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(1, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    ArrayList<String> items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("apples", items.get(0));
    assertEquals("bananas", items.get(1));
    assertEquals("ice cream", items.get(2));
  }

  /**
   * Tests the scenario where user makes a new list that IS currently in the datastore and does not
   * specify objects to add.
   */
  @Test
  public void testMakeListExisting() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Start a grocery list.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");

    ArrayList<String> items = new ArrayList<>(Arrays.asList("apples", "bananas", "ice cream"));
    tester.setCustomDatabase(
        "grocery", items, TimeUtils.stringToDate("2020-02-11T09:30:00-00:00").getTime());

    Output output = tester.getOutput();
    assertEquals(
        "Created! What are some items to add to your new grocery list?",
        output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(2, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    assertNull(entity.getProperty("items"));

    entity = databaseQuery.get(1);
    assertEquals("grocery (02/11/2020 09:30:00)", (String) entity.getProperty("listName"));
    List<String> queryItems = (List<String>) entity.getProperty("items");
    for (int i = 0; i < queryItems.size(); i++) {
      assertEquals(items.get(i), queryItems.get(i));
    }
  }

  /**
   * Tests the scenario where user makes a new list that IS currently in the datastore and DOES
   * specify objects to add. Also tests that an existing list that is not overwritten is not
   * changed.
   */
  @Test
  public void testMakeListObjectExisting() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Start a grocery list with potatoes and carrots.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"potatoes and carrots\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");

    ArrayList<String> items = new ArrayList<>(Arrays.asList("apples", "bananas", "ice cream"));
    tester.setCustomDatabase(
        "grocery", items, TimeUtils.stringToDate("2020-02-11T09:30:00-00:00").getTime());

    ArrayList<String> otherListItems = new ArrayList<>(Arrays.asList("hats", "shoes", "jackets"));
    tester.setCustomDatabase(
        "shopping", otherListItems, TimeUtils.stringToDate("2020-02-11T09:00:00-00:00").getTime());

    Output output = tester.getOutput();
    assertEquals("Created! Anything else you would like to add?", output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(3, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    ArrayList<String> queryItems = (ArrayList<String>) entity.getProperty("items");
    assertEquals("potatoes", queryItems.get(0));
    assertEquals("carrots", queryItems.get(1));

    entity = databaseQuery.get(1);
    assertEquals("grocery (02/11/2020 09:30:00)", (String) entity.getProperty("listName"));
    queryItems = (ArrayList<String>) entity.getProperty("items");
    for (int i = 0; i < queryItems.size(); i++) {
      assertEquals(items.get(i), queryItems.get(i));
    }

    entity = databaseQuery.get(2);
    assertEquals("shopping", (String) entity.getProperty("listName"));
    queryItems = (ArrayList<String>) entity.getProperty("items");
    assertEquals(items.size(), queryItems.size());
    for (int i = 0; i < queryItems.size(); i++) {
      assertEquals(otherListItems.get(i), queryItems.get(i));
    }
  }

  /**
   * Tests the scenario where user makes a new list that IS currently in the datastore and does not
   * specify objects to add. Then, user is prompted to add elements (testing custom case) and also
   * update the list again after.
   */
  @Test
  public void testConsecutiveAdd() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Start a grocery list.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");

    ArrayList<String> items = new ArrayList<>(Arrays.asList("apples", "bananas", "ice cream"));
    tester.setCustomDatabase(
        "grocery", items, TimeUtils.stringToDate("2020-02-11T09:30:00-00:00").getTime());

    Output output = tester.getOutput();
    assertEquals(
        "Created! What are some items to add to your new grocery list?",
        output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(2, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    assertNull(entity.getProperty("items"));

    entity = databaseQuery.get(1);
    assertEquals("grocery (02/11/2020 09:30:00)", (String) entity.getProperty("listName"));
    List<String> queryItems = (List<String>) entity.getProperty("items");
    for (int i = 0; i < queryItems.size(); i++) {
      assertEquals(items.get(i), queryItems.get(i));
    }

    tester.setParameters(
        "Add milk and honey.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"milk and honey\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - custom");

    output = tester.getOutput();
    assertEquals("Updated!", output.getFulfillmentText());

    databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(2, databaseQuery.size());

    entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    queryItems = (List<String>) entity.getProperty("items");
    assertEquals(2, queryItems.size());
    assertEquals("milk", queryItems.get(0));
    assertEquals("honey", queryItems.get(1));

    entity = databaseQuery.get(1);
    assertEquals("grocery (02/11/2020 09:30:00)", (String) entity.getProperty("listName"));
    queryItems = (List<String>) entity.getProperty("items");
    assertEquals(items.size(), queryItems.size());
    for (int i = 0; i < queryItems.size(); i++) {
      assertEquals(items.get(i), queryItems.get(i));
    }

    tester.setParameters(
        "Add milk and honey.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"strawberries\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - custom");

    output = tester.getOutput();
    assertEquals("Updated!", output.getFulfillmentText());

    databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(2, databaseQuery.size());

    entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    queryItems = (List<String>) entity.getProperty("items");
    assertEquals(3, queryItems.size());
    assertEquals("milk", queryItems.get(0));
    assertEquals("honey", queryItems.get(1));
    assertEquals("strawberries", queryItems.get(2));

    entity = databaseQuery.get(1);
    assertEquals("grocery (02/11/2020 09:30:00)", (String) entity.getProperty("listName"));
    queryItems = (List<String>) entity.getProperty("items");
    assertEquals(items.size(), queryItems.size());
    for (int i = 0; i < queryItems.size(); i++) {
      assertEquals(items.get(i), queryItems.get(i));
    }
  }

  /** Tests the scenario where user tries to add elements to a list that is not in the datastore. */
  @Test
  public void testListNotFound() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Update my shopping list with hats and books.",
            "{\"list-name\":\"shopping\", "
                + "\"list-objects\":\"hats and books\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - add");

    ArrayList<String> items = new ArrayList<>(Arrays.asList("apples", "bananas", "ice cream"));
    tester.setCustomDatabase(
        "grocery", items, TimeUtils.stringToDate("2020-02-11T09:30:00-00:00").getTime());

    Output output = tester.getOutput();
    assertEquals(
        "Your shopping list has not been created yet, so a new list was created with those items.",
        output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(2, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("shopping", (String) entity.getProperty("listName"));
    List<String> queryItems = (List<String>) entity.getProperty("items");
    assertEquals(2, queryItems.size());
    assertEquals("hats", queryItems.get(0));
    assertEquals("books", queryItems.get(1));

    entity = databaseQuery.get(1);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    queryItems = (List<String>) entity.getProperty("items");
    for (int i = 0; i < queryItems.size(); i++) {
      assertEquals(items.get(i), queryItems.get(i));
    }
  }

  /**
   * Tests list aggregation: 1) creates a new list for user 1, 2) updates the list for user 1, 3)
   * creates a new list for user 1 of the same name, 4) updates that list, 5) creates a list for
   * user 2, 6) creates a differently named list for user 2, 7) check for correct aggregate numbers
   */
  @Test
  public void testListAggregation() throws Exception {

    // 1) creates a new list for user 1

    TestHelper tester =
        new TestHelper(
            "Start a grocery list with apples, bananas, and ice cream.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"apples, bananas, and ice cream\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");

    Output output = tester.getOutput();
    assertEquals("Created! Anything else you would like to add?", output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(1, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    ArrayList<String> items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("apples", items.get(0));
    assertEquals("bananas", items.get(1));
    assertEquals("ice cream", items.get(2));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate-groceri");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("apples")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("bananas")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));

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

    databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(1, databaseQuery.size());

    entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("apples", items.get(0));
    assertEquals("bananas", items.get(1));
    assertEquals("ice cream", items.get(2));
    assertEquals("pineapple", items.get(3));
    assertEquals("apple", items.get(4));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate-groceri");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(2, (long) entity.getProperty(MemoryUtils.stemmed("apples")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("bananas")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("pineapple")));

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

    databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(2, databaseQuery.size());

    entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("pineapple", items.get(0));
    assertEquals("apple", items.get(1));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate-groceri");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(3, (long) entity.getProperty(MemoryUtils.stemmed("apples")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("bananas")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));
    assertEquals(2, (long) entity.getProperty(MemoryUtils.stemmed("pineapple")));

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

    databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(2, databaseQuery.size());

    entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("pineapple", items.get(0));
    assertEquals("apple", items.get(1));
    assertEquals("chocolate", items.get(2));
    assertEquals("ice cream", items.get(3));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate-groceri");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(3, (long) entity.getProperty(MemoryUtils.stemmed("apples")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("bananas")));
    assertEquals(2, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));
    assertEquals(2, (long) entity.getProperty(MemoryUtils.stemmed("pineapple")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("chocolate")));

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

    databaseQuery = tester.fetchDatastoreEntities("List", "2");
    assertEquals(1, databaseQuery.size());

    entity = databaseQuery.get(0);
    assertEquals("Groceries", (String) entity.getProperty("listName"));
    items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("chocolate", items.get(0));
    assertEquals("ice cream", items.get(1));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate-groceri", "2");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("chocolate")));

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

    databaseQuery = tester.fetchDatastoreEntities("List", "2");
    assertEquals(2, databaseQuery.size());

    entity = databaseQuery.get(0);
    assertEquals("shopping", (String) entity.getProperty("listName"));
    items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("books", items.get(0));
    assertEquals("chocolate", items.get(1));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate-groceri", "2");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("chocolate")));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate-shop", "2");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("books")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("chocolate")));

    // 7) check for correct aggregate numbers

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(3, (long) entity.getProperty(MemoryUtils.stemmed("apples")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("bananas")));
    assertEquals(2, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));
    assertEquals(2, (long) entity.getProperty(MemoryUtils.stemmed("pineapple")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("chocolate")));

    databaseQuery = tester.fetchDatastoreEntities("ListAggregate", "2");
    assertEquals(1, databaseQuery.size());
    entity = databaseQuery.get(0);
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("books")));
    assertEquals(2, (long) entity.getProperty(MemoryUtils.stemmed("chocolate")));
    assertEquals(1, (long) entity.getProperty(MemoryUtils.stemmed("ice cream")));
  }
}
