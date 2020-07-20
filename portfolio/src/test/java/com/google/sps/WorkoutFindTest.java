package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.InvalidProtocolBufferException;
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
public class WorkoutFindTest {

  private static Logger log = LoggerFactory.getLogger(WorkoutFindTest.class);

  // Testing output when user specifies all possible parameters
  @Test
  public void testWorkoutFind() throws InvalidProtocolBufferException, IOException {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Find me 30 minute HIIT workouts from Madfit",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"duration\": {\"unit\": \"min\", \"amount\": 30 }, \"youtube-channel\": \"MadFit\", \"workout-type\": \"HIIT\"}",
            // Intent that you expect dialogflow to return based on your query
            "workout.find");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(
        "Here are videos for: 30 min HIIT workouts from MadFit", output.getFulfillmentText());
  }

  // Testing output when user does not specify youtube-channel
  @Test
  public void testWorkoutFindWithoutChannel() throws InvalidProtocolBufferException, IOException {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Search for some 15 minute ab workouts",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"duration\": {\"unit\": \"min\", \"amount\": 15 }, \"youtube-channel\": \"\", \"workout-type\": \"abs\"}",
            // Intent that you expect dialogflow to return based on your query
            "workout.find");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Here are videos for: 15 min ab workouts ", output.getFulfillmentText());
  }

  // Testing output when user does not specify workout-type
  @Test
  public void testWorkoutFindWithoutWorkoutType()
      throws InvalidProtocolBufferException, IOException {

    TestHelper tester =
        new TestHelper(
            // User input text
            "45 minute workouts from Popsugar Fitness",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"duration\": {\"unit\": \"min\", \"amount\": 45 }, \"youtube-channel\": \"POPSUGAR Fitness\", \"workout-type\": \"\"}",
            // Intent that you expect dialogflow to return based on your query
            "workout.find");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(
        "Here are videos for: 45 min workouts from POPSUGAR Fitness", output.getFulfillmentText());
  }

  // Testing output when user does not specify duration
  @Test
  public void testWorkoutFindWithoutDuration() throws InvalidProtocolBufferException, IOException {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Find me ab workouts from Blogilates",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"duration\": \"\", \"youtube-channel\": \"Blogilates\", \"workout-type\": \"ab\"}",
            // Intent that you expect dialogflow to return based on your query
            "workout.find");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Here are videos for: ab workouts from Blogilates", output.getFulfillmentText());
  }

  // Testing output when user only specifies youtube-channel
  @Test
  public void testWorkoutFindWithChannelOnly() throws InvalidProtocolBufferException, IOException {

    TestHelper tester =
        new TestHelper(
            // User input text
            "I want workouts by Pamela Reif",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"duration\": \"\", \"youtube-channel\": \"Pamela Reif\", \"workout-type\": \"\"}",
            // Intent that you expect dialogflow to return based on your query
            "workout.find");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Here are videos for: workouts from Pamela Reif", output.getFulfillmentText());
  }

  // Testing output when user only specifies workout-type
  @Test
  public void testWorkoutFindWithWorkoutTypeOnly()
      throws InvalidProtocolBufferException, IOException {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Find me tabata workouts",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"duration\": \"\", \"youtube-channel\": \"\", \"workout-type\": \"tabata\"}",
            // Intent that you expect dialogflow to return based on your query
            "workout.find");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Here are videos for: tabata workouts ", output.getFulfillmentText());
  }

  // Testing output when user only specifies duration
  @Test
  public void testWorkoutFindWithDurationOnly() throws InvalidProtocolBufferException, IOException {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Find me 45 minute workouts",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"duration\": {\"unit\": \"min\", \"amount\": 45 }, \"youtube-channel\": \"\", \"workout-type\": \"\"}",
            // Intent that you expect dialogflow to return based on your query
            "workout.find");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Here are videos for: 45 min workouts ", output.getFulfillmentText());
  }
}
