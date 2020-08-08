/*
 * Copyright 2019 Google Inc.
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

package com.google.sps.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.*;
import com.google.sps.data.Output;
import com.google.sps.servlets.BookAgentServlet;
import com.google.sps.servlets.TestHelper;
import com.google.sps.utils.TimeUtils;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoryTest {

  private static Logger log = LoggerFactory.getLogger(MemoryTest.class);
  private TestHelper tester;
  ArrayList<String> commentList;

  /**
   * Test setup which prepopulates the database with a bunch of default values to test database
   * retrieval for different keywords and time durations.
   */
  @Before
  public void setUp() throws ParseException, URISyntaxException {
    // Pre-populate database with some comments.
    commentList =
        new ArrayList<>(
            Arrays.asList(
                "hello",
                "Hello!",
                "tell me a joke",
                "A bus is a vehicle that runs twice as fast when you are after it as when you are in it.",
                "search conversation history for the word apple",
                "Sorry, unable to find any results including the keyword \"apple.\"",
                "Here is a sentence with two words in the same SENTENCE",
                "hello",
                "test1",
                "test2",
                "test3",
                "test4",
                "test5",
                "test6",
                "test7",
                "camel"));
    tester = new TestHelper();
    tester.setCustomDatabase(commentList, 0);

    ArrayList<String> addOns =
        new ArrayList<>(Arrays.asList("febDate1", "febDate2", "camel2", "febDate3"));
    commentList.addAll(addOns);
    tester.setCustomDatabase(addOns, TimeUtils.stringToDate("2014-02-11T09:30:00-08:00").getTime());

    addOns = new ArrayList<>(Arrays.asList("febDate4", "febDate5", "febDate6"));
    commentList.addAll(addOns);
    tester.setCustomDatabase(addOns, TimeUtils.stringToDate("2014-02-11T09:34:00-08:00").getTime());

    addOns = new ArrayList<>(Arrays.asList("febDate7", "febDate8", "febDate9"));
    commentList.addAll(addOns);
    tester.setCustomDatabase(addOns, TimeUtils.stringToDate("2014-02-11T09:36:00-08:00").getTime());

    addOns = new ArrayList<>(Arrays.asList("febDate7", "febDate8", "camel3", "febDate9"));
    commentList.addAll(addOns);
    tester.setCustomDatabase(addOns, TimeUtils.stringToDate("2014-02-12T12:00:00-08:00").getTime());
  }

  /** Checks that no display output or database query is made if the user is not logged in. */
  @Test
  public void testNotLoggedIn() throws Exception {
    tester.setParameters(
        "Search conversation history for the word hello.",
        "{\"keyword\":\"hello\"}",
        "memory.keyword");
    tester.setLoggedOut();

    Output output = tester.getOutput();

    assertEquals("Please login to access user history.", output.getFulfillmentText());
    assertNull(output.getDisplay());

    tester.setLoggedIn();
  }

  /**
   * Tests a search for the keyword hello: - should identify the first hello at index 0 and
   * surrounding conversation of comments at indices 0 - 6 - verifies that surrounding conversation
   * feature works if there are no comments before identified comment - should identify the second
   * hello at index 1 and surrounding conversation at indices 0 - 7 - verifies that program
   * identifies keywords even if capitalization is different - should identiy the third hello at
   * index 7 and surrounding conversation at indices 1 - 14 - verifies that surrounding conversation
   * feature works and grabs 6 comments before and after identified comment
   */
  @Test
  public void testGeneral() throws Exception {

    tester.setParameters(
        "Search conversation history for the word hello.",
        "{\"keyword\":\"hello\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"hello.\"", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(3, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "hello");

    // "hello"
    List<String> neighbors = allComments.get(0);
    assertEquals(7, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i), neighbors.get(i));
    }

    // "Hello!"
    neighbors = allComments.get(1);
    assertEquals(8, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i), neighbors.get(i));
    }

    // "hello" (second)
    neighbors = allComments.get(2);
    assertEquals(13, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 1), neighbors.get(i));
    }
  }

  /**
   * Tests a search for the keyword apple: - should identify the first apple at index 4 - should
   * identify the second apple at index 5 - verifies that program identifies keywords inside
   * quotations (mainly that \" symbol doesn't break anything)
   */
  @Test
  public void testQuoteAroundWord() throws Exception {

    tester.setParameters(
        "Search conversation history for the word apple.",
        "{\"keyword\":\"apple\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"apple.\"", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(2, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "apple");

    // "apple" (first)
    List<String> neighbors = allComments.get(0);
    assertEquals(11, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i), neighbors.get(i));
    }

    // "apple" (second)
    neighbors = allComments.get(1);
    assertEquals(12, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i), neighbors.get(i));
    }
  }

  /**
   * Tests a search for the keyword sentence: - should identify the comment at line 6 - verifies
   * that program identifies only one comment even though keyword appears twice in that comment
   */
  @Test
  public void testWordTwiceInOneSentence() throws Exception {

    tester.setParameters(
        "Search conversation history for the word sentence.",
        "{\"keyword\":\"sentence\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"sentence.\"",
        output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(1, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "sentence");

    // "sentence"
    List<String> neighbors = allComments.get(0);
    assertEquals(13, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i), neighbors.get(i));
    }
  }

  /**
   * Tests a search for the keyword test: - should identify all comments after line 8 - verifies
   * that surrounding conversation feature works for identified comments that have no surrounding
   * comments after it
   */
  @Test
  public void testNoSurroundingCommentAfter() throws Exception {

    tester.setParameters(
        "Search conversation history for the word test.",
        "{\"keyword\":\"test\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"test.\"", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(7, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "test");

    for (int i = 0; i < allComments.size(); i++) {
      List<String> neighbors = allComments.get(i);
      assertEquals(13, neighbors.size());
      for (int j = 0; j < neighbors.size(); j++) {
        assertEquals(commentList.get(j + 2 + i), neighbors.get(j));
      }
    }
  }

  /**
   * Tests a search for the keyword blueberry: - verifies that a keyword that doesn't exist in
   * conversation history return a search not found
   */
  @Test
  public void testNonexistent() throws Exception {

    tester.setParameters(
        "Search conversation history for the word blueberry.",
        "{\"keyword\":\"blueberry\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Sorry, there were no results matching the keyword \"blueberry.\"",
        output.getFulfillmentText());
    assertNull(output.getDisplay());
  }

  @Test
  public void testWordDifferentDay() throws Exception {
    tester.setParameters(
        "Search conversation history for the word camel.",
        "{\"keyword\":\"camel\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"camel.\"", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(3, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "camel");

    // "camel"
    List<String> neighbors = allComments.get(0);
    assertEquals(13, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 9), neighbors.get(i));
    }

    // "camel2"
    neighbors = allComments.get(1);
    assertEquals(13, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 12), neighbors.get(i));
    }

    // "camel3"
    neighbors = allComments.get(2);
    assertEquals(8, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 22), neighbors.get(i));
    }
  }

  @Test
  public void testDayDuration() throws Exception {
    tester.setParameters(
        "Show me conversation history from February 11 2014.",
        "{\"date-time-enhanced\": {\"date-time\": \"2014-02-11T12:00:00-08:00\"},"
            + "\"date-time-original\": February 11 2014}",
        "memory.time");

    Output output = tester.getOutput();
    assertEquals("Here are all the results from February 11 2014.", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(10, identifiedCommentList.size());

    List<String> allComments =
        identifiedCommentList.stream()
            .map((surroundingCommentEntity) -> getComment(surroundingCommentEntity))
            .collect(Collectors.toList());

    for (int i = 0; i < allComments.size(); i++) {
      assertEquals(commentList.get(i + 16), allComments.get(i));
    }
  }

  @Test
  public void testDayStart928() throws Exception {
    tester.setParameters(
        "Show me conversation history from February 11 2014 at 9:28 AM.",
        "{\"date-time-enhanced\": {\"date-time\": {\"date-time\": \"2014-02-11T9:28:00-08:00\"}},"
            + "\"date-time-original\": \"February 11 2014 at 9:28 AM\"}",
        "memory.time");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 11 2014 at 9:28 AM.", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(4, identifiedCommentList.size());

    List<String> allComments =
        identifiedCommentList.stream()
            .map((surroundingCommentEntity) -> getComment(surroundingCommentEntity))
            .collect(Collectors.toList());

    for (int i = 0; i < allComments.size(); i++) {
      assertEquals(commentList.get(i + 16), allComments.get(i));
    }
  }

  @Test
  public void testDayStart931() throws Exception {
    tester.setParameters(
        "Show me conversation history from February 11 2014 at 9:31 AM.",
        "{\"date-time-enhanced\": {\"date-time\": {\"date-time\": \"2014-02-11T9:31:00-08:00\"}},"
            + "\"date-time-original\": \"February 11 2014 at 9:31 AM\"}",
        "memory.time");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 11 2014 at 9:31 AM.", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(
        8,
        identifiedCommentList.size()); // 8 bc grabs the first one from 9:34 but not 1 ms after that

    List<String> allComments =
        identifiedCommentList.stream()
            .map((surroundingCommentEntity) -> getComment(surroundingCommentEntity))
            .collect(Collectors.toList());

    for (int i = 0; i < allComments.size(); i++) {
      assertEquals(commentList.get(i + 16), allComments.get(i));
    }
  }

  @Test
  public void testDayStart934() throws Exception {
    tester.setParameters(
        "Show me conversation history from February 11 2014 at 9:34 AM.",
        "{\"date-time-enhanced\": {\"date-time\": {\"date-time\": \"2014-02-11T9:34:00-08:00\"}},"
            + "\"date-time-original\": \"February 11 2014 at 9:34 AM\"}",
        "memory.time");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 11 2014 at 9:34 AM.", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(10, identifiedCommentList.size());

    List<String> allComments =
        identifiedCommentList.stream()
            .map((surroundingCommentEntity) -> getComment(surroundingCommentEntity))
            .collect(Collectors.toList());

    for (int i = 0; i < allComments.size(); i++) {
      assertEquals(commentList.get(i + 16), allComments.get(i));
    }
  }

  @Test
  public void testDayStart937() throws Exception {
    tester.setParameters(
        "Show me conversation history from February 11 2014 at 9:34 AM.",
        "{\"date-time-enhanced\": {\"date-time\": {\"date-time\": \"2014-02-11T9:34:00-08:00\"}},"
            + "\"date-time-original\": \"February 11 2014 at 9:34 AM\"}",
        "memory.time");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 11 2014 at 9:34 AM.", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(10, identifiedCommentList.size());

    List<String> allComments =
        identifiedCommentList.stream()
            .map((surroundingCommentEntity) -> getComment(surroundingCommentEntity))
            .collect(Collectors.toList());

    for (int i = 0; i < allComments.size(); i++) {
      assertEquals(commentList.get(i + 16), allComments.get(i));
    }
  }

  @Test
  public void testDayNoInterval() throws Exception {
    tester.setParameters(
        "Show me conversation history from February 11 2014 at 10:00 AM.",
        "{\"date-time-enhanced\": {\"date-time\": {\"date-time\": \"2014-02-11T10:00:00-08:00\"}},"
            + "\"date-time-original\": \"February 11 2014 at 10:00 AM\"}",
        "memory.time");

    Output output = tester.getOutput();
    assertEquals(
        "Could not find any conversation from February 11 2014 at 10:00 AM.",
        output.getFulfillmentText());
    assertNull(output.getDisplay());
  }

  @Test
  public void testMonth() throws Exception {
    tester.setParameters(
        "Show me conversation history from February 2014.",
        "{\"date-time-enhanced\": {\"date-time\": { \"startDate\": \"2014-02-01T00:00:00-08:00\", \"endDate\": \"2014-02-28T23:59:59-08:00\" }},"
            + "\"date-time-original\": \"February 2014\"}",
        "memory.time");

    Output output = tester.getOutput();
    assertEquals("Here are all the results from February 2014.", output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(14, identifiedCommentList.size());

    List<String> allComments =
        identifiedCommentList.stream()
            .map((surroundingCommentEntity) -> getComment(surroundingCommentEntity))
            .collect(Collectors.toList());

    for (int i = 0; i < allComments.size(); i++) {
      assertEquals(commentList.get(i + 16), allComments.get(i));
    }
  }

  @Test
  public void testCamelFebruaryMonth() throws Exception {
    tester.setParameters(
        "Show me all mentions of camel from February 2014.",
        "{\"keyword\": \"camel\","
            + "\"date-time-enhanced\": {\"date-time\": { \"startDate\": \"2014-02-01T00:00:00-08:00\", \"endDate\": \"2014-02-28T23:59:59-08:00\" }},"
            + "\"date-time-original\": \"February 2014\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 2014 including the keyword \"camel.\"",
        output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(2, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "camel");

    // "camel2"
    List<String> neighbors = allComments.get(0);
    assertEquals(9, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 16), neighbors.get(i));
    }

    // "camel3"
    neighbors = allComments.get(1);
    assertEquals(8, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 22), neighbors.get(i));
    }
  }

  @Test
  public void testCamelFebruary11() throws Exception {
    tester.setParameters(
        "Show me all mentions of camel from February 11, 2014.",
        "{\"keyword\": \"camel\","
            + "\"date-time-enhanced\": {\"date-time\": \"2014-02-11T12:00:00-08:00\"},"
            + "\"date-time-original\": \"February 11, 2014\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 11, 2014 including the keyword \"camel.\"",
        output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(1, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "camel");

    // "camel2"
    List<String> neighbors = allComments.get(0);
    assertEquals(9, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 16), neighbors.get(i));
    }
  }

  @Test
  public void testCamelFebruary11Time() throws Exception {
    tester.setParameters(
        "Show me all mentions of camel from February 11, 2014 at 9:30 AM.",
        "{\"keyword\": \"camel\","
            + "\"date-time-enhanced\": {\"date-time\": {\"date-time\": \"2014-02-11T9:30:00-08:00\"}},"
            + "\"date-time-original\": \"February 11, 2014 at 9:30 AM\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 11, 2014 at 9:30 AM including the keyword \"camel.\"",
        output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(1, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "camel");

    // "camel2"
    List<String> neighbors = allComments.get(0);
    assertEquals(7, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 16), neighbors.get(i));
    }
  }

  @Test
  public void testCamelFebruary11Time2() throws Exception {
    tester.setParameters(
        "Show me all mentions of camel from February 11, 2014 at 9:34 AM.",
        "{\"keyword\": \"camel\","
            + "\"date-time-enhanced\": {\"date-time\": {\"date-time\": \"2014-02-11T9:34:00-08:00\"}},"
            + "\"date-time-original\": \"February 11, 2014 at 9:34 AM\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results from February 11, 2014 at 9:34 AM including the keyword \"camel.\"",
        output.getFulfillmentText());

    Map<String, Value> result = BookAgentServlet.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationPairList").getListValue().getValuesList();
    assertEquals(1, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "camel");

    // "camel2"
    List<String> neighbors = allComments.get(0);
    assertEquals(9, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i + 16), neighbors.get(i));
    }
  }

  private List<List<String>> unpackAllComments(List<Value> identifiedCommentList, String word) {
    List<List<String>> allComments = new ArrayList<>();

    for (Value listObject : identifiedCommentList) {
      Map<String, Value> commentGroup = listObject.getStructValue().getFieldsMap();
      String identifiedComment = getComment(commentGroup.get("key"));
      assertTrue(identifiedComment.toLowerCase().contains(word.toLowerCase()));

      List<Value> surroundingComments = commentGroup.get("value").getListValue().getValuesList();
      List<String> comments =
          surroundingComments.stream()
              .map((surroundingCommentEntity) -> getComment(surroundingCommentEntity))
              .collect(Collectors.toList());
      allComments.add(comments);
    }
    return allComments;
  }

  private String getComment(Value commentEntity) {
    return commentEntity
        .getStructValue()
        .getFieldsMap()
        .get("propertyMap")
        .getStructValue()
        .getFieldsMap()
        .get("comment")
        .getStringValue();
  }
}
