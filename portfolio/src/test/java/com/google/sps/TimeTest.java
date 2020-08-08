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

import com.google.maps.errors.ApiException;
import com.google.sps.data.Location;
import com.google.sps.data.Output;
import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This class runs JUnit tests to test Time intent outputs, given a mock Dialogflow response with
 * defined intents, fulfillment and user input phrases.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TimeTest {
  Location placeOne = null;
  Location placeTwo = null;

  @Before
  public void init() {
    try {
      placeOne = Location.create("Los Angeles");
      placeTwo = Location.create("New York");
    } catch (IllegalStateException
        | IOException
        | ApiException
        | InterruptedException
        | ArrayIndexOutOfBoundsException e) {
      Assert.fail("Should not have thrown any exception");
    }
  }

  @Test
  public void getTimeRequest() throws Exception {
    TestHelper tester =
        new TestHelper(
            // User input text
            "What time is it in Los Angeles?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {\"admin-area\": \"\","
                + "\"city\": \"Los Angeles\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "time.get");

    Output output = tester.getOutput();
    ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(placeOne.getTimeZoneID()));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");

    String timeString = currentTime.format(formatter);
    String actual = output.getFulfillmentText();
    String expected = "It is " + timeString + " in Los Angeles.";
    assertEquals(expected, actual);
  }

  @Test
  public void checkTimeRequest() throws Exception {
    TestHelper tester =
        new TestHelper(
            "Check the time in new york",
            "{\"location\": {\"admin-area\": \"\","
                + "\"city\": \"New York\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"},"
                + "\"time\": \"2020-06-30T16:00:00-07:00\"}",
            "time.check");

    Output output = tester.getOutput();
    ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(placeTwo.getTimeZoneID()));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
    String timeString = currentTime.format(formatter);

    String actual = output.getFulfillmentText();
    String expected = "In New York, it is currently " + timeString + ".";
    assertEquals(expected, actual);
  }

  @Test
  public void checkNotAllParamsPresent() throws Exception {
    TestHelper tester =
        new TestHelper("What time is it?", "{\"location\": \"\"}", "time.get", false);

    Output output = tester.getOutput();
    String actual = output.getFulfillmentText();
    String expected = "I'm sorry, I didn't catch that. Can you repeat that?";
    assertEquals(expected, actual);
  }

  @Test
  public void checkMissingLocation() throws Exception {
    TestHelper tester = new TestHelper("What time is it?");

    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "Where?";
    assertEquals(expected, actual);
  }

  @Test
  public void checkTimeContext() throws Exception {
    TestHelper tester =
        new TestHelper(
            "What time is it in New York when it is 4pm in Los Angeles.",
            "{\"location-from\": {\"admin-area\": \"\","
                + "\"city\": \"Los Angeles\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"},"
                + "\"location-to\": {\"admin-area\": \"\","
                + "\"city\": \"New York\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"},"
                + "\"time-from\": \"2020-06-30T16:00:00-07:00\"}",
            "time.convert");

    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "It's 7:00 PM in New York when it's 4:00 PM in Los Angeles.";
    assertEquals(expected, actual);
  }

  @Test
  public void checkTimeZone() throws Exception {
    TestHelper tester =
        new TestHelper(
            "What time zone is durham",
            "{\"location\": {\"admin-area\": \"\","
                + "\"city\": \"Durham\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "time.time_zones");

    Output output = tester.getOutput();
    String actual = output.getFulfillmentText();
    String expected = "The timezone in Durham is Eastern Standard Time (EDT).";
    assertEquals(expected, actual);
  }

  @Test
  public void checkTimeDifferenceBehind() throws Exception {
    TestHelper tester =
        new TestHelper(
            "tell me the time difference between Los Angeles and Australia",
            "{\"location-1\": {\"admin-area\": \"\","
                + "\"city\": \"Los Angeles\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"},"
                + "\"location-2\": {\"admin-area\": \"\","
                + "\"city\": \"\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"Australia\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "time.time_difference");

    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "Los Angeles is 16 hours and 30 minutes behind Australia.";
    assertEquals(expected, actual);
  }

  @Test
  public void checkTimeDifferenceAhead() throws Exception {
    TestHelper tester =
        new TestHelper(
            "tell me the time difference between New York and Sunnyvale",
            "{\"location-1\": {\"admin-area\": \"\","
                + "\"city\": \"New York\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"},"
                + "\"location-2\": {\"admin-area\": \"\","
                + "\"city\": \"Sunnyvale\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "time.time_difference");

    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "New York is 3 hours ahead of Sunnyvale.";
    assertEquals(expected, actual);
  }

  @Test
  public void checkTimeDifferenceEquals() throws Exception {
    TestHelper tester =
        new TestHelper(
            "tell me the time difference between New York and Durham",
            "{\"location-1\": {\"admin-area\": \"\","
                + "\"city\": \"New York\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"},"
                + "\"location-2\": {\"admin-area\": \"\","
                + "\"city\": \"Durham\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "time.time_difference");

    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "New York is in the same time zone as Durham.";
    assertEquals(expected, actual);
  }
}
