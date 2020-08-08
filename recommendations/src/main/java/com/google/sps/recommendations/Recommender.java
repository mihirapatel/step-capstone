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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.ejml.simple.SimpleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recommender {

  private static Logger log = LoggerFactory.getLogger(Recommender.class);

  private int K;
  private final int STEPS = 10000;
  private final double ALPHA_START = 0.1;
  private final double BETA = 0.02;
  private final double DELTA = 0.01;
  private List<String> itemIndexMapping;
  private Map<Integer, String> userIDIndexMapping;

 /**
  * Recommender constructor with a custom number of latent features.
  *
  * @param k Number of latent features to use in matrix factorization
  */
  public Recommender(int k) {
    K = k;
  }

 /** 
  * Recommender constructor with default 2 latent features.
  */
  public Recommender() {
    this(2);
  }

  /**
   * Generates recommendations from given datastore entries for the given user.
   *
   * @param datastore Datastore instance
   * @param stemmedListName stemmed name of the list
   * @param entities List of fractional aggregate entities containing all entities except the
   *     current user's in the database.
   * @param uniqueItems Set of all unique property items in all entities.
   * @return List of Pairs where each pair contains the item name and the expected frequency that
   *     the user would add an item to their list.
   */
  public void makeRecommendations(
      DatastoreService datastore,
      String stemmedListName,
      List<Entity> entities,
      Set<String> uniqueItems) 
      throws IllegalStateException {
    SimpleMatrix dataMatrix = createMatrixFromDatabaseEntities(entities, uniqueItems);
    SimpleMatrix userFeatures = SimpleMatrix.random_DDRM​(dataMatrix.numRows(), K, -2.0, 2.0, new Random(1));
    SimpleMatrix itemFeatures = SimpleMatrix.random_DDRM​(K, dataMatrix.numCols(), -2.0, 2.0, new Random(1));
    SimpleMatrix predictedResults = matrixFactorization(dataMatrix, userFeatures, itemFeatures);
    savePredictions(datastore, stemmedListName, predictedResults);
  }

  /**
   * Converts database entities into fully populated data matrix.
   *
   * @param userEntity Fractional aggregate entity of the current user
   * @param entities List of fractional aggregate entities containing all entities except the
   *     current user's in the database.
   * @param uniqueItems Set of all unique property items in all entities.
   * @return Matrix containing values for each user as rows, items as columns, and fractional number
   *     of times an item has appeared on the user's list as values. 0.0 if user never listed an
   *     item.
   */
  SimpleMatrix createMatrixFromDatabaseEntities(List<Entity> entities, Set<String> uniqueItems) {
    itemIndexMapping = new ArrayList<String>(uniqueItems);
    userIDIndexMapping = new HashMap<>();
    Collections.sort(itemIndexMapping);
    double[][] userItemData = new double[entities.size()][uniqueItems.size()];
    for (int i = 0; i < entities.size(); i++) {
      addEntity(userItemData, entities.get(i), i);
    }
    return new SimpleMatrix(userItemData);
  }

  /**
   * Adds a single entity to a row of the data array.
   *
   * @param userItemData double array containing unfilled matrix values
   * @param e Entity to fill the given row of the matrix
   * @param row The row to be for corresponding entity.
   */
  private void addEntity(double[][] userItemData, Entity e, int row) {
    userIDIndexMapping.put(row, (String) e.getProperty("userID"));
    for (String item : e.getProperties().keySet()) {
      if (DatabaseUtils.AGG_ENTITY_ID_PROPERTIES.contains(item)) {
        continue;
      }
      userItemData[row][itemIndexMapping.indexOf(item)] = (double) e.getProperty(item);
    }
  }

  /**
   * Uses matrix factorization to compute the predicted result matrix by continuously multiplying
   * userFeatures and itemFeatures matrices. Based on the error between feature matrix product and
   * given data matrix, it increments/adjusts the feature matrices and tries again until error
   * reaches threshold of 0.001 or STEPS iterations has been completed.
   *
   * @param dataMatrix Matrix with real data values for user list item history.
   * @param userFeatures Matrix with guesses for how much each user is affiliated with the K
   *     features
   * @param itemFeatures Matrix with guesses for how much each item is affiliated with the K
   *     features
   * @return Matrix with the final best prediction for userFeatures * itemFeatures
   */
  SimpleMatrix matrixFactorization(
      SimpleMatrix dataMatrix, SimpleMatrix userFeatures, SimpleMatrix itemFeatures) 
      throws IllegalStateException {
    log.info("Input matrix: " + dataMatrix);
    for (int step = 0; step < STEPS; step++) {
      double updatedLearningRate = Math.max(ALPHA_START / (Math.sqrt(step + 1)), 0.005);
      for (int row = 0; row < dataMatrix.numRows(); row++) {
        for (int col = 0; col < dataMatrix.numCols(); col++) {
          double element = dataMatrix.get(row, col);
          if (Math.abs(element - 0.0) > DELTA) {
            double error =
                element
                    - userFeatures
                        .extractVector(true, row)
                        .dot(itemFeatures.extractVector(false, col));
            for (int k = 0; k < K; k++) {
              double userFeatures_ik = userFeatures.get(row, k);
              double itemFeatures_kj = itemFeatures.get(k, col);
              userFeatures.set(
                  row, k, increment(userFeatures_ik, itemFeatures_kj, error, updatedLearningRate));
              itemFeatures.set(
                  k, col, increment(itemFeatures_kj, userFeatures_ik, error, updatedLearningRate));
            }
          }
        }
      }
      SimpleMatrix estimatedData = userFeatures.mult(itemFeatures);
      if (estimatedData.hasUncountable()) {
        log.error("Failure at step: " + step);
        log.error("User feature matrix: " + userFeatures);
        log.error("Item feature matrix: " + itemFeatures);
        throw new IllegalStateException("NaN error in matrix factorization.");
      }
      double totalError = 0.0;
      for (int row = 0; row < dataMatrix.numRows(); row++) {
        for (int col = 0; col < dataMatrix.numCols(); col++) {
          double element = dataMatrix.get(row, col);
          if (Math.abs(element - 0.0) > DELTA) {
            totalError +=
                Math.pow(
                    element
                        - userFeatures
                            .extractVector(true, row)
                            .dot(itemFeatures.extractVector(false, col)),
                    2);
            for (int k = 0; k < K; k++) {
              totalError +=
                  (BETA / 2)
                      * (Math.pow(userFeatures.get(row, k), 2)
                          + Math.pow(itemFeatures.get(k, col), 2));
            }
          }
        }
      }
      if (totalError < 0.001) {
        return estimatedData;
      }
    }
    log.info("Return matrix: " + userFeatures.mult(itemFeatures));
    return userFeatures.mult(itemFeatures);
  }

  /**
   * Increments each element based on the error at that element.
   *
   * @param e1 The value being adjusted.
   * @param e2 Corresponding value in the other matrix
   * @param error Error between the product at this value and the real data matrix at the
   *     corresponding value.
   * @param alpha Current alpha learning rate
   * @return Double representing the incremental adjustment for e1.
   */
  private double increment(double e1, double e2, double error, double alpha) {
    return e1 + alpha * (2 * error * e2 - BETA * e1);
  }

  /**
   * Stores results of matrix factorization into database.
   *
   * @param datastore Datastore instance
   * @param stemmedListName Stemmed name of the list that predictions were calculated for
   * @param predictedResults Matrix result of matrix factorization.
   * @return List of Pairs where each pair contains the item name and the expected frequency that
   *     the user would add an item to their list.
   */
  private void savePredictions(
      DatastoreService datastore, String stemmedListName, SimpleMatrix predictedResults) {
    for (int i = 0; i < predictedResults.numRows(); i++) {
      Entity entity = new Entity("UserPredictions-" + stemmedListName, userIDIndexMapping.get(i));
      for (int j = 0; j < itemIndexMapping.size(); j++) {
        entity.setProperty(itemIndexMapping.get(j), predictedResults.get(i, j));
      }
      log.info("Stored prediction entity: " + entity);
      datastore.put(entity);
    }
  }
}
