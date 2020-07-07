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
public class MapsSearchTest {

  private static Logger log = LoggerFactory.getLogger(MapsSearchTest.class);

  @Test
  public void testMapsSearchCountry() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Where is India located?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"India\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Here is the map for: India");
    assertEquals(output.getDisplay(), "{\"limit\":-1,\"lng\":78.96288,\"lat\":20.593684}");
  }

  @Test
  public void testMapsSearchZipCode() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Locate SW1A 1AA on map",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"SW1A 1AA\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Here is the map for: London SW1A 1AA, UK");
    assertEquals(output.getDisplay(), "{\"limit\":-1,\"lng\":-0.1445783,\"lat\":51.502436}");
  }

  @Test
  public void testMapsSearchIsland() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Map of Ko Tao",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"Ko Tao\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(
        output.getFulfillmentText(),
        "Here is the map for: Ko Tao, Ko Pha-ngan District, Surat Thani, Thailand");
    assertEquals(
        output.getDisplay(), "{\"limit\":-1,\"lng\":99.84039589999999,\"lat\":10.0956102}");
  }

  @Test
  public void testMapsSearchBusinessName() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Where is Mount Whitney?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"Mount Whitney\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Here is the map for: Mt. Whitney, California, USA");
    assertEquals(
        output.getDisplay(), "{\"limit\":-1,\"lng\":-118.29226,\"lat\":36.57849909999999}");
  }

  @Test
  public void testMapsSearchCounty() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Where is Kings County, NY?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"Brooklyn\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"New York\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Here is the map for: Brooklyn, NY, USA");
    assertEquals(output.getDisplay(), "{\"limit\":-1,\"lng\":-73.9441579,\"lat\":40.6781784}");
  }

  @Test
  public void testMapsSearchState() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Show me a map of California",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"California\","
                + "\"street-address\": \"\","
                + "\"city\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Here is the map for: California, USA");
    assertEquals(output.getDisplay(), "{\"limit\":-1,\"lng\":-119.4179324,\"lat\":36.778261}");
  }

  @Test
  public void testMapsSearchStreetAddress() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Show me 2930 Pearl St, Boulder, CO 80301",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"80301\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"Colorado\","
                + "\"street-address\": \"2930 Pearl St\","
                + "\"city\": \"Boulder\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(
        output.getFulfillmentText(),
        "Here is the map for: Google Bldg 2930, 2930 Pearl St, Boulder, CO 80301, USA");
    assertEquals(output.getDisplay(), "{\"limit\":-1,\"lng\":-105.2545612,\"lat\":40.0216013}");
  }

  @Test
  public void testMapsSearchStreetAddress2() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Where is 500 W 2nd St?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"500 W 2nd St\","
                + "\"city\": \"\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(
        output.getFulfillmentText(), "Here is the map for: 500 W 2nd St, Austin, TX 78701, USA");
    assertEquals(output.getDisplay(), "{\"limit\":-1,\"lng\":-97.7495642,\"lat\":30.2660754}");
  }

  @Test
  public void testMapsSearchCity() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "I want to see the map of Cairo.",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"location\": {"
                + "\"country\": \"\","
                + "\"zip-code\": \"\","
                + "\"island\": \"\","
                + "\"shortcut\": \"\","
                + "\"business-name\": \"\","
                + "\"subadmin-area\": \"\","
                + "\"admin-area\": \"\","
                + "\"street-address\": \"\","
                + "\"city\": \"Cairo\"}}",
            // Intent that you expect dialogflow to return based on your query
            "maps.search");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(
        output.getFulfillmentText(), "Here is the map for: Cairo, Cairo Governorate, Egypt");
    assertEquals(output.getDisplay(), "{\"limit\":-1,\"lng\":31.2357116,\"lat\":30.0444196}");
  }
}