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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class StemUtils {

  private static Logger log = LoggerFactory.getLogger(StemUtils.class);

  /**
   * Stores the stem-to-word dictionary.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Database entity to retrieve data from
   * @param itemName Name of the item to be stemmed and saved.
   */
  public static void saveStemData(DatastoreService datastore, String userID, String itemName) {
    saveStemData(datastore, "StemDict", userID, itemName);
    saveStemData(datastore, "UniversalStemDict", "1", itemName);
  }

  /**
   * Stores the stem-to-word dictionary for general cases.
   *
   * @param keyname Name of the query key
   * @param keyID String ID of the query key
   * @param datastore Database entity to retrieve data from
   * @param itemName Name of the item to be stemmed and saved.
   */
  private static void saveStemData(
      DatastoreService datastore, String keyname, String keyID, String itemName) {
    Entity entity;
    try {
      entity = datastore.get(KeyFactory.createKey(keyname, keyID));
    } catch (EntityNotFoundException e) {
      entity = new Entity(keyname, keyID);
    }
    entity.setProperty(stemmed(itemName), itemName);
    datastore.put(entity);
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
    return snowballStemmer.getCurrent().toLowerCase().replaceAll("\\s+", "");
  }

  /**
   * Gets the unstemmed version of a word based off of a given user's stem mapping. If the word does
   * not exist in the given user's stem history, takes the unstemmed version from the universal stem
   * dictionary. Throws an error if stem-to-unstem mapping does not exist anywhere.
   *
   * @param userID String containing current user's unique ID
   * @param datastore Database entity to retrieve data from
   * @param stemmedWord Stemmed version of a word to be unstemmed.
   * @return unstemmed version of the input word as given by the specified user
   */
  public static String unstem(String userID, DatastoreService datastore, String stemmedWord)
      throws EntityNotFoundException, IllegalStateException {
    String userUnstemmed = tryUnstem(datastore, "StemDict", userID, stemmedWord);
    if (userUnstemmed == null) {
      String universalUnstemmed = tryUnstem(datastore, "UniversalStemDict", "1", stemmedWord);
      if (universalUnstemmed == null) {
        throw new IllegalStateException("Stem mapping does not exist for stemmed word.");
      }
      return universalUnstemmed;
    }
    return userUnstemmed;
  }

  /**
   * Attempts to find stem-to-unstem mapping for a stemmed word. Returns null if not found.
   *
   * @param datastore Database instance
   * @param keyname Name of the query key
   * @param keyID ID of the query
   * @param stemmedWord Word to be unstemmed
   * @return unstemmed word or null if no mapping exists for the stemmed word
   */
  private static String tryUnstem(
      DatastoreService datastore, String keyname, String keyID, String stemmedWord)
      throws EntityNotFoundException {
    Entity entity = datastore.get(KeyFactory.createKey(keyname, keyID));
    return (String) entity.getProperty(stemmedWord);
  }

  /**
   * Stems each string entry in a list of strings.
   *
   * @param items List of strings containing items to add to list
   * @return List of stemmed strings
   */
  public static List<String> stemmedList(List<String> items) {
    return items.stream().map(e -> StemUtils.stemmed(e)).collect(Collectors.toList());
  }
}
