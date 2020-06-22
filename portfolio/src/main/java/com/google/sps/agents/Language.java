package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.utils.AgentUtils;
import java.io.IOException;
import java.util.Map;
import com.google.sps.data.Output;
import com.google.sps.agents.Agent;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
 
/**
 * DialogFlow API Detect Intent sample with audio files processes as an audio stream.
 */
public class Language implements Agent {
    String language;
 
    @Override 
    public void setParameters(Map<String, Value> parameters) {
      language = parameters.get("language").getStringValue();
    }
    
    @Override
    public String getOutput() {
      if (AgentUtils.getLanguageCode(language) == null) {
        return "Sorry, this language is not supported.";
      }
      return "Switching conversation language to " + language;
    }
 
    @Override
    public String getDisplay() {
        return null;
    }
 
    @Override
    public String getRedirect() {
        return null;
    }
}