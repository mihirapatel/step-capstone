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
      setParameters(parameters);
    }
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    System.out.println("Name parameters");
    System.out.println(parameters);
    String nameType = parameters.get("type").getStringValue();
    String name = null;
    nameType = nameType.equals("") ? "first name" : nameType;
    switch (nameType) {
      case "first name":
        name = parameters.get("given-name").getStringValue();
        break;
      case "middle name":
        name = parameters.get("given-name").getStringValue();
        break;
      case "last name":
        name = parameters.get("last-name").getStringValue();
        break;
      case "nickname":
        name = parameters.get("nick-name").getStringValue();
        name = (name == null) ? parameters.get("given-name").getStringValue() : name;
    }
    UserUtils.saveName(nameType, name);
    outputText = "Changing your " + nameType + " to be " + name;
    userDisplayName = UserUtils.getDisplayName();
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
