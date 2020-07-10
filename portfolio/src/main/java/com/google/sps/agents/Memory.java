package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.gson.Gson;
import com.google.protobuf.Value;
import com.google.sps.data.ConversationOutput;
import com.google.sps.data.Pair;
import com.google.sps.utils.MemoryUtils;
import com.google.sps.utils.TimeUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Memory Agent */
public class Memory implements Agent {
  private final String intentName;
  private String userID;
  private String fulfillment;
  private String display;
  private DatastoreService datastore;
  private UserService userService;

  /**
   * Memory agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   * @param userService UserService instance to access userID and other user info.
   * @param datastore DatastoreService instance used to access past comments from the user's
   *     database.
   */
  public Memory(
      String intentName,
      Map<String, Value> parameters,
      UserService userService,
      DatastoreService datastore) {
    this.intentName = intentName;
    this.userService = userService;
    this.datastore = datastore;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    if (!userService.isUserLoggedIn()) {
      fulfillment = "Please login to access conversation history.";
      return;
    }
    userID = userService.getCurrentUser().getUserId();
    if (intentName.contains("keyword")) {
      findKeyword(parameters);
    } else if (intentName.contains("time")) {
      findTimePeriodComments(parameters);
    }
  }

  private void findKeyword(Map<String, Value> parameters) {
    String word = parameters.get("keyword").getStringValue();
    List<Pair<Entity, List<Entity>>> conversationList;
    String timePeriodDisplay = "";
    Value dateObject = parameters.get("date-time");
    try {
      if (dateObject != null) {
        Pair<Long, Long> timeRange = getTimeRange(dateObject);
        conversationList =
            MemoryUtils.getKeywordCommentEntitiesWithTime(
                datastore, userID, word.toLowerCase(), timeRange.getKey(), timeRange.getValue());
        timePeriodDisplay = " from " + parameters.get("date-time-original").getStringValue();
      } else {
        conversationList =
            MemoryUtils.getKeywordCommentEntities(datastore, userID, word.toLowerCase());
      }
      if (conversationList.isEmpty()) {
        fulfillment = "Sorry, there were no results matching the keyword \"" + word + ".\"";
      } else {
        fulfillment =
            "Here are all the results"
                + timePeriodDisplay
                + " including the keyword \""
                + word
                + ".\"";
        ConversationOutput convoOutput = new ConversationOutput(word, conversationList);
        display = convoOutput.toString();
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private void findTimePeriodComments(Map<String, Value> parameters) {
    try {
      Pair<Long, Long> timeRange = getTimeRange(parameters.get("date-time"));
      List<Entity> conversationSnippet =
          MemoryUtils.getTimePeriodCommentEntities(
              datastore, userID, timeRange.getKey(), timeRange.getValue());
      if (conversationSnippet.isEmpty()) {
        fulfillment =
            "Could not find any conversation from "
                + parameters.get("date-time-original").getStringValue()
                + ".";
      } else {
        fulfillment =
            "Here are all the results from "
                + parameters.get("date-time-original").getStringValue()
                + ".";
        display = new Gson().toJson(conversationSnippet);
        ;
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private Pair<Long, Long> getTimeRange(Value dateObject) throws ParseException {
    if (dateObject.hasStructValue()) {
      Map<String, Value> durationMap = dateObject.getStructValue().getFieldsMap();
      Date start = TimeUtils.stringToDate(durationMap.get("startDateTime").getStringValue());
      Date end = TimeUtils.stringToDate(durationMap.get("endDateTime").getStringValue());
      return new Pair(start.getTime(), end.getTime());
    } else {
      String dateString = dateObject.getStringValue();
      Date dateTime = TimeUtils.stringToDate(dateString);
      return new Pair(dateTime.getTime() - 300000, dateTime.getTime() + 300000);
    }
  }

  @Override
  public String getOutput() {
    return fulfillment;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  @Override
  public String getRedirect() {
    return null;
  }
}
