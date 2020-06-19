package com.google.sps.data;
 
/** Ouput shown on web app screen. */
public final class Output {
 
  private final String userInput;
  private final String fulfillmentText;
  private final byte[] byteStringToByteArray;
  private final String display;
 
  // Constructor without display
  public Output(String userInput, String fulfillmentText, byte[] byteStringToByteArray) {
      this.userInput = userInput;
      this.fulfillmentText = fulfillmentText;
      this.byteStringToByteArray = byteStringToByteArray;
      this.display = null;
  }
 
 
  // Constructor with display
  public Output(String userInput, String fulfillmentText, byte[] byteStringToByteArray, String display) {
      this.userInput = userInput;
      this.fulfillmentText = fulfillmentText;
      this.byteStringToByteArray = byteStringToByteArray;
      this.display = display;
  }
 
  public String getUserInput() {
      return this.userInput;
  }
 
  public String getFulfillmentText() {
      return this.fulfillmentText;
  }
 
  public byte[] getByteStringToByteArray() {
      return this.byteStringToByteArray;
  }
 
  public String getDisplay() {
      return this.display;
  }
 
 
}
