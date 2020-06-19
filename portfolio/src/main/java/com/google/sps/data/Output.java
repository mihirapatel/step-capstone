package com.google.sps.data;

/** Ouptut shown on web app screen. */
public final class Output {

  private final String userInput;
  private final String fulfillmentText;
  private final String display;

  // Constructor without display
  public Output(String userInput, String fulfillmentText) {
    this.userInput = userInput;
    this.fulfillmentText = fulfillmentText;
    this.display = null;
  }

  // Constructor with display
  public Output(String userInput, String fulfillmentText, String display) {
    this.userInput = userInput;
    this.fulfillmentText = fulfillmentText;
    this.display = display;
  }

  public String getUserInput() {
    return this.userInput;
  }

  public String getFulfillmentText() {
    return this.fulfillmentText;
  }

  public String getDisplay() {
    return this.display;
  }
}