package com.google.sps.servlets;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/** Tests for creating valid Location objects */
@RunWith(JUnit4.class)
public final class SnowballTest {

  @Test
  public void testBerries() throws Exception {
    SnowballStemmer snowballStemmer = new englishStemmer();
    snowballStemmer.setCurrent("berry");
    snowballStemmer.stem();
    String result = snowballStemmer.getCurrent();
    Assert.assertEquals("berri", result);

    snowballStemmer.setCurrent("berries");
    snowballStemmer.stem();
    result = snowballStemmer.getCurrent();
    Assert.assertEquals("berri", result);

    snowballStemmer.setCurrent("raspberry");
    snowballStemmer.stem();
    result = snowballStemmer.getCurrent();
    Assert.assertEquals("raspberri", result);
  }

  @Test
  public void testGroceries() throws Exception {
    SnowballStemmer snowballStemmer = new englishStemmer();
    snowballStemmer.setCurrent("groceries");
    snowballStemmer.stem();
    String result = snowballStemmer.getCurrent();

    snowballStemmer.setCurrent("grocery");
    snowballStemmer.stem();
    String result2 = snowballStemmer.getCurrent();
    Assert.assertEquals(result, result2);

    snowballStemmer.setCurrent("Grocery");
    snowballStemmer.stem();
    String result3 = snowballStemmer.getCurrent();
    Assert.assertEquals(result2, result3.toLowerCase());
  }
}
