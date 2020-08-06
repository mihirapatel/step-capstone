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

import com.google.protobuf.*;
import com.google.sps.data.Output;
import com.google.sps.data.Pair;
import com.google.sps.servlets.TestHelper;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryListRecommendationsTest {

  private static Logger log = LoggerFactory.getLogger(MemoryListRecommendationsTest.class);

  /**
   * Tests that past recommendations are being appropriately parsed for case of 0 items being
   * recommended.
   */
  @Test
  public void testNoPastRecommendation() throws Exception {

    // No past recs
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

    tester.tearDown();
  }

  /**
   * Tests that past recommendations are being appropriately parsed for case of one item being
   * recommended.
   */
  @Test
  public void testPastRecommendationOneItemDialog() throws Exception {
    TestHelper tester = new TestHelper();
    tester.setPastRecommendations(Arrays.asList(new Pair("apple", 1.0)));
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    assertEquals(
        "Created! Based on your previous grocery lists, would you like to add apple?",
        tester.getOutput().getFulfillmentText());

    // Yes response
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", " + "\"yes-objects\":\"\"}",
        "memory.list - make - yes");
    assertEquals("Updated!", tester.getOutput().getFulfillmentText());
    List<String> listItems =
        (List<String>) tester.fetchDatastoreEntities("List").get(0).getProperty("items");
    assertTrue(listItems.contains("apple"));

    tester.tearDown();
  }

  /**
   * Tests that past recommendations are being appropriately parsed for case of two items being
   * recommended.
   */
  @Test
  public void testPastRecommendationTwoItemsDialog() throws Exception {
    TestHelper tester = new TestHelper();
    tester.setPastRecommendations(
        Arrays.asList(new Pair("apple", 1.0), new Pair("pineapple", 0.7), new Pair("grapes", 0.2)));
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    assertEquals(
        "Created! Based on your previous grocery lists, would you like to add apple and pineapple?",
        tester.getOutput().getFulfillmentText());

    // No response
    tester.setParameters("no", "{\"list-name\":\"grocery\"}", "memory.list - make - no");
    assertEquals("Your preferences are noted.", tester.getOutput().getFulfillmentText());
    List<String> listItems =
        (List<String>) tester.fetchDatastoreEntities("List").get(0).getProperty("items");
    assertNull(listItems);
  }

  /**
   * Tests that past recommendations are being appropriately parsed for case of many items being
   * recommended.
   */
  @Test
  public void testPastRecommendationManyItemsDialog() throws Exception {
    TestHelper tester = new TestHelper();
    tester.setPastRecommendations(
        Arrays.asList(
            new Pair("apple", 1.0),
            new Pair("watermelon", 0.9),
            new Pair("blueberries", 0.8),
            new Pair("pineapple", 0.7),
            new Pair("grapes", 0.2)));
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    assertEquals(
        "Created! Based on your previous grocery lists, would you like to add apple, watermelon, and blueberries?",
        tester.getOutput().getFulfillmentText());

    // Yes and no response
    tester.setParameters(
        "yes",
        "{\"list-name\":\"grocery\","
            + "\"yes-objects\":\"apple and watermelon but not blueberries\"}",
        "memory.list - make - yes");
    assertEquals("Updated!", tester.getOutput().getFulfillmentText());
    List<String> listItems =
        (List<String>) tester.fetchDatastoreEntities("List").get(0).getProperty("items");
    assertTrue(listItems.contains("apple"));
    assertTrue(listItems.contains("watermelon"));
    assertFalse(listItems.contains("blueberries"));
  }

  /**
   * Tests that user recommendations are being appropriately parsed for case of 0 items being
   * recommended.
   */
  @Test
  public void testNoUserRecommendation() throws Exception {

    // No user recs
    TestHelper tester =
        new TestHelper(
            "Start a grocery list.",
            "{\"list-name\":\"grocery\", "
                + "\"list-objects\":\"apple and pears\","
                + "\"new-list\": \"\","
                + "\"generic-list\": \"\"}",
            "memory.list - make");
    Output output = tester.getOutput();
    assertEquals("Created!", output.getFulfillmentText());

    tester.tearDown();
  }

  /**
   * Tests that user recommendations are being appropriately parsed for case of one item being
   * recommended.
   */
  @Test
  public void testUserRecommendationOneItemDialog() throws Exception {
    TestHelper tester = new TestHelper();
    tester.setUserRecommendations(Arrays.asList(new Pair("apple", 1.0)));
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"blueberries and mangos\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    assertEquals(
        "Created! Based on your list item preferences, you might be interested in adding apple to your grocery list.",
        tester.getOutput().getFulfillmentText());

    // Yes response
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", " + "\"yes-objects\":\"\"}",
        "memory.list - make - yes");
    assertEquals("Updated!", tester.getOutput().getFulfillmentText());
    List<String> listItems =
        (List<String>) tester.fetchDatastoreEntities("List").get(0).getProperty("items");
    assertTrue(listItems.contains("apple"));

    tester.tearDown();
  }

  /**
   * Tests that user recommendations are being appropriately parsed for case of two items being
   * recommended.
   */
  @Test
  public void testUserRecommendationTwoItemsDialog() throws Exception {
    TestHelper tester = new TestHelper();
    tester.setUserRecommendations(
        Arrays.asList(new Pair("apple", 1.0), new Pair("pineapple", 0.7), new Pair("grapes", 0.2)));
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"blueberries\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    assertEquals(
        "Created! Based on your list item preferences, you might be interested in adding apple and pineapple to your grocery list.",
        tester.getOutput().getFulfillmentText());

    // No response
    tester.setParameters("no", "{\"list-name\":\"grocery\"}", "memory.list - make - no");
    tester.setUserRecommendations(
        Arrays.asList(
            new Pair("apple", 0.0), new Pair("pineapple", -0.3), new Pair("grapes", 0.2)));
    assertEquals("Your preferences are noted.", tester.getOutput().getFulfillmentText());
    List<String> listItems =
        (List<String>) tester.fetchDatastoreEntities("List").get(0).getProperty("items");
    assertFalse(listItems.contains("apple"));
    assertFalse(listItems.contains("pineapple"));
  }

  /**
   * Tests that user recommendations are being appropriately parsed for case of many items being
   * recommended.
   */
  @Test
  public void testUserRecommendationManyItemsDialog() throws Exception {
    TestHelper tester = new TestHelper();
    tester.setUserRecommendations(
        Arrays.asList(
            new Pair("apple", 1.0),
            new Pair("watermelon", 0.9),
            new Pair("blueberries", 0.8),
            new Pair("pineapple", 0.7),
            new Pair("mangos", 0.65),
            new Pair("grapes", 0.2)));
    tester.setParameters(
        "Start a grocery list.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    assertEquals(
        "Created! Based on your list item preferences, you might be interested in adding watermelon, blueberries, and pineapple to your grocery list.",
        tester.getOutput().getFulfillmentText());

    // Yes and no response
    tester.setParameters(
        "yes",
        "{\"list-name\":\"grocery\","
            + "\"yes-objects\":\"pineapple and watermelon but not blueberries\"}",
        "memory.list - make - yes");
    tester.setUserRecommendations(
        Arrays.asList(
            new Pair("apple", 1.0),
            new Pair("watermelon", 0.9),
            new Pair("blueberries", -0.2),
            new Pair("pineapple", 0.7),
            new Pair("mangos", 0.65),
            new Pair("grapes", 0.2)));
    assertEquals(
        "Updated! Based on your list item preferences, you might be interested in adding mangos to your grocery list.",
        tester.getOutput().getFulfillmentText());
    List<String> listItems =
        (List<String>) tester.fetchDatastoreEntities("List").get(0).getProperty("items");
    assertTrue(listItems.contains("pineapple"));
    assertTrue(listItems.contains("watermelon"));
    assertFalse(listItems.contains("blueberries"));
  }
}
