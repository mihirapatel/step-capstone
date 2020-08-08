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
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationsController {

  private static Logger log = LoggerFactory.getLogger(RecommendationsController.class);
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * POST method that stores new user info regarding their list and items into database.
   *
   * @param userID String containing current user's unique ID
   * @param stemmedListName Stemmed name of the list that user is storing info into.
   * @param newList "true" or "false" based on whether this is the beginning of a new list.
   * @param userFeedback "true" or "false" indicating whether items are being added or rejected.
   * @param items List of strings containing items to add to list.
   */
  @RequestMapping(value = "/storeInfo", method = RequestMethod.POST, consumes = "application/json")
  @ResponseBody
  public ResponseEntity storeInfo(
      @RequestParam(value = "userID") String userID,
      @RequestParam(value = "stemmedListName") String stemmedListName,
      @RequestParam(value = "newList") String newList,
      @RequestParam(value = "positiveFeedback") String userFeedback,
      @RequestBody List<String> items) {
    log.info("storing user info");
    boolean positiveFeedback = userFeedback.equals("true");
    if (positiveFeedback) {
      DatabaseUtils.storeUserListInformation(
          datastore, userID, stemmedListName, items, newList.equals("true"));
    } else {
      long listCount;
      try {
        Entity e = datastore.get(KeyFactory.createKey(stemmedListName, userID));
        listCount = (long) e.getProperty("count");
      } catch (EntityNotFoundException e) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
      DatabaseUtils.updateFractionalAggregation(
          datastore, userID, stemmedListName, items, listCount, false, positiveFeedback);
    }
    log.info("success");
    return new ResponseEntity(HttpStatus.OK);
  }

  /**
   * GET method that retrieves past user recommendations.
   *
   * @param userID String containing current user's unique ID
   * @param stemmedListName Stemmed name of the list to provide recommendations for
   */
  @GetMapping("/pastUserRecs")
  public List<Pair<String, Double>> pastUserRecs(
      @RequestParam(value = "userID") String userID,
      @RequestParam(value = "stemmedListName") String stemmedListName) {
    try {
      log.info("making past user recs");
      return RecommendationUtils.makePastRecommendations(userID, datastore, stemmedListName);
    } catch (IllegalStateException | EntityNotFoundException e) {
      e.printStackTrace();
      return Collections.EMPTY_LIST;
    }
  }

  /**
   * GET method that retrieves general user recommendations.
   *
   * @param userID String containing current user's unique ID
   * @param stemmedListName Stemmed name of the list to provide recommendations for
   */
  @GetMapping("/generalUserRecs")
  public List<Pair<String, Double>> generalUserRecs(
      @RequestParam(value = "userID") String userID,
      @RequestParam(value = "stemmedListName") String stemmedListName) {
    log.info("making general user recs");
    try {
      return RecommendationUtils.makeUserRecommendations(userID, datastore, stemmedListName);
    } catch (IllegalStateException | EntityNotFoundException e) {
      e.printStackTrace();
      return Collections.EMPTY_LIST;
    }
  }

  /**
   * POST method that resets database to default demo values
   *
   * @param confirm True to confirm database reset
   */
  @GetMapping("/reset")
  public ResponseEntity reset(@RequestParam(value = "confirm") String confirm) {
    log.info("resetting database");
    try {
      if (confirm.equals("true")) {
        DatabaseUtils.resetDatabase(datastore);
      } else {
        throw new IllegalStateException("Confirm should be set to true");
      }
    } catch (IllegalStateException e) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity(HttpStatus.OK);
  }
}
