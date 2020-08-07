package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import com.google.sps.utils.AgentUtils;
import java.util.Map;

/** Language Agent */
public class Language implements Agent {

  String language;
  private final String intentName;
  private String searchText;

  public Language(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

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
