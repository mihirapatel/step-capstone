package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.sps.data.Output;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TranslateTest {

  private static Logger log = LoggerFactory.getLogger(TranslateTest.class);

  @Test
  public void testTranslation() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Translate Spanish word trabajador into English",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"text\": \"trabajador\",\"lang-to\": \"English\", \"lang-from\": \"Spanish\"}",
            // Intent that you expect dialogflow to return based on your query
            "translate.text");

    Output output = tester.getOutput();

    // Assertions
    String expected = "Trabajador in English is: employee";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTranslationWithoutText() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Translate from English to German",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"text\": \"\",\"lang-to\": \"German\", \"lang-from\": \"English\"}",
            // Intent that you expect dialogflow to return based on your query
            "translate.text",
            false);

    Output output = tester.getOutput();

    // Assertions
    String expected = "What do you want to translate?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTranslationWithoutLangFrom() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "How do you say Hello, how are you to Punjabi",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"text\": \"Hello, how are you\",\"lang-to\": \"Punjabi\", \"lang-from\": \"\"}",
            // Intent that you expect dialogflow to return based on your query
            "translate.text",
            false);

    Output output = tester.getOutput();

    // Assertions
    String expected = "From what language?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTranslationWithoutLangTo() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Translate the English word breakfast",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"text\": \"breakfast\",\"lang-to\": \"\", \"lang-from\": \"English\"}",
            // Intent that you expect dialogflow to return based on your query
            "translate.text",
            false);

    Output output = tester.getOutput();

    // Assertions
    String expected = "To what language?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTranslationWithoutLangParams() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Translate jacket",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"text\": \"jacket\",\"lang-to\": \"\", \"lang-from\": \"\"}",
            // Intent that you expect dialogflow to return based on your query
            "translate.text",
            false);

    Output output = tester.getOutput();

    // Assertions
    String expected = "To what language?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }
}
