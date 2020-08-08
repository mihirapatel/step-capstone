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

package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.utils.TimeUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/** Reminders Agent */
public class RemindersAgent implements Agent {
  private String intentName = null;
  private String recurrence = null;
  private Date date = null;
  private String name = "";
  private String fulfillment = "";
  private String display = null;

  /**
   * Reminders agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public RemindersAgent(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    try {
      setParameters(parameters);
    } catch (Exception e) {
      fulfillment = null;
    }
  }

  /**
   * Method that handles parameter assignment for fulfillment text and display based on the user's
   * input intent and extracted parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters) {
    if (intentName.equals("snooze")) {
      Struct durationStruct = parameters.get("date-time").getStructValue();
      Map<String, Value> durationMap = durationStruct.getFieldsMap();
      try {
        Date start = TimeUtils.stringToDate(durationMap.get("startDateTime").getStringValue());
        Date end = TimeUtils.stringToDate(durationMap.get("endDateTime").getStringValue());
        int diffSec = (int) ((end.getTime() - start.getTime()) / 1000);
        if (diffSec < 1 || diffSec > 86400) {
          fulfillment =
              "Sorry, unable to set a timer for less than 1 second or more than 1 day. Please try adding a reminder instead.";
        } else {
          fulfillment = "Starting a timer for " + TimeUtils.secondsToHMSString(diffSec) + " now.";
          display = TimeUtils.makeClockDisplay(diffSec);
        }
      } catch (ParseException e) {
        System.err.println("Unable to parse date format.");
      }
    } else {
      fulfillment = "Sorry, the reminders feature is not supported yet.";
      // TODO: if we ever get access to storing user info or calendars...
    }
  }

  @Override
  public String getOutput() {
    return fulfillment;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  @Override
  public String getRedirect() {
    return null;
  }
}
