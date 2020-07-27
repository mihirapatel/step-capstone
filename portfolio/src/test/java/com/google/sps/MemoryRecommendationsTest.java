package com.google.sps.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.*;
import com.google.sps.data.Output;
import com.google.sps.data.Pair;
import com.google.sps.servlets.TestHelper;
import com.google.sps.utils.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoryRecommendationsTest {

  private static Logger log = LoggerFactory.getLogger(MemoryRecommendationsTest.class);

  /**
   * Tests list aggregation: 1) creates a new list for user 1, 2) updates the list for user 1, 3)
   * creates a new list for user 1 of the same name, 4) updates that list, 5) creates a list for
   * user 2, 6) creates a differently named list for user 2, 7) check for correct aggregate numbers
   * 8) create a new list for user 1 of the same name, 9) create a new list for user 1 of the same
   * name and check for recommendation.
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
    assertEquals("Created!", output.getFulfillmentText());

    List<String> expectedItems = Arrays.asList("apples", "bananas", "ice cream");
    tester.checkDatabaseItems(1, "grocery", expectedItems);
    tester.checkAggregate("groceri", expectedItems, Arrays.asList(1, 1, 1));
    tester.checkFracAggregate("groceri", expectedItems, Arrays.asList(1.0, 1.0, 1.0));

    // 2) updates list for user 1

    tester.setParameters(
        "Update grocery list with pineapple and apple.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"pineapple and apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - add");

    output = tester.getOutput();
    assertEquals("Updated!", output.getFulfillmentText());

    tester.checkDatabaseItems(
        1, "grocery", Arrays.asList("apples", "bananas", "ice cream", "pineapple", "apple"));
    tester.checkAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(2, 1, 1, 1));
    tester.checkFracAggregate(
        "groceri",
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
    assertEquals("Created!", output.getFulfillmentText());

    tester.checkDatabaseItems(2, "grocery", Arrays.asList("pineapple", "apple"));
    tester.checkAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(3, 1, 1, 2));
    tester.checkFracAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(1.6, 0.6, 0.6, 1.0));

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

    tester.checkDatabaseItems(
        2, "grocery", Arrays.asList("pineapple", "apple", "chocolate", "ice cream"));
    tester.checkAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(3, 1, 2, 2, 1));
    tester.checkFracAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(1.6, 0.6, 1.0, 1.0, 0.4));

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
    assertEquals("Created!", output.getFulfillmentText());

    tester.checkDatabaseItems(1, "2", "Groceries", Arrays.asList("chocolate", "ice cream"));
    tester.checkAggregate(
        "groceri", "2", Arrays.asList("ice cream", "chocolate"), Arrays.asList(1, 1));
    tester.checkFracAggregate(
        "groceri", "2", Arrays.asList("ice cream", "chocolate"), Arrays.asList(1.0, 1.0));

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
    assertEquals("Created!", output.getFulfillmentText());

    tester.checkDatabaseItems(2, "2", "shopping", Arrays.asList("books", "chocolate"));
    tester.checkAggregate(
        "groceri", "2", Arrays.asList("ice cream", "chocolate"), Arrays.asList(1, 1));
    tester.checkFracAggregate(
        "groceri", "2", Arrays.asList("ice cream", "chocolate"), Arrays.asList(1.0, 1.0));
    tester.checkAggregate("shop", "2", Arrays.asList("books", "chocolate"), Arrays.asList(1, 1));
    tester.checkFracAggregate(
        "shop", "2", Arrays.asList("books", "chocolate"), Arrays.asList(1.0, 1.0));

    // 8) Create a new list of user 1
    tester.setParameters(
        "Start a grocery list with ice cream and apple.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"ice cream and apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    tester.setUser("test@example.com", "1");

    output = tester.getOutput();
    assertEquals("Created!", output.getFulfillmentText());

    tester.checkDatabaseItems(3, "grocery", Arrays.asList("ice cream", "apple"));
    tester.checkAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(4, 1, 3, 2, 1));
    tester.checkFracAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(1.36, 0.36, 1.0, 0.6, 0.24));

    // 9) Create a 4th empty grocery list for user 1 and check past recommendations
    tester.setParameters(
        "Start a grocery list 4.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");

    output = tester.getOutput();
    assertEquals(
        "Created! Based on your previous lists, would you like to add apple, ice cream, and pineapple?",
        output.getFulfillmentText());

    tester.checkDatabaseItems(4, "grocery", new ArrayList<String>());
    tester.checkAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(4, 1, 3, 2, 1));
    tester.checkFracAggregate(
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(1.36, 0.36, 1.0, 0.6, 0.24));

    // 10) Create a 5th grocery list for user 1 and check past recommendations (should now
    // only have 2 recommendations)
    tester.setParameters(
        "Update a grocery list with apple.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - add");

    output = tester.getOutput();
    assertEquals(
        "Updated!",
        output.getFulfillmentText());

    // 10) Create a 6th and 7th empty grocery list for user 1 and check past recommendations (last
    // one should now only have 1 recommendation)
    tester.setParameters(
        "Start a grocery list 6.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");

    output = tester.getOutput();
    assertEquals(
        "Created! Based on your previous lists, would you like to add apple and ice cream?",
        output.getFulfillmentText());

    tester.setParameters(
        "Add apple to my grocery list.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - add");

    output = tester.getOutput();
    assertEquals(
        "Updated!",
        output.getFulfillmentText());

    tester.setParameters(
        "Start a grocery list 6.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");

    output = tester.getOutput();
    assertEquals(
        "Created! Based on your previous lists, would you like to add apple?",
        output.getFulfillmentText());
  }

  /**
   * Tests recommendations against other users: 1) create 5 users, 2) update user 1's list, and 3)
   * assert that only one recommendation is correctly returned for user 1.
   */
  @Test
  public void testUser1Recommendations() throws Exception {
    TestHelper tester = new TestHelper();

    // Creates User 1 with history: 1 for item 1, 0.6 for item 2, and 0.2 for item 4.
    tester.makeUserList(
        "1",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 2),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    tester.makeUserList(
        "2",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    tester.makeUserList(
        "3",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 5)));
    tester.makeUserList(
        "4",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 4)));
    tester.makeUserList(
        "5",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 0),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 5),
                new Pair<String, Integer>("donut", 4)));

    tester.setUser("test@example.com", "1");
    tester.setParameters(
        "Update grocery list with apple and banana.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"apple and banana\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - add");
    log.info("Testing User 1's recommendations. Should be nothing");
    Output output = tester.getOutput();
    assertEquals(
        "Updated!",
        output.getFulfillmentText());
  }

  /**
   * Tests recommendations against other users: 1) create 5 users, 2) make new user 2's list, and 3)
   * assert that correct recommendation of banana is returned for user 2.
   */
  @Test
  public void testUser2Recommendations() throws Exception {
    TestHelper tester = new TestHelper();

    // Creates User 1 with history: 1 for item 1, 0.6 for item 2, and 0.2 for item 4.
    tester.makeUserList(
        "1",
        4,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 3),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    tester.makeUserList(
        "2",
        4,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 3),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    tester.makeUserList(
        "3",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 5)));
    tester.makeUserList(
        "4",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 4)));
    tester.makeUserList(
        "5",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 0),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 5),
                new Pair<String, Integer>("donut", 4)));

    tester.setUser("test@example.com", "2");
    tester.setParameters(
        "Create a grocery list with apple.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"apple\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    log.info("Testing User 2's recommendations. Should be banana");
    Output output = tester.getOutput();
    assertEquals("Created! Based on your list item history, you might be interested in adding banana to your grocery list.", output.getFulfillmentText());
  }

  /**
   * Tests recommendations against other users: 1) create 5 users, 2) make a new list, and 3) assert
   * that correct recommendation for carrot and donut is returned for user 4.
   */
  @Test
  public void testUser4Recommendations() throws Exception {
    TestHelper tester = new TestHelper();

    // Creates User 1 with history: 1 for item 1, 0.6 for item 2, and 0.2 for item 4.
    tester.makeUserList(
        "1",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 5),
                new Pair<String, Integer>("banana", 3),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    tester.makeUserList(
        "2",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    tester.makeUserList(
        "3",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 5)));
    tester.makeUserList(
        "4",
        4,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 4)));
    tester.makeUserList(
        "5",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 0),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 5),
                new Pair<String, Integer>("donut", 4)));

    tester.setUser("test@example.com", "4");
    tester.setParameters(
        "Make a new grocery list with egg.",
        "{\"list-name\":\"grocery\", "
            + "\"list-objects\":\"egg\","
            + "\"new-list\": \"\","
            + "\"generic-list\": \"\"}",
        "memory.list - make");
    Output output = tester.getOutput();
    log.info("Testing User 4's recommendations. Should be carrot and donut??.");
    assertEquals(
        "Created! Based on your list item history, you might be interested in adding carrot and donut to your grocery list.",
        output.getFulfillmentText());
  }
}
