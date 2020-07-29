package com.google.sps.recommendations;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationsController {

  private static Logger log = LoggerFactory.getLogger(RecommendationsController.class);
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * Post method that stores new user info regarding their list and items into database.
   *
   * @param userID ID used to identify the user
   * @param stemmedListName Stemmed name of the list that user is storing info into.
   * @param newList "true" or "false" based on whether this is the beginning of a new list.
   * @param items List of strings representing new items added to the list.
   */
  @RequestMapping(value = "/storeInfo", method = RequestMethod.POST, consumes = "application/json")
  @ResponseBody
  @ResponseStatus(value = HttpStatus.OK)
  public void storeInfo(
      @RequestParam(value = "userID") String userID,
      @RequestParam(value = "stemmedListName") String stemmedListName,
      @RequestParam(value = "newList") String newList,
      @RequestBody List<String> items) {
    log.info("storing user info");
    DatabaseUtils.storeUserListInformation(
        datastore, userID, stemmedListName, items, newList.equals("true"));
    log.info("success");
  }

  @GetMapping("/pastUserRecs")
  public List<Pair<String, Double>> pastUserRecs(
      @RequestParam(value = "userID") String userID,
      @RequestParam(value = "stemmedListName") String stemmedListName) {
    try {
      log.info("making past user recs");
      return RecommendationUtils.makePastRecommendations(userID, datastore, stemmedListName);
    } catch (IllegalStateException | EntityNotFoundException e) {
      return Collections.EMPTY_LIST;
    }
  }

  @GetMapping("/generalUserRecs")
  public List<Pair<String, Double>> generalUserRecs(
      @RequestParam(value = "userID") String userID,
      @RequestParam(value = "stemmedListName") String stemmedListName) {
    log.info("making general user recs");
    try {
      return RecommendationUtils.makeUserRecommendations(userID, datastore, stemmedListName);
    } catch (IllegalStateException | EntityNotFoundException e) {
      return Collections.EMPTY_LIST;
    }
  }
}
