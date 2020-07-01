package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import java.util.Map;

/**
 * Agents handle Dialogflow's detected intent and parameters for each user request. From the
 * detected intents and parameters, agents will generate appropriate audio outputs, displays, and
 * redirect links to return to the user
 */
public interface Agent {

  /**
   * This function parses a Map of parameters from Dialogflow and sets the necessary output and/or
   * display and/or redirect links that will be outputted to the user
   *
   * @param address user-inputted location
   */
  public void setParameters(Map<String, Value> parameters) throws Exception;

  /**
   * This function returns a String containing the text to be outputted to the user (on the screen
   * and via audio)
   *
   * @return String audio/text output for user
   */
  public String getOutput();

  /**
   * This function returns a String containing the HTML code to be displayed to the user interface
   * (if any)
   *
   * @return String of HTML code
   */
  public String getDisplay();

  /**
   * This function returns a String containing URL to the page the user should be redirected to (if
   * any)
   *
   * @return String of link URL
   */
  public String getRedirect();
}
