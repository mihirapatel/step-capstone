package com.google.sps.utils;
 
// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.utils.TextUtils;
import com.google.sps.utils.SpeechUtils;
import com.google.sps.data.Output;
import com.google.sps.agents.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
 
/**
 * Identifies agent from Dialogflow API Query result and creates Output object
 */
public class AgentUtils {
  
  public static Output getOutput(QueryResult queryResult) {
    String fulfillment = null;
    String display = null;
    String redirect = null;
    byte[] byteStringToByteArray = null;
    Agent object = null;

    String detectedIntent = queryResult.getIntent().getDisplayName();
    String agentName = getAgentName(detectedIntent);
    String intentName = getIntentName(detectedIntent);

    // Retrieve detected input from DialogFlow result.
    String inputDetected = queryResult.getQueryText();
    inputDetected = inputDetected.equals("") ? " (null) " : inputDetected;
    Map<String, Value> parameterMap = getParameterMap(queryResult);

    // Set Response Info From Web Search Agent
    if (agentName.equals("calculator")){
        object = new Tip(intentName, parameterMap);
    }
    if (agentName.equals("currency")){
        object = new Currency(intentName, parameterMap);
    }
    if (agentName.equals("language")){
        object = new Language(intentName, parameterMap);
    }
    if (agentName.equals("name")){
        object = new Name(intentName, parameterMap);
    }
    if (agentName.equals("reminders")){
        object = new Reminders(intentName, parameterMap);
    }
    if (agentName.equals("time")){
        object = new Time(intentName, parameterMap);
    }
    if (agentName.equals("translate")){
        object = new Translate(intentName, parameterMap);
    }
    if (agentName.equals("units")){
        object = new UnitConverter(intentName, parameterMap);
    }
    if (agentName.equals("weather")){
        object = new Weather(intentName, parameterMap);
    }
    if (agentName.equals("web")){
        object = new WebSearch(intentName, parameterMap);
    }
    if (object != null){
        fulfillment = object.getOutput();
        display = object.getDisplay();
        redirect = object.getRedirect();
    }
    else {
        fulfillment = queryResult.getFulfillmentText();
        fulfillment = fulfillment.equals("") ? "I didn't hear you. Can you repeat that?" : fulfillment;
    }
    
    byteStringToByteArray = getByteStringToByteArray(fulfillment);
    Output output = new Output(inputDetected, fulfillment, byteStringToByteArray, display, redirect);
    return output;
  }

  public static String getAgentName(String detectedIntent) {
    String[] intentList = detectedIntent.split("\\.", 2);
    return intentList[0];
  }

  public static String getIntentName(String detectedIntent) {
    String[] intentList = detectedIntent.split("\\.", 2);
    String intentName = detectedIntent;
    if (intentList.length > 1){
        intentName = intentList[1];
    }
    return intentName;
  }

  public static Map<String, Value> getParameterMap(QueryResult queryResult) {
    Struct paramStruct = queryResult.getParameters();
    Map<String, Value> parameters = paramStruct.getFieldsMap();
    return parameters;
  }

  public static byte[] getByteStringToByteArray(String fulfillment){
    byte[] byteArray = null;
    try {
        ByteString audioResponse = SpeechUtils.synthesizeText(fulfillment);
        byteArray = audioResponse.toByteArray();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return byteArray;
  }
}



