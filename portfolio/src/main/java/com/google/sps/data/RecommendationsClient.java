package com.google.sps.data;

import com.google.appengine.api.log.InvalidRequestException;
import com.google.gson.Gson;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
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

  public void setUserID(String userID) {
    this.userID = userID;
  }

  /**
   * Stores the integer aggregate count of number of times user has placed a given item in a list.
   *
   * @param stemmedListName The stemmed name of the list to store aggregation information for.
   * @param items List of strings containing items that were newly added.
   * @param newList Indicates whether the list is a new list (true) or updating existing (false)
   * @param positiveFeedback Boolean to indicate if the items are liked/added by user (true) or not
   *     liked by user (false)
   */
  public void saveAggregateListData(
      String stemmedListName, List<String> items, boolean newList, boolean positiveFeedback)
      throws InvalidRequestException {
    log.info("making storeInfo api request");
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
   * @param userID String representing the ID of the user giving recommendations for
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

  private Pair<String, Double> makePair(LinkedHashMap e) {
    String key = (String) e.get("key");
    double value = (double) e.get("value");
    return new Pair<>(key, value);
  }
}
