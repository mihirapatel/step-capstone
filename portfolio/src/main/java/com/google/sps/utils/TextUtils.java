package com.google.sps.utils;

import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.sps.data.DialogFlow;
import java.io.IOException;

/** DialogFlow API Detects Intent with input text. */
public class TextUtils {

  public static DialogFlow detectIntentStream(String text, String languageCode) {
    DialogFlow dialogFlowResult = null;

    try (SessionsClient sessionsClient = SessionsClient.create()) {
      dialogFlowResult = new DialogFlow(text, languageCode, sessionsClient);

      System.out.println("====================");
      System.out.format("Query Text: '%s'\n", dialogFlowResult.getQueryText());
      System.out.format(
          "Detected Intent: %s (confidence: %f)\n",
          dialogFlowResult.getIntentName(), dialogFlowResult.getIntentConfidence());
      System.out.format("Fulfillment Text: '%s'\n", dialogFlowResult.getFulfillmentText());

    } catch (IOException e) {
      e.printStackTrace();
    }
    return dialogFlowResult;
  }
}
