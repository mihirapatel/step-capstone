package com.google.sps.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.sps.data.Output;
import com.google.sps.servlets.TestHelper;
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

  /**
   * Test setup which prepopulates the database with a bunch of default values to test database
   * retrieval for different keywords and time durations.
   */
  @Before
  public void setUp() {
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

  /** Checks that no display output or database query is made if the user is not logged in. */
  @Test
  public void testNotLoggedIn() throws Exception {

    String jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"Tom\","
            + "\"type\": \"\"}";
    tester.setParameters("Change my name to Tom.", jsonParams, "name.change");
    tester.setLoggedOut();

    Output output = tester.getOutput();

    assertEquals("Please login to modify your name.", output.getFulfillmentText());
    assertNull(output.getDisplay());
  }

  @Test
  public void testGeneralName() throws Exception {

    String jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"Tom\","
            + "\"type\": \"\"}";
    tester.setParameters("Change my name to Tom.", jsonParams, "name.change");

    Output output = tester.getOutput();

    assertEquals("Changing your first name to be Tom.", output.getFulfillmentText());
    assertEquals("Tom", output.getDisplay());
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
  public void testFirstName() throws Exception {

    String jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"Tom\","
            + "\"type\": \"first name\"}";
    tester = new TestHelper("Change my first name to Tom.", jsonParams, "name.change");

    Output output = tester.getOutput();

    assertEquals("Changing your first name to be Tom.", output.getFulfillmentText());
    assertEquals("Tom", output.getDisplay());
  }

  /**
   * Tests a search for the keyword apple: - should identify the first apple at index 4 - should
   * identify the second apple at index 5 - verifies that program identifies keywords inside
   * quotations (mainly that \" symbol doesn't break anything)
   */
  @Test
  public void testNickName() throws Exception {

    String jsonParams =
        "{\"nick-name\": \"Tom\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"\","
            + "\"type\": \"nickname\"}";
    tester.setParameters("Change my nickname to Tom.", jsonParams, "name.change");

    Output output = tester.getOutput();

    assertEquals("Changing your nickname to be Tom.", output.getFulfillmentText());
    assertEquals("Tom", output.getDisplay());
  }

  /**
   * Tests a search for the keyword sentence: - should identify the comment at line 6 - verifies
   * that program identifies only one comment even though keyword appears twice in that comment
   */
  @Test
  public void testNickNameGiven() throws Exception {

    String jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"Tom\","
            + "\"type\": \"nickname\"}";
    tester.setParameters("Change my nickname to Tom.", jsonParams, "name.change");

    Output output = tester.getOutput();

    assertEquals("Changing your nickname to be Tom.", output.getFulfillmentText());
    assertEquals("Tom", output.getDisplay());
  }

  /**
   * Tests a search for the keyword test: - should identify all comments after line 8 - verifies
   * that surrounding conversation feature works for identified comments that have no surrounding
   * comments after it
   */
  @Test
  public void testOnlyLastName() throws Exception {

    String jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"Tom\","
            + "\"given-name\": \"\","
            + "\"type\": \"last name\"}";
    tester.setParameters("Change my last name to Tom.", jsonParams, "name.change");

    Output output = tester.getOutput();

    assertEquals("Changing your last name to be Tom.", output.getFulfillmentText());
    assertEquals("test@example.com", output.getDisplay());
  }

  /**
   * Tests a search for the keyword blueberry: - verifies that a keyword that doesn't exist in
   * conversation history return a search not found
   */
  @Test
  public void testDidNotHearName() throws Exception {

    String jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"\","
            + "\"type\": \"nickname\"}";
    tester.setParameters("Change my nickname to Tom.", jsonParams, "name.change");

    Output output = tester.getOutput();

    assertEquals(
        "I'm sorry, I didn't catch the name. Can you repeat that?", output.getFulfillmentText());
    assertNull(output.getDisplay());
  }

  @Test
  public void testConsecutiveChanges() throws Exception {

    // Set name to be Tom -- should output display name as Tom since first name change

    String jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"Tom\","
            + "\"type\": \"\"}";

    tester.setParameters("Change my name to Tom.", jsonParams, "name.change");

    Output output = tester.getOutput();

    assertEquals("Changing your first name to be Tom.", output.getFulfillmentText());
    assertEquals("Tom", output.getDisplay());

    // Set nickname to be NicknameTom -- should output display name as NicknameTom since nickname >
    // name

    jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"NicknameTom\","
            + "\"type\": \"nickname\"}";
    tester.setParameters("Change my nickname to Tom.", jsonParams, "name.change");

    output = tester.getOutput();

    assertEquals("Changing your nickname to be NicknameTom.", output.getFulfillmentText());
    assertEquals("NicknameTom", output.getDisplay());

    // Set name to be NameTom -- should output display name as NicknameTom since nickname exists and
    // nickname > name

    jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"NameTom\","
            + "\"type\": \"first name\"}";
    tester.setParameters("Change my first name to NameTom.", jsonParams, "name.change");

    output = tester.getOutput();

    assertEquals("Changing your first name to be NameTom.", output.getFulfillmentText());
    assertEquals("NicknameTom", output.getDisplay());

    // Set last name to be LastNameTom -- should output display name as NicknameTom since last name
    // never displayed

    jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"LastNameTom\","
            + "\"type\": \"last name\"}";
    tester.setParameters("Change my first name to NameTom.", jsonParams, "name.change");

    output = tester.getOutput();

    assertEquals("Changing your last name to be LastNameTom.", output.getFulfillmentText());
    assertEquals("NicknameTom", output.getDisplay());

    // Set nickname to be NewNicknameTom -- should output display name as NicknameTom since nickname
    // > name

    jsonParams =
        "{\"nick-name\": \"\","
            + "\"last-name\": \"\","
            + "\"given-name\": \"NewNicknameTom\","
            + "\"type\": \"nickname\"}";
    tester.setParameters("Change my nickname to NewNicknameTom.", jsonParams, "name.change");

    output = tester.getOutput();

    assertEquals("Changing your nickname to be NewNicknameTom.", output.getFulfillmentText());
    assertEquals("NewNicknameTom", output.getDisplay());
  }
}
