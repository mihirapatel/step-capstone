package com.google.sps.data;

import com.google.appengine.api.datastore.Entity;
import com.google.sps.utils.MemoryUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private List<String> itemIndexMapping;

  public Recommender(int k) {
    K = k;
  }

  public Recommender() {
    this(2);
  }

  /**
   * Generates recommendations from given datastore entries for the given user.
   *
   * @param userEntity Fractional aggregate entity of the current user
   * @param entities List of fractional aggregate entities containing all entities except the
   *     current user's in the database.
   * @param uniqueItems Set of all unique property items in all entities.
   * @return List of Pairs where each pair contains the item name and the expected frequency that
   *     the user would add an item to their list.
   */
  public List<Pair<String, Double>> makeRecommendations(
      Entity userEntity, List<Entity> entities, Set<String> uniqueItems) {
    SimpleMatrix dataMatrix = createMatrixFromDatabaseEntities(userEntity, entities, uniqueItems);
    SimpleMatrix userFeatures = SimpleMatrix.random_DDRM​(dataMatrix.numRows(), K, -2.0, 2.0, new Random(1));
    SimpleMatrix itemFeatures = SimpleMatrix.random_DDRM​(K, dataMatrix.numCols(), -2.0, 2.0, new Random(1));
    SimpleMatrix predictedResults = matrixFactorization(dataMatrix, userFeatures, itemFeatures);
    return getUserPredictions(predictedResults);
  }

   /**
   * Converts database entities into fully populated data matrix.
   *
   * @param userEntity Fractional aggregate entity of the current user
   * @param entities List of fractional aggregate entities containing all entities except the
   *     current user's in the database.
   * @param uniqueItems Set of all unique property items in all entities.
   * @return Matrix containing values for each user as rows, items as columns, and fractional number of
   *     times an item has appeared on the user's list as values. 0.0 if user never listed an item.
   */
  SimpleMatrix createMatrixFromDatabaseEntities(
      Entity userEntity, List<Entity> entities, Set<String> uniqueItems) {
    itemIndexMapping = new ArrayList<String>(uniqueItems);
    Collections.sort(itemIndexMapping);
    double[][] userItemData = new double[entities.size() + 1][uniqueItems.size()];
    addEntity(userItemData, userEntity, 0);
    for (int i = 0; i < entities.size(); i++) {
      addEntity(userItemData, entities.get(i), i + 1);
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
    for (String item : e.getProperties().keySet()) {
      if (MemoryUtils.AGG_ENTITY_ID_PROPERTIES.contains(item)) {
        continue;
      }
      userItemData[row][itemIndexMapping.indexOf(item)] = (double) e.getProperty(item);
    }
  }

   /**
   * Uses matrix factorization to compute the predicted result matrix by continuously 
   * multiplying userFeatures and itemFeatures matrices. Based on the error between feature
   * matrix product and given data matrix, it increments/adjusts the feature matrices and 
   * tries again until error reaches threshold of 0.001 or STEPS iterations has been completed.
   *
   * @param dataMatrix Matrix with real data values for user list item history.
   * @param userFeatures Matrix with guesses for how much each user is affiliated with the K features
   * @param itemFeatures Matrix with guesses for how much each item is affiliated with the K features
   * @return Matrix with the final best prediction for userFeatures * itemFeatures
   */
  SimpleMatrix matrixFactorization(
      SimpleMatrix dataMatrix, SimpleMatrix userFeatures, SimpleMatrix itemFeatures) {
    log.info("Input matrix: " + dataMatrix);
    for (int step = 0; step < STEPS; step++) {
      double updated_learning_rate = Math.max(ALPHA_START / (Math.sqrt(step + 1)), 0.005);
      for (int row = 0; row < dataMatrix.numRows(); row++) {
        for (int col = 0; col < dataMatrix.numCols(); col++) {
          double element = dataMatrix.get(row, col);
          if (element > 0) {
            double error =
                element
                    - userFeatures
                        .extractVector(true, row)
                        .dot(itemFeatures.extractVector(false, col));
            for (int k = 0; k < K; k++) {
              double userFeatures_ik = userFeatures.get(row, k);
              double itemFeatures_kj = itemFeatures.get(k, col);
              userFeatures.set(
                  row,
                  k,
                  increment(userFeatures_ik, itemFeatures_kj, error, updated_learning_rate));
              itemFeatures.set(
                  k,
                  col,
                  increment(itemFeatures_kj, userFeatures_ik, error, updated_learning_rate));
            }
          }
        }
      }
      SimpleMatrix estimatedData = userFeatures.mult(itemFeatures);
      double totalError = 0.0;
      for (int row = 0; row < dataMatrix.numRows(); row++) {
        for (int col = 0; col < dataMatrix.numCols(); col++) {
          double element = dataMatrix.get(row, col);
          if (element > 0) {
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
   * @param error Error between the product at this value and the real data matrix at the corresponding value.
   * @param alpha Current alpha learning rate
   * @return Double representing the incremental adjustment for e1.
   */
  private double increment(double e1, double e2, double error, double alpha) {
    return e1 + alpha * (2 * error * e2 - BETA * e1);
  }

   /**
   * Extracts the row corresponding to the given user (always the 0th row) and 
   * maps each column with the corresponding item name so that each item name 
   * corresponds to the frequency prediction made by matrix factorization.
   *
   * @param predictedResults Matrix result of matrix factorization.
   * @return List of Pairs where each pair contains the item name and the expected frequency that
   *     the user would add an item to their list.
   */
  private List<Pair<String, Double>> getUserPredictions(SimpleMatrix predictedResults) {
    SimpleMatrix userRow = predictedResults.extractVector(true, 0);
    List<Pair<String, Double>> pairedUserPredictions = new ArrayList<>();
    for (int i = 0; i < itemIndexMapping.size(); i++) {
      pairedUserPredictions.add(
          new Pair<String, Double>(itemIndexMapping.get(i), userRow.get(0, i)));
    }
    return pairedUserPredictions;
  }
}
