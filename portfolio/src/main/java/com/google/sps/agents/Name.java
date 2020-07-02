package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import com.google.sps.utils.UserUtils;
import java.util.Map;

/** Name Agent */
public class Name implements Agent {
  private final String intentName;
  String outputText;
  String userID;
  String userDisplayName;

  public Name(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    userID = UserUtils.getUserID();
    if (userID == null) {
      outputText = "Please login to modify your name.";
    } else {
      try {
        setParameters(parameters);
      } catch (Exception e) {
        return;
      }
    }
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    System.out.println("Name parameters");
    System.out.println(parameters);
    String nameType = parameters.get("type").getStringValue();
    String name = null;
    nameType = nameType.equals("") ? "first name" : nameType;
    name = getSpecificName(parameters, nameType);
    if (name.equals("")) {
      outputText = "I'm sorry, I didn't catch the name. Can you repeat that?";
    } else {
      UserUtils.saveName(nameType, name);
      outputText = "Changing your " + nameType + " to be " + name;
      userDisplayName = UserUtils.getDisplayName();
    }
  }

  private String getSpecificName(Map<String, Value> parameters, String nameType) {
    String name = parameters.get("given-name").getStringValue();
    if (!name.equals("")) {
      return name;
    }
    if (nameType.equals("last name")) {
      return parameters.get("last-name").getStringValue();
    } else if (nameType.equals("nickname")) {
      return parameters.get("nick-name").getStringValue();
    }
    return null;
  }

  @Override
  public String getOutput() {
    return outputText;
  }

  @Override
  public String getDisplay() {
    return userDisplayName;
  }

  @Override
  public String getRedirect() {
    return null;
  }
}
