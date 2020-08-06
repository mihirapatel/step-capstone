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

package com.google.sps.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class StemUtils {

  private static Logger log = LoggerFactory.getLogger(StemUtils.class);

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
   * Stems each string entry in a list of strings.
   *
   * @param items List of strings containing items to add to list
   * @return List of stemmed strings
   */
  public static List<String> stemmedList(List<String> items) {
    return items.stream().map(e -> StemUtils.stemmed(e)).collect(Collectors.toList());
  }
}
