package com.google.sps.data;

import static org.junit.Assert.*;

import com.google.appengine.api.datastore.Entity;
import com.google.sps.servlets.TestHelper;
import com.google.sps.utils.StemUtils;
import java.util.*;
import org.ejml.simple.SimpleMatrix;
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
    TestHelper tester = new TestHelper();
    List<String> items = Arrays.asList("apple", "banana", "carrot", "donut");

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

    tester.checkFracAggregate("groceri", "1", items, Arrays.asList(1.0, 0.6, 0.0, 0.2));
    tester.checkFracAggregate("groceri", "2", items, Arrays.asList(0.8, 0.0, 0.0, 0.2));
    tester.checkFracAggregate("groceri", "3", items, Arrays.asList(0.2, 0.2, 0.0, 1.0));
    tester.checkFracAggregate("groceri", "4", items, Arrays.asList(0.2, 0.0, 0.0, 0.8));
    tester.checkFracAggregate("groceri", "5", items, Arrays.asList(0.0, 0.2, 1.0, 0.8));

    List<Entity> entities = tester.fetchDatastoreAllUsers("Frac-groceri");
    Recommender rec = new Recommender();
    SimpleMatrix matrix =
        rec.createMatrixFromDatabaseEntities(
            entities.remove(0), entities, new HashSet<String>(StemUtils.stemmedList(items)));
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        assertEquals(dataMatrix.get(i, j) / 5, matrix.get(i, j), 0.01);
      }
    }
  }
}