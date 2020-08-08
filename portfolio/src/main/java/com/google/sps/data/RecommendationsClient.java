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

package com.google.sps.data;

import com.google.appengine.api.log.InvalidRequestException;
import com.google.gson.Gson;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RecommendationsClient {

  private static Logger log = LoggerFactory.getLogger(RecommendationsClient.class);
  private String userID;
  private static final String BASE_URL = "https://arliu-step-2020-3.wl.r.appspot.com/";

  /**
   * Sets the userID for the recommendations client instance.
   *
   * @param userID String containing current user's unique ID
   */
  public void setUserID(String userID) {
    this.userID = userID;
  }

  /**
   * Starts a backend thread to call recommendations API to store newly added list item into
   * recommendations database.
   *
   * @param stemmedListName The stemmed name of the list to store aggregation information for.
   * @param items List of strings containing items to add to list
   * @param newList Indicates whether the list is a new list (true) or updating existing (false)
   * @param positiveFeedback Boolean to indicate if the items are liked/added by user (true) or not
   *     liked by user (false)
   */
  public void saveAggregateListData(
      String stemmedListName, List<String> items, boolean newList, boolean positiveFeedback)
      throws InvalidRequestException {
    log.info("making storeInfo api request");
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            callRecommendationsAPI(stemmedListName, items, newList, positiveFeedback);
          }
        });

    log.info("started api request");
  }

  /**
   * Creates an API call to the recommender API and sends list item data to be stored. Assumes that
   * list of items is not empty.
   *
   * @param stemmedListName Stemmed name of the list
   * @param items List of strings containing items to add to list
   * @param newList Indicates whether this is the beginning of a list for count purposes
   * @param positiveFeedback Boolean indicating whether user is adding elements or responding
   *     negatively to recommendations
   */
  private void callRecommendationsAPI(
      String stemmedListName, List<String> items, boolean newList, boolean positiveFeedback) {
    RestTemplate restTemplate = new RestTemplate();
    String urlString =
        BASE_URL
            + "storeInfo?userID="
            + userID
            + "&stemmedListName="
            + stemmedListName
            + "&newList="
            + newList
            + "&positiveFeedback="
            + positiveFeedback;
    HttpEntity<List<String>> entity = new HttpEntity<>(items);
    log.info("http entity: " + entity.getBody());
    ResponseEntity<Void> result =
        restTemplate.exchange(urlString, HttpMethod.POST, entity, Void.class);
    if (result.getStatusCode() != HttpStatus.OK) {
      throw new InvalidRequestException("Error sending info to recommendations API.");
    }
    log.info("storeInfo success");
  }

  /**
   * Retrieves recommendations for a user based on their own list history.
   *
   * @param stemmedListName Stemmed name of the list we are providing recommendations for.
   * @return List of pairs containing all items and their corresponding user preference frequency as
   *     a double value
   */
  public List<Pair<String, Double>> getPastRecommendations(String stemmedListName)
      throws URISyntaxException {
    return callRecommendationsAPI("pastUserRecs", userID, stemmedListName);
  }

  /**
   * Retrieves recommendations for a user based on other similar user history.
   *
   * @param stemmedListName Stemmed name of the list we are providing recommendations for.
   * @return List of pairs containing all items and their corresponding user preference frequency as
   *     a double value
   */
  public List<Pair<String, Double>> getUserRecommendations(String stemmedListName)
      throws URISyntaxException {
    return callRecommendationsAPI("generalUserRecs", userID, stemmedListName);
  }

  /**
   * Calls recommendations API to get any possible list item recommendations for the user. Throws
   * URISyntaxException if there is an error in URI creation. Otherwise, if no item suggestions
   * exist, returns an empty list.
   *
   * @param methodName String name of the type of recommendation requested (pastUser or generalUser)
   * @param userID String containing current user's unique ID
   * @param stemmedListName Stemmed name of the list we are providing recommendations for.
   * @return List of pairs containing all items and their corresponding user preference frequency as
   *     a double value
   */
  private List<Pair<String, Double>> callRecommendationsAPI(
      String methodName, String userID, String stemmedListName) throws URISyntaxException {
    log.info("making pastUserRecs api request");
    RestTemplate restTemplate = new RestTemplate();
    String urlString =
        BASE_URL + methodName + "?userID=" + userID + "&stemmedListName=" + stemmedListName;
    URI uri = new URI(urlString);
    ResponseEntity<List> result = restTemplate.getForEntity(uri, List.class);
    if (result.getStatusCode() != HttpStatus.OK) {
      throw new InvalidRequestException("Error sending info to recommendations API.");
    }
    log.info("pastUserRecs success");
    List<LinkedHashMap<String, Double>> resultList = result.getBody();
    Gson gson = new Gson();
    List<Pair<String, Double>> formattedList =
        resultList.stream().map(e -> makePair(e)).collect(Collectors.toList());
    return formattedList;
  }

  /** Helper method for converting LinkedHashMap into a Pair object */
  private Pair<String, Double> makePair(LinkedHashMap obj) {
    String key = (String) obj.get("key");
    double value = (double) obj.get("value");
    return new Pair<>(key, value);
  }
}
