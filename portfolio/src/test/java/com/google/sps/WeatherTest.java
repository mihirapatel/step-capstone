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

@RunWith(MockitoJUnitRunner.Silent.class)
public class WeatherTest {
  @Test
  public void getWeatherRequest() throws Exception {
    TestHelper tester =
        new TestHelper(
            // User input text
            "What is the weather like in Los Angeles?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"address\": {\"admin-area\": \"\","
                + "\"city\": \"Los Angeles\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "weather");
    Output output = tester.getOutput();

    String actualRedirect = output.getRedirect();
    String expectedRedirect = "http://www.google.com/search?q=weather+in+Los+Angeles,+CA,+USA";
    assertEquals(expectedRedirect, actualRedirect);

    String actualFulfillment = output.getFulfillmentText();
    String expectedFulfillment = "Redirecting you to the current forecast in Los Angeles.";
    assertEquals(expectedFulfillment, actualFulfillment);
  }

  @Test
  public void checkFollowUpRequest() throws Exception {
    TestHelper tester =
        new TestHelper(
            "What about new york",
            "{\"address\": {\"admin-area\": \"\","
                + "\"city\": \"New York\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "weather.context:weather - comment:address");
    Output output = tester.getOutput();

    String actualRedirect = output.getRedirect();
    String expectedRedirect = "http://www.google.com/search?q=weather+in+New+York,+NY,+USA";
    assertEquals(expectedRedirect, actualRedirect);

    String actualFulfillment = output.getFulfillmentText();
    String expectedFulfillment = "Redirecting you to the current forecast in New York.";
    assertEquals(expectedFulfillment, actualFulfillment);
  }

  @Test
  public void checkDefaultCity() throws Exception {
    TestHelper tester =
        new TestHelper(
            "What is the weather in brentwood",
            "{\"address\": {\"admin-area\": \"\","
                + "\"city\": \"Brentwood\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "weather");
    Output output = tester.getOutput();

    String actualRedirect = output.getRedirect();
    String expectedRedirect = "http://www.google.com/search?q=weather+in+Brentwood,+CA+94513,+USA";
    assertEquals(expectedRedirect, actualRedirect);

    String actualFulfillment = output.getFulfillmentText();
    String expectedFulfillment = "Redirecting you to the current forecast in Brentwood.";
    assertEquals(expectedFulfillment, actualFulfillment);
  }

  @Test
  public void checkSpecifiedCity() throws Exception {
    TestHelper tester =
        new TestHelper(
            "What is the weather in Brentwood, Texas",
            "{\"address\": {\"admin-area\": \"\","
                + "\"city\": \"Brentwood\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"Texas\"}}",
            "weather");
    Output output = tester.getOutput();

    String actualRedirect = output.getRedirect();
    String expectedRedirect =
        "http://www.google.com/search?q=weather+in+Brentwood,+Austin,+TX,+USA";
    assertEquals(expectedRedirect, actualRedirect);

    String actualFulfillment = output.getFulfillmentText();
    String expectedFulfillment = "Redirecting you to the current forecast in Brentwood, Texas.";
    assertEquals(expectedFulfillment, actualFulfillment);
  }

  @Test
  public void checkNotAllParamsPresent() throws Exception {
    TestHelper tester =
        new TestHelper("What is the weather?", "{\"address\": \"\"}", "weather", false);
    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "I'm sorry, I didn't catch that. Can you repeat that?";
    assertEquals(expected, actual);
  }

  @Test
  public void checkMissingLocation() throws Exception {
    TestHelper tester = new TestHelper("What is the weather?");
    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "Where?";
    assertEquals(expected, actual);
  }
}
