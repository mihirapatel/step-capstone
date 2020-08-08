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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.List;
import org.ejml.simple.SimpleMatrix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public final class RecommenderTest {

  private static Logger log = LoggerFactory.getLogger(RecommenderTest.class);
  private double[] data =
      new double[] {
        5.0, 3.0, 0.0, 1.0,
        4.0, 0.0, 0.0, 1.0,
        1.0, 1.0, 0.0, 5.0,
        1.0, 0.0, 0.0, 4.0,
        0.0, 1.0, 5.0, 4.0
      };
  private SimpleMatrix dataMatrix = new SimpleMatrix(5, 4, true, data);
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

  /** Tests that matrix factorization method is working reasonably correctly. */
  @Test
  public void testFactorization() throws Exception {
    SimpleMatrix userFeatures = SimpleMatrix.random_DDRM​(dataMatrix.numRows(), 2, -1.0, 1.0, new Random(1));
    SimpleMatrix itemFeatures = SimpleMatrix.random_DDRM​(2, dataMatrix.numCols(), -1.0, 1.0, new Random(1));

    Recommender rec = new Recommender();
    SimpleMatrix predictedResults = rec.matrixFactorization(dataMatrix, userFeatures, itemFeatures);

    double[] expected =
        new double[] {
          4.97, 2.98, 2.18, 0.98,
          3.97, 2.40, 1.97, 0.99,
          1.02, 0.93, 5.32, 4.93,
          1.00, 0.85, 4.59, 3.93,
          1.36, 1.07, 4.89, 4.12
        };
    SimpleMatrix expectedMatrix = new SimpleMatrix(5, 4, true, expected);
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        assertEquals(expectedMatrix.get(i, j) > 2.5, predictedResults.get(i, j) > 2.5);
      }
    }
  }

  /** Create 5 user database entries and check that matrix is properly created from the data. */
  @Test
  public void testMatrixCreation() throws Exception {

    List<String> items = Arrays.asList("apple", "banana", "carrot", "donut");

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
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        "1",
        items,
        Arrays.asList(1.0, Math.pow(0.6, 2), 0.0, Math.pow(0.6, 4)));
    TestHelper.checkFracAggregate(
        datastore, "groceri", "2", items, Arrays.asList(1.0, 0.0, 0.0, Math.pow(0.6, 3)));
    TestHelper.checkFracAggregate(
        datastore,
        "groceri",
        "3",
        items,
        Arrays.asList(Math.pow(0.6, 4), Math.pow(0.6, 4), 0.0, 1.0));
    TestHelper.checkFracAggregate(
        datastore, "groceri", "4", items, Arrays.asList(Math.pow(0.6, 3), 0.0, 0.0, 1.0));
    TestHelper.checkFracAggregate(
        datastore, "groceri", "5", items, Arrays.asList(0.0, Math.pow(0.6, 4), 1.0, 0.6));

    List<Entity> entities = TestHelper.fetchDatastoreAllUsers(datastore, "Frac-groceri");
    Recommender rec = new Recommender();
    SimpleMatrix matrix =
        rec.createMatrixFromDatabaseEntities(
            entities, new HashSet<String>(StemUtils.stemmedList(items)));
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        if (dataMatrix.get(i, j) < 1) {
          assertEquals(0.0, matrix.get(i, j), 0.01);
        } else {
          int maxPower = i % 2 == 1 ? 4 : 5;
          assertEquals(Math.pow(0.6, maxPower - dataMatrix.get(i, j)), matrix.get(i, j), 0.01);
        }
      }
    }
  }
}
