package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.sps.data.Book;
import com.google.sps.data.BookQuery;
import com.google.sps.data.DialogFlowClient;
import com.google.sps.data.Output;
import com.google.sps.utils.BooksMemoryUtils;
import com.google.sps.utils.MemoryUtils;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelper {

  private static Logger log = LoggerFactory.getLogger(TestHelper.class);

  @Mock DialogFlowClient dialogFlowMock;
  @Mock UserService userServiceMock;

  @InjectMocks TextInputServlet textInputServlet;

  HttpServletRequest request;
  HttpServletResponse response;
  TextInputServlet servlet;
  DatastoreService customDatastore;
  String sessionID;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  /** Default constructor for TestHelper that sets up testing environment and empty mocks. */
  public TestHelper() {
    helper.setUp();
    customDatastore = DatastoreServiceFactory.getDatastoreService();
    userServiceMock = mock(UserService.class);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    dialogFlowMock = mock(DialogFlowClient.class);
    servlet = new TestableTextInputServlet();
    sessionID = "fallbackTestingID";
    setLoggedIn();
  }

  /**
   * TestHelper constructor that runs end-to-end testing of Dialogflow and agents. Use to test both
   * Dialogflow's ability to detect the proper intent and verify agent fulfillment. Default has a
   * logged in user. (Must set logged out user manually using set method).
   *
   * @param inputText Text input of user's intent sent to Dialogflow.
   */
  public TestHelper(String inputText) {
    this();
    servlet = new TestableDFTextInputServlet();
    setInputText(inputText);
  }

  /**
   * TestHelper constructor that mocks Dialogflow with expected intent detection. Use to test proper
   * agent fulfillment given the correct Dialogflow output.
   *
   * @param inputText Text input of user's intent sent to Dialogflow.
   * @param parameters String json that consists of expected parameters identified by Dialogflow.
   * @param intentName String name of expected intent identified by Dialogflow.
   */
  public TestHelper(String inputText, String parameters, String intentName)
      throws InvalidProtocolBufferException {
    this();
    servlet = new TestableTextInputServlet();
    setParameters(inputText, parameters, intentName);
  }

  /**
   * TestHelper constructor that mocks Dialogflow in case of insufficient intent detection. Use to
   * test proper agent fulfillment given incomplete required parameters.
   *
   * @param inputText Text input of user's intent sent to Dialogflow.
   * @param parameters String json that consists of expected parameters identified by Dialogflow.
   * @param intentName String name of expected intent identified by Dialogflow.
   * @param allParamsPresent Boolean indicating if all required parameters are present.
   */
  public TestHelper(
      String inputText, String parameters, String intentName, Boolean allParamsPresent)
      throws InvalidProtocolBufferException {
    this(inputText, parameters, intentName);
    setParamsPresent(allParamsPresent);
  }

  /**
   * TestHelper constructor that mocks Dialogflow in case of insufficient intent detection and sets
   * up session ID for current "session". Use to test proper agent fulfillment given a sessionID.
   *
   * @param inputText Text input of user's intent sent to Dialogflow.
   * @param parameters String json that consists of expected parameters identified by Dialogflow.
   * @param intentName String name of expected intent identified by Dialogflow.
   * @param id String unique ID for current session
   */
  public TestHelper(String inputText, String parameters, String intentName, String id)
      throws InvalidProtocolBufferException {
    this(inputText, parameters, intentName);
    sessionID = id;
  }

  /**
   * Gets the output object created by agent fulfillment after at the end of back-end process.
   *
   * @return Output object identical to that which is passed back to javascript.
   */
  public Output getOutput() throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    verify(request, atLeast(1)).getParameter("request-input");
    writer.flush();
    Output output = new Gson().fromJson(stringWriter.toString(), Output.class);
    return output;
  }

  /**
   * Gets the output object created by agent fulfillment after at the end of back-end process.
   *
   * @param intent intent String passed as parameter to Servlet
   * @param sessionID unique ID for current session
   * @param parameterMap String containing parameters needed, if any, by BookAgent
   * @param queryID unique ID (within sessionID) for current query
   * @return Output object identical to that which is passed back to javascript.
   */
  public Output getOutput(String intent, String sessionID, String parameterMap, String queryID)
      throws InvalidProtocolBufferException {
    BookAgentServlet bookServlet = new BookAgentServlet();
    Output output =
        bookServlet.getOutputFromBookAgent(
            intent,
            sessionID,
            BookAgentServlet.stringToMap(parameterMap),
            "en-US",
            queryID,
            customDatastore);
    return output;
  }

  /**
   * Sets the input text to mock http request.
   *
   * @param inputText Text input of user's intent sent to Dialogflow.
   */
  public void setInputText(String inputText) {
    when(request.getParameter("request-input")).thenReturn(inputText);
  }

  /**
   * Sets the general return parameters to mock dialogflow.
   *
   * @param inputText Text input of user's intent sent to Dialogflow.
   * @param parameters String json that consists of expected parameters identified by Dialogflow.
   * @param intentName String name of expected intent identified by Dialogflow.
   */
  public void setParameters(String inputText, String parameters, String intentName)
      throws InvalidProtocolBufferException {
    setInputText(inputText);
    setSessionID();
    Map<String, Value> map = BookAgentServlet.stringToMap(parameters);
    when(dialogFlowMock.getParameters()).thenReturn(map);
    when(dialogFlowMock.getIntentName()).thenReturn(intentName);
    when(dialogFlowMock.getQueryText()).thenReturn(inputText);
    when(dialogFlowMock.getIntentConfidence()).thenReturn((float) 1.0);
    when(dialogFlowMock.getFulfillmentText()).thenReturn("");
    when(dialogFlowMock.getAllRequiredParamsPresent()).thenReturn(true);
  }

  /**
   * Sets the mock returned for dialogflow all parameters present.
   *
   * @param allParamsPresent Boolean indicating if all required parameters are present.
   */
  public void setParamsPresent(boolean allParamsPresent) {
    when(dialogFlowMock.getAllRequiredParamsPresent()).thenReturn(allParamsPresent);
  }

  /**
   * Sets the mocks returned for a default logged-in user. Default user has: email:
   * "test@example.com" user id: "1"
   */
  public void setLoggedIn() {
    setUser("test@example.com", "1");
  }

  /** Sets the user service mock to return a logged-out user. */
  public void setLoggedOut() {
    when(userServiceMock.isUserLoggedIn()).thenReturn(false);
  }

  /** Sets the sessionID for testing */
  public void setSessionID() {
    when(request.getParameter("session-id")).thenReturn(sessionID);
  }

  /**
   * Creates a customized user.
   *
   * @param email String containing the user's email.
   * @param id String containing the user's id number.
   */
  public void setUser(String email, String id) {
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    when(userServiceMock.getCurrentUser()).thenReturn(new User(email, "authDomain", id));
  }

  /**
   * Populates customizes datastore with desired string comments.
   *
   * @param comments List of strings containing comments to be stored in custom database.
   */
  public void setCustomDatabase(List<String> comments) {
    setCustomDatabase(comments, (new Date()).getTime());
  }

  /**
   * Populates customizes datastore with desired string comments.
   *
   * @param comments List of strings containing comments to be stored in custom database.
   */
  public void setCustomDatabase(List<String> comments, long startTime) {
    int increment = 0;
    for (String comment : comments) {
      MemoryUtils.makeCommentEntity("1", customDatastore, comment, true, startTime + (increment++));
    }
  }

  /**
   * Populates customizes datastore with desired list attributes.
   *
   * @param comments List of strings containing comments to be stored in custom database.
   */
  public void setCustomDatabase(String listName, ArrayList<String> items, long startTime) {
    MemoryUtils.makeListEntity(customDatastore, "1", items, listName, startTime);
  }

  /**
   * Populates customizes datastore with desired BookQuery object.
   *
   * @param query BookQuery object to store
   * @param sessionID unique id of session to store
   * @param queryID unique id (within sessionID) of query to store
   */
  public void setCustomDatabase(BookQuery query, String sessionID, String queryID) {
    BooksMemoryUtils.storeBookQuery(query, sessionID, queryID, customDatastore);
  }

  /**
   * Populates customizes datastore with desired Indices parameters, used to construct an indices
   * object.
   *
   * @param startIndex index to start retrieving Volume objects from
   * @param resultsStored number of results stored
   * @param totalResults total matches in Google Book API
   * @param displayNum number of results displayed request
   * @param sessionID unique id of session to store
   * @param queryID unique id (within sessionID) of query to store
   */
  public void setCustomDatabase(
      int startIndex,
      int totalResults,
      int resultsStored,
      int displayNum,
      String sessionID,
      String queryID) {
    BooksMemoryUtils.storeIndices(
        startIndex, totalResults, resultsStored, displayNum, sessionID, queryID, customDatastore);
  }

  /**
   * Populates customizes datastore with desired Books, starting at startIndex.
   *
   * @param books ArrayList of Book objects to store
   * @param startIndex index to start order at
   * @param sessionID unique id of session to store
   * @param queryID unique id (within sessionID) of query to store
   */
  public void setCustomDatabase(
      ArrayList<Book> books, int startIndex, String sessionID, String queryID) {
    BooksMemoryUtils.storeBooks(books, startIndex, sessionID, queryID, customDatastore);
  }

  /**
   * Retrieves a list of entity objects for the default user from the given query for testing
   * purposes to ensure that result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @return A list of entity objects returned by datastore.
   */
  public List<Entity> fetchDatastoreEntities(String category) {
    return fetchDatastoreEntities(category, "1");
  }

  /**
   * Retrieves a list of entity objects for all users from the given query for testing purposes to
   * ensure that result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @return A list of entity objects returned by datastore.
   */
  public List<Entity> fetchDatastoreAllUsers(String category) {
    Query query = new Query(category);
    return customDatastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Retrieves a list of entity objects from the given query for testing purposes to ensure that
   * result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @param userID String representing the user ID number for getting specific info about other
   *     users
   * @return A list of entity objects returned by datastore.
   */
  public List<Entity> fetchDatastoreEntities(String category, String userID) {
    Filter filter = new FilterPredicate("userID", FilterOperator.EQUAL, userID);
    return fetchDatastoreEntities(category, filter);
  }

  /**
   * Retrieves a list of entity objects from the given query for testing purposes to ensure that
   * result in datastore match the expected.
   *
   * @param category String containing the type of entity we are querying for.
   * @param filter Filter for query results.
   * @return A list of entity objects returned by datastore.
   */
  public List<Entity> fetchDatastoreEntities(String category, Filter filter) {
    Query query =
        new Query(category).setFilter(filter).addSort("timestamp", SortDirection.DESCENDING);
    return customDatastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  private class TestableTextInputServlet extends TextInputServlet {
    @Override
    public DialogFlowClient createDialogFlow(
        String text, String languageCode, SessionsClient sessionsClient) {
      return dialogFlowMock;
    }

    @Override
    public UserService createUserService() {
      return userServiceMock;
    }

    @Override
    public DatastoreService createDatastore() {
      return customDatastore;
    }
  }

  private class TestableDFTextInputServlet extends TextInputServlet {
    @Override
    public UserService createUserService() {
      return userServiceMock;
    }
  }
}
