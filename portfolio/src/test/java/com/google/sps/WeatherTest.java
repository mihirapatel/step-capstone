/*
 * Copyright 2019 Google LLC
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

package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.sps.data.Output;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This class runs JUnit tests to test Weather intent outputs, given a mock Dialogflow response with
 * defined intents, fulfillment and user input phrases.
 */
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
