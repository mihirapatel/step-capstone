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
   * @param items List of strings to be stemmed
   * @return List of stemmed strings
   */
  public static List<String> stemmedList(List<String> items) {
    return items.stream().map(e -> StemUtils.stemmed(e)).collect(Collectors.toList());
  }
}
