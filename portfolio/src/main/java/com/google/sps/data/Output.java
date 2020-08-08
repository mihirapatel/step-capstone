/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.data;

/** Ouput shown on web app screen. */
public final class Output {

  private final String userInput;
  private final String fulfillmentText;
  private final byte[] byteStringToByteArray;
  private final String display;
  private final String redirect;
  private final String intent;

  /**
   * Output constructor for instance without display.
   *
   * @param userInput String representation of user input
   * @param fulfillmentText String representation of system response
   * @param byteStringToByteArray Byte array containing output audio response
   * @param intent String containing the detected intent for user input
   */
  public Output(
      String userInput, String fulfillmentText, byte[] byteStringToByteArray, String intent) {
    this(userInput, fulfillmentText, byteStringToByteArray, null, null, intent);
  }

  /**
   * Output constructor for instance with display.
   *
   * @param userInput String representation of user input
   * @param fulfillmentText String representation of system response
   * @param byteStringToByteArray Byte array containing output audio response
   * @param display String containing necessary information to create frontend javascript display
   * @param intent String containing the detected intent for user input
   */
  public Output(
      String userInput,
      String fulfillmentText,
      byte[] byteStringToByteArray,
      String display,
      String intent) {
    this(userInput, fulfillmentText, byteStringToByteArray, display, null, intent);
  }

  /**
   * Output constructor for instance with display and redirect.
   *
   * @param userInput String representation of user input
   * @param fulfillmentText String representation of system response
   * @param byteStringToByteArray Byte array containing output audio response
   * @param display String containing necessary information to create frontend javascript display
   * @param redirect String containing necessary information for frontend redirect
   * @param intent String containing the detected intent for user input
   */
  public Output(
      String userInput,
      String fulfillmentText,
      byte[] byteStringToByteArray,
      String display,
      String redirect,
      String intent) {
    this.userInput = userInput;
    this.fulfillmentText = fulfillmentText;
    this.byteStringToByteArray = byteStringToByteArray;
    this.display = display;
    this.redirect = redirect;
    this.intent = intent;
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

  public String getRedirect() {
    return this.redirect;
  }

  public String getIntent() {
    return this.intent;
  }
}
