package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.Map;
import com.google.sps.data.Output;
import com.google.sps.agents.Agent;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
 
/**
 * DialogFlow API Detect Intent sample with audio files processes as an audio stream.
 */
public class WebSearch implements Agent {
    String searchText;
 
    @Override 
    public void setParameters(Map<String, Value> parameters) {
      searchText = parameters.get("q").getStringValue();
    }
    
    @Override
    public String getOutput() {
      return "Redirecting to Google Search for " + searchText;
    }
 
    @Override
    public String getDisplay() {
        return null;
    }
 
    @Override
    public String getRedirect() {
        String baseURL = "http://www.google.com/search?q=";
        String[] individualWords = searchText.split(" ");
        String endURL = String.join("+", individualWords);
        return baseURL + endURL;
    }
}
 
 
 
 

 

