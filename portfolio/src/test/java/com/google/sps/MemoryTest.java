package com.google.sps.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.*;
import com.google.sps.data.Output;
import com.google.sps.servlets.TestHelper;
import java.util.*;
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
  List<String> commentList;

  @Before
  public void setUp() {
    // Pre-populate database with some comments.
    commentList =
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
            "test7");
    tester = new TestHelper();
    tester.setCustomDatabase(commentList);
  }

  @Test
  public void testNotLoggedIn() throws Exception {

    tester.setParameters(
        "Search conversation history for the word hello.",
        "{\"keyword\":\"hello\"}",
        "memory.keyword");
    tester.setLoggedOut();

    Output output = tester.getOutput();

    assertEquals("Please login to access conversation history.", output.getFulfillmentText());
    assertNull(output.getDisplay());

    tester.setLoggedIn();
  }

  @Test
  public void testHello() throws Exception {

    tester.setParameters(
        "Search conversation history for the word hello.",
        "{\"keyword\":\"hello\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"hello.\"", output.getFulfillmentText());

    Map<String, Value> result = TestHelper.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
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

  @Test
  public void testApple() throws Exception {

    tester.setParameters(
        "Search conversation history for the word apple.",
        "{\"keyword\":\"apple\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"apple.\"", output.getFulfillmentText());

    Map<String, Value> result = TestHelper.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
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

  @Test
  public void testSentence() throws Exception {

    tester.setParameters(
        "Search conversation history for the word sentence.",
        "{\"keyword\":\"sentence\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"sentence.\"",
        output.getFulfillmentText());

    Map<String, Value> result = TestHelper.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(1, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "sentence");

    // "sentence"
    List<String> neighbors = allComments.get(0);
    assertEquals(13, neighbors.size());
    for (int i = 0; i < neighbors.size(); i++) {
      assertEquals(commentList.get(i), neighbors.get(i));
    }
  }

  @Test
  public void testTest() throws Exception {

    tester.setParameters(
        "Search conversation history for the word test.",
        "{\"keyword\":\"test\"}",
        "memory.keyword");

    Output output = tester.getOutput();
    assertEquals(
        "Here are all the results including the keyword \"test.\"", output.getFulfillmentText());

    Map<String, Value> result = TestHelper.stringToMap(output.getDisplay());
    List<Value> identifiedCommentList =
        result.get("conversationList").getListValue().getValuesList();
    assertEquals(7, identifiedCommentList.size());

    List<List<String>> allComments = unpackAllComments(identifiedCommentList, "test");

    for (int i = 0; i < allComments.size(); i++) {
      List<String> neighbors = allComments.get(i);
      assertEquals(13 - i, neighbors.size());
      for (int j = 0; j < neighbors.size(); j++) {
        assertEquals(commentList.get(j + 2 + i), neighbors.get(j));
      }
    }
  }

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
