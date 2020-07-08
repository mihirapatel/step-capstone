package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.sps.data.Output;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MapsFindTest {

  @Test
  public void testFindCity() throws Exception {

    TestHelper tester =
        new TestHelper(
            "find some restaurants in san jose",
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"San Jose\"},"
                + "\"place-attraction\": \"restaurants\","
                + "\"number\": \"-1\"}",
            "maps.find");

    Output output = tester.getOutput();

    assertEquals(
        "Here are the top results for restaurants in San Jose, CA, USA.",
        output.getFulfillmentText());
    assertEquals(
        "{\"attractionQuery\":\"restaurants\",\"limit\":-1,\"lng\":-121.8863286,\"lat\":37.3382082}",
        output.getDisplay());
  }

  @Test
  public void testFindCountry() throws Exception {

    TestHelper tester =
        new TestHelper(
            "find some restaurants in Italy",
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"Italy\"},"
                + "\"place-attraction\": \"restaurants\","
                + "\"number\": \"-1\"}",
            "maps.find");

    Output output = tester.getOutput();

    assertEquals("Here are the top results for restaurants in Italy.", output.getFulfillmentText());
    assertEquals(
        "{\"attractionQuery\":\"restaurants\",\"limit\":-1,\"lng\":12.56738,\"lat\":41.87194}",
        output.getDisplay());
  }

  @Test
  public void testFindLimit() throws Exception {

    TestHelper tester =
        new TestHelper(
            "find 5 restaurants in Italy",
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"Italy\"},"
                + "\"place-attraction\": \"restaurants\","
                + "\"number\": 5}",
            "maps.find");

    Output output = tester.getOutput();

    assertEquals(
        "Here are the top 5 results for restaurants in Italy.", output.getFulfillmentText());
    assertEquals(
        "{\"attractionQuery\":\"restaurants\",\"limit\":5,\"lng\":12.56738,\"lat\":41.87194}",
        output.getDisplay());
  }

  @Test
  public void testFindInvalidLimit() throws Exception {

    TestHelper tester =
        new TestHelper(
            "find 5 restaurants in Italy",
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"Italy\"},"
                + "\"place-attraction\": \"restaurants\","
                + "\"number\": 0}",
            "maps.find");

    Output output = tester.getOutput();

    assertEquals(
        "Invalid input for number of restaurants. Please try again with a positive integer between 1 and 20.",
        output.getFulfillmentText());
    assertNull(output.getDisplay());
  }

  @Test
  public void testFindInvalidUpperLimit() throws Exception {

    TestHelper tester =
        new TestHelper(
            "find 5 restaurants in Italy",
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"Italy\"},"
                + "\"place-attraction\": \"restaurants\","
                + "\"number\": 21}",
            "maps.find");

    Output output = tester.getOutput();

    assertEquals(
        "Invalid input for number of restaurants. Please try again with a positive integer between 1 and 20.",
        output.getFulfillmentText());
    assertNull(output.getDisplay());
  }
}
