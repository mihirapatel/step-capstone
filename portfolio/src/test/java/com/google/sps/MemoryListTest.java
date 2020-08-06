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

package com.google.sps.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.Entity;
import com.google.protobuf.*;
import com.google.sps.data.Output;
import com.google.sps.servlets.TestHelper;
import com.google.sps.utils.TimeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    tester.tearDown();
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
    assertEquals("Created!", output.getFulfillmentText());

    List<Entity> databaseQuery = tester.fetchDatastoreEntities("List");
    assertEquals(1, databaseQuery.size());

    Entity entity = databaseQuery.get(0);
    assertEquals("grocery", (String) entity.getProperty("listName"));
    ArrayList<String> items = (ArrayList<String>) entity.getProperty("items");
    assertEquals("apples", items.get(0));
    assertEquals("bananas", items.get(1));
    assertEquals("ice cream", items.get(2));
    tester.tearDown();
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
    tester.tearDown();
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
    assertEquals("Created!", output.getFulfillmentText());

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
    tester.tearDown();
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
    tester.tearDown();
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
    tester.tearDown();
  }
}
