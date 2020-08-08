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

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoryRecommendationsTest {

  private static Logger log = LoggerFactory.getLogger(MemoryRecommendationsTest.class);
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

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
    List<String> expectedItems = Arrays.asList("apples", "bananas", "ice cream");

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), expectedItems, true);

    TestHelper.checkAggregate(datastore, "groceri", expectedItems, Arrays.asList(1, 1, 1));
    TestHelper.checkFracAggregate(
        datastore, "groceri", expectedItems, Arrays.asList(1.0, 1.0, 1.0));

    // 2) updates list for user 1

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), Arrays.asList("pineapple", "apple"), false);

    TestHelper.checkAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(2, 1, 1, 1));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(2.0, 1.0, 1.0, 1.0));

    // 3) creates a new list for user 1 of the same name

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), Arrays.asList("pineapple", "apple"), true);

    TestHelper.checkAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(3, 1, 1, 2));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple"),
        Arrays.asList(1.6, 0.6, 0.6, 1.0));

    // 4) updates that list

    DatabaseUtils.storeUserListInformation(
        datastore,
        "1",
        StemUtils.stemmed("grocery"),
        Arrays.asList("chocolate", "ice cream"),
        false);

    TestHelper.checkAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(3, 1, 2, 2, 1));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(1.6, 0.6, 1.0, 1.0, 0.4));

    // 5) creates a list for user 2

    DatabaseUtils.storeUserListInformation(
        datastore,
        "2",
        StemUtils.stemmed("grocery"),
        Arrays.asList("chocolate", "ice cream"),
        true);

    TestHelper.checkAggregate(
        datastore, "groceri", "2", Arrays.asList("ice cream", "chocolate"), Arrays.asList(1, 1));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        "2",
        Arrays.asList("ice cream", "chocolate"),
        Arrays.asList(1.0, 1.0));

    // 5) creates a different list for user 2

    DatabaseUtils.storeUserListInformation(
        datastore, "2", StemUtils.stemmed("shopping"), Arrays.asList("books", "chocolate"), true);

    TestHelper.checkAggregate(
        datastore, "groceri", "2", Arrays.asList("ice cream", "chocolate"), Arrays.asList(1, 1));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        "2",
        Arrays.asList("ice cream", "chocolate"),
        Arrays.asList(1.0, 1.0));
    TestHelper.checkAggregate(
        datastore, "shop", "2", Arrays.asList("books", "chocolate"), Arrays.asList(1, 1));
    TestHelper.checkFracAggregate(
        datastore, "shop", "2", Arrays.asList("books", "chocolate"), Arrays.asList(1.0, 1.0));

    // 8) Create a new list of user 1

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), Arrays.asList("ice cream", "apple"), true);

    TestHelper.checkAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(4, 1, 3, 2, 1));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(1.36, 0.36, 1.0, 0.6, 0.24));

    // 9) Create a 4th empty grocery list for user 1 and check past recommendations

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), new ArrayList<String>(), true);

    assertEquals(
        "apple, ice cream, and pineapple",
        formatResult(
            RecommendationUtils.makePastRecommendations(
                "1", datastore, StemUtils.stemmed("grocery"))));

    TestHelper.checkAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(4, 1, 3, 2, 1));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "ice cream", "pineapple", "chocolate"),
        Arrays.asList(1.36, 0.36, 1.0, 0.6, 0.24));

    // 10) Create a 5th grocery list for user 1 and check past recommendations (should now
    // only have 2 recommendations)
    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), Arrays.asList("apple"), true);

    // 10) Create a 6th and 7th empty grocery list for user 1 and check past recommendations (last
    // one should now only have 1 recommendation)
    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), new ArrayList<String>(), true);
    assertEquals(
        "apple and ice cream",
        formatResult(
            RecommendationUtils.makePastRecommendations(
                "1", datastore, StemUtils.stemmed("grocery"))));

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), Arrays.asList("apple"), true);

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), new ArrayList<String>(), true);
    assertEquals(
        "apple",
        formatResult(
            RecommendationUtils.makePastRecommendations(
                "1", datastore, StemUtils.stemmed("grocery"))));
  }

  /**
   * Tests recommendations against other users: 1) create 5 users, 2) update user 1's list, and 3)
   * assert that only one recommendation is correctly returned for user 1.
   */
  @Test
  public void testUser1Recommendations() throws Exception {

    // Creates User 1 with history: 1 for item 1, 0.6 for item 2, and 0.2 for item 4.
    TestHelper.makeUserList(
        datastore,
        "1",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 2),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    TestHelper.makeUserList(
        datastore,
        "2",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    TestHelper.makeUserList(
        datastore,
        "3",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 5)));
    TestHelper.makeUserList(
        datastore,
        "4",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 4)));
    TestHelper.makeUserList(
        datastore,
        "5",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 0),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 5),
                new Pair<String, Integer>("donut", 4)));

    DatabaseUtils.storeUserListInformation(
        datastore, "1", StemUtils.stemmed("grocery"), Arrays.asList("apple", "banana"), true);
    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              formatResult(
                  RecommendationUtils.makeUserRecommendations(
                      "1", datastore, StemUtils.stemmed("grocery")),
                  StemUtils.stemmedList(Arrays.asList("apple", "banana")));
            });

    String expectedMessage = "No items in PQ to suggest";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  /**
   * Tests recommendations against other users: 1) create 5 users, 2) make new user 2's list, and 3)
   * assert that correct recommendation of banana is returned for user 2.
   */
  @Test
  public void testUser2Recommendations() throws Exception {

    // Creates User 1 with history: 1 for item 1, 0.6 for item 2, and 0.2 for item 4.
    TestHelper.makeUserList(
        datastore,
        "1",
        4,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 3),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    TestHelper.makeUserList(
        datastore,
        "2",
        4,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 3),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    TestHelper.makeUserList(
        datastore,
        "3",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 5)));
    TestHelper.makeUserList(
        datastore,
        "4",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 4)));
    TestHelper.makeUserList(
        datastore,
        "5",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 0),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 5),
                new Pair<String, Integer>("donut", 4)));

    DatabaseUtils.storeUserListInformation(
        datastore, "2", StemUtils.stemmed("grocery"), Arrays.asList("apple"), true);
    assertEquals(
        "banana",
        formatResult(
            RecommendationUtils.makeUserRecommendations(
                "2", datastore, StemUtils.stemmed("grocery")),
            StemUtils.stemmedList(Arrays.asList("apple"))));
  }

  /**
   * Tests recommendations against other users: 1) create 5 users, 2) make a new list, and 3) assert
   * that correct recommendation for carrot and donut is returned for user 4.
   */
  @Test
  public void testUser4Recommendations() throws Exception {

    // Creates User 1 with history: 1 for item 1, 0.6 for item 2, and 0.2 for item 4.
    TestHelper.makeUserList(
        datastore,
        "1",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 5),
                new Pair<String, Integer>("banana", 3),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    TestHelper.makeUserList(
        datastore,
        "2",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    TestHelper.makeUserList(
        datastore,
        "3",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 5)));
    TestHelper.makeUserList(
        datastore,
        "4",
        4,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 1),
                new Pair<String, Integer>("banana", 0),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 4)));
    TestHelper.makeUserList(
        datastore,
        "5",
        5,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 0),
                new Pair<String, Integer>("banana", 1),
                new Pair<String, Integer>("carrot", 5),
                new Pair<String, Integer>("donut", 4)));

    DatabaseUtils.storeUserListInformation(
        datastore, "4", StemUtils.stemmed("grocery"), Arrays.asList("egg"), true);
    assertEquals(
        "carrot and donut",
        formatResult(
            RecommendationUtils.makeUserRecommendations(
                "4", datastore, StemUtils.stemmed("grocery")),
            StemUtils.stemmedList(Arrays.asList("egg"))));
  }

  @Test
  public void testDecrementItem() throws Exception {
    TestHelper.makeUserList(
        datastore,
        "1",
        4,
        (List<Pair<String, Integer>>)
            Arrays.asList(
                new Pair<String, Integer>("apple", 4),
                new Pair<String, Integer>("banana", 3),
                new Pair<String, Integer>("carrot", 0),
                new Pair<String, Integer>("donut", 1)));
    DatabaseUtils.updateFractionalAggregation(
        datastore, "1", "groceri", Arrays.asList("apple", "banana", "carrot"), 4, false, false);

    TestHelper.checkAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "carrot", "donut"),
        Arrays.asList(4, 3, 0, 1));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        Arrays.asList("apples", "bananas", "carrot", "donut"),
        Arrays.asList(0.0, -0.4, -1.0, Math.pow(0.6, 3)));
  }

  public static String formatResultString(List<Pair<String, Double>> items) {
    if (items.size() == 1) {
      return items.get(0).getKey();
    }
    if (items.size() == 2) {
      return String.format("%s and %s", items.get(0).getKey(), items.get(1).getKey());
    } else {
      return String.format(
          "%s, %s, and %s", items.get(0).getKey(), items.get(1).getKey(), items.get(2).getKey());
    }
  }

  public static String formatResult(List<Pair<String, Double>> items, List<String> existingItems)
      throws IllegalStateException {
    List<Pair<String, Double>> filteredUserInterest =
        items.stream()
            .filter(
                e ->
                    (!existingItems.contains(StemUtils.stemmed(e.getKey()))
                        && (e.getValue() > 0.4)))
            .collect(Collectors.toList());
    if (filteredUserInterest.isEmpty()) {
      throw new IllegalStateException("No items in PQ to suggest");
    }
    return formatResultString(filteredUserInterest);
  }

  public static String formatResult(List<Pair<String, Double>> items) {
    List<Pair<String, Double>> filteredUserInterest =
        items.stream().filter(e -> (e.getValue() > 0.49)).collect(Collectors.toList());
    return formatResultString(filteredUserInterest);
  }
}
