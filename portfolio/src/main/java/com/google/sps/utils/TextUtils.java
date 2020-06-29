package com.google.sps.utils;

import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.sps.data.DialogFlow;
import java.io.IOException;

/** DialogFlow API Detects Intent with input text. */
public class TextUtils {


    public static QueryResult detectIntentStream(String text, String languageCode) {
        QueryResult queryResult = null;

        try (SessionsClient sessionsClient = SessionsClient.create()) {
            DialogFlow dialogFlow = new DialogFlow(text, languageCode, sessionsClient);
            queryResult = dialogFlow.getResponse();

            System.out.println("====================");
            System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
            System.out.format("Detected Intent: %s (confidence: %f)\n",
                queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
            System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryResult;
    }
}
