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
public class TipTest {

  private static Logger log = LoggerFactory.getLogger(TipTest.class);

  @Test
  public void testTipConversion() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Calculate 15 percent tip for $67.87 split with 3 people",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"tip-percentage\": \"15%\", \"amount-without-tip\": 67.87, \"currency\": \"USD\", \"people-number\": 3.0}",
            // Intent that you expect dialogflow to return based on your query
            "calculator.tips");

    Output output = tester.getOutput();

    // Assertions
    String expected = "The total tip is $10.18, coming out to $3.39 per person";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTipConversionWithoutPeople() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "20 percent tip for $159.43",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"tip-percentage\": \"20%\", \"amount-without-tip\": 159.43, \"currency\": \"USD\", \"people-number\": 0.0}",
            // Intent that you expect dialogflow to return based on your query
            "calculator.tips");

    Output output = tester.getOutput();

    // Assertions
    String expected = "The total tip is $31.89";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTipConversionWithoutTipPercentageMock() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Calculate tip on 23.00 Euros",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"tip-percentage\": \"\", \"amount-without-tip\": 23.00, \"currency\": \"EUR\", \"people-number\": 0.0}",
            // Intent that you expect dialogflow to return based on your query
            "calculator.tips");

    Output output = tester.getOutput();

    // Assertions
    String expected = "I'm sorry, I didn't catch that. Can you repeat that?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTipConversionWithoutTipPercentageReal() throws Exception {

    TestHelper tester = new TestHelper("Calculate tip on 65.00 dollars");
    Output output = tester.getOutput();

    // Assertions
    String expected = "What percentage tip?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTipConversionWithoutAllParamsMock() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Calculate tip",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"tip-percentage\": \"\", \"amount-without-tip\": 0.0, \"currency\": \"\", \"people-number\": 0.0}",
            // Intent that you expect dialogflow to return based on your query
            "calculator.tips",
            false);

    Output output = tester.getOutput();

    // Assertions
    String expected = "I'm sorry, I didn't catch that. Can you repeat that?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }

  @Test
  public void testTipConversionWithoutAllParamsReal() throws Exception {

    TestHelper tester = new TestHelper("Calculate tip");
    Output output = tester.getOutput();

    // Assertions
    String expected = "What percentage tip?";
    String actual = output.getFulfillmentText();
    assertEquals(expected, actual);
  }
}
