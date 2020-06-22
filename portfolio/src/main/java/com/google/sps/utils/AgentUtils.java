package com.google.sps.utils;
 
// Imports the Google Cloud client library
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.Map;
import com.google.sps.data.Output;
import com.google.sps.agents.Language;
import com.google.sps.agents.WebSearch;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import com.google.sps.utils.TextUtils;
import com.google.gson.Gson;
import com.google.sps.utils.SpeechUtils;
import com.google.protobuf.ByteString;
 
/**
 * DialogFlow API Detect Intent sample with audio files processes as an audio stream.
 */
public class AgentUtils {
  
  public static Output getOutput(QueryResult queryResult, String languageCode) {
    String fulfillment = null;
    String display = null;
    String redirect = null;
    byte[] byteStringToByteArray = null;
 
    // Retrieve detected input and intent from DialogFlow result.
    String inputDetected = queryResult.getQueryText();
    inputDetected = inputDetected.equals("") ? " (null) " : inputDetected;
    String detectedIntent = queryResult.getIntent().getDisplayName();
 
    // Set Response Info From Web Search Agent
    if (detectedIntent.equals("web.search")){
        WebSearch webObject = new WebSearch();
        webObject.setParameters(getParameterMap(queryResult));
        fulfillment = webObject.getOutput();
        display = webObject.getDisplay();
        redirect = webObject.getRedirect();
        byteStringToByteArray = getByteStringToByteArray(fulfillment, languageCode);
    } else if (detectedIntent.contains("language.switch")) {
        Language webObject = new Language();
        webObject.setParameters(getParameterMap(queryResult));
        fulfillment = webObject.getOutput();
        display = webObject.getDisplay();
        redirect = webObject.getRedirect();
        byteStringToByteArray = getByteStringToByteArray(fulfillment, languageCode);
    }
 
    else{
      fulfillment = queryResult.getFulfillmentText();
      fulfillment = fulfillment.equals("") ? "I didn't hear you. Can you repeat that?" : fulfillment;
      byteStringToByteArray = getByteStringToByteArray(fulfillment, languageCode);
    }
 
    Output output = new Output(inputDetected, fulfillment, byteStringToByteArray, display, redirect);
    return output;
  }
 
  public static Map<String, Value> getParameterMap(QueryResult queryResult) {
    Struct paramStruct = queryResult.getParameters();
    Map<String, Value> parameters = paramStruct.getFieldsMap();
    return parameters;
  }
 
  public static byte[] getByteStringToByteArray(String fulfillment, String languageCode){
    byte[] byteArray = null;
    try {
        ByteString audioResponse = SpeechUtils.synthesizeText(fulfillment, languageCode);
        byteArray = audioResponse.toByteArray();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return byteArray;
  }

  public static String getLanguageCode(String language) {
    switch(language) {
      case "Chinese":
        return "zh-CN";
      case "English": 
        return "en-US";
      case "French":
        return "fr";
      case "German":
        return "de";
      case "Hindi":
        return "hi";
      case "Italian":
        return "it";
      case "Japanese":
        return "ja";
      case "Korean":
        return "ko";
      case "Portuguese":
        return "pt";
      case "Russian":
        return "ru";
      case "Spanish":
        return "es";
      case "Swedish":
        return "sv";
      default:
        return null;
    }
  }
}
