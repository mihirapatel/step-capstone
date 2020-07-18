package com.google.sps.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class StemUtils {

  private static Logger log = LoggerFactory.getLogger(StemUtils.class);

  /**
   * Stores the stem-to-word dictionary for each user.
   *
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param itemName Name of the item to be stemmed and saved.
   */
  public static void saveStemData(DatastoreService datastore, String userID, String itemName) {
    Entity entity;
    try {
      entity = datastore.get(KeyFactory.createKey("StemDict", userID));
    } catch (EntityNotFoundException e) {
      entity = new Entity("StemDict", userID);
    }
    entity.setProperty(stemmed(itemName), itemName);
    datastore.put(entity);
    log.info("STEM DICTIONARY: " + entity);
  }

  /**
   * Reduces words to their stems for word correlation.
   *
   * @param word Word to be reduced
   * @return The stem of the inputted word.
   */
  public static String stemmed(String word) {
    SnowballStemmer snowballStemmer = new englishStemmer();
    snowballStemmer.setCurrent(word);
    snowballStemmer.stem();
    return snowballStemmer.getCurrent();
  }

  /**
   * Gets the unstemmed version of a word based off of a given user's stem mapping
   *
   * @param userID The logged-in user's ID
   * @param datastore Database entity to retrieve data from
   * @param stemmedWord Stemmed version of a word to be unstemmed.
   * @return unstemmed version of the input word as given by the specified user
   */
  public static String unstem(String userID, DatastoreService datastore, String stemmedWord)
      throws EntityNotFoundException {
    Entity entity = datastore.get(KeyFactory.createKey("StemDict", userID));
    return (String) entity.getProperty(stemmedWord);
  }
}
