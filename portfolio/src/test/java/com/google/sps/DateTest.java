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
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This class runs JUnit tests to test Date intent outputs, given a mock Dialogflow response with
 * defined intents, fulfillment and user input phrases.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class DateTest {
  Location losAngeles = null;
  Location london = null;

  @Before
  public void init() {
    try {
      losAngeles = Location.create("Los Angeles");
      london = Location.create("London");
    } catch (IllegalStateException
        | IOException
        | ApiException
        | InterruptedException
        | ArrayIndexOutOfBoundsException e) {
      Assert.fail("Should not have thrown any exception");
    }
  }

  @Test
  public void getDateRequest() throws Exception {
    TestHelper tester =
        new TestHelper(
            // User input text
            "What day is it in Los Angeles?",
            // Parameter JSON string (copy paste from Dialogflow)
            "{\"location\": {\"admin-area\": \"\","
                + "\"city\": \"Los Angeles\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "date.get",
            true);

    Output output = tester.getOutput();
    ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(losAngeles.getTimeZoneID()));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d uuuu");
    String dateString = currentTime.format(formatter);

    String actual = output.getFulfillmentText();
    String expected = "It is " + dateString + " in Los Angeles.";
    assertEquals(expected, actual);
  }

  @Test
  public void getDateContextRequest() throws Exception {
    TestHelper tester =
        new TestHelper(
            "What about in london?",
            "{\"location\": {\"admin-area\": \"\","
                + "\"city\": \"London\","
                + "\"street-address\": \"\","
                + "\"island\": \"\","
                + "\"zip-code\": \"\","
                + "\"country\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\"}}",
            "date.context:date",
            true);

    Output output = tester.getOutput();
    ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(london.getTimeZoneID()));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d uuuu");
    String dateString = currentTime.format(formatter);

    String actual = output.getFulfillmentText();
    String expected = "It is " + dateString + " in London.";
    assertEquals(expected, actual);
  }

  @Test
  public void checkNotAllParamsPresent() throws Exception {
    TestHelper tester =
        new TestHelper("What day is it?", "{\"location\": \"\"}", "date.get", false);

    Output output = tester.getOutput();
    String actual = output.getFulfillmentText();
    String expected = "I'm sorry, I didn't catch that. Can you repeat that?";
    assertEquals(expected, actual);
  }

  @Test
  public void checkMissingLocation() throws Exception {
    TestHelper tester = new TestHelper("What day is it?");

    Output output = tester.getOutput();
    String actual = output.getFulfillmentText();
    String expected = "Where?";
    assertEquals(expected, actual);
  }

  @Test
  public void checkDateContextPast() throws Exception {
    TestHelper tester =
        new TestHelper(
            "what day was august 2, 1999",
            "{\"date-time\": \"1999-08-02T12:00:00-07:00\"," + "\"unit-time\": \"day\"}",
            "date.day-of-date",
            true);

    Output output = tester.getOutput();
    String actual = output.getFulfillmentText();
    String expected = "August 2, 1999 was a Monday.";
    assertEquals(expected, actual);
  }

  @Test
  public void checkDateContextFuture() throws Exception {
    TestHelper tester =
        new TestHelper(
            "what day is july 4, 3000",
            "{\"date-time\": \"3000-07-04T12:00:00-07:00\"," + "\"unit-time\": \"day\"}",
            "date.day-of-date",
            true);

    Output output = tester.getOutput();

    String actual = output.getFulfillmentText();
    String expected = "July 4, 3000 is a Friday.";
    assertEquals(expected, actual);
  }
}
