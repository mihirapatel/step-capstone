package com.google.sps.data;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import java.io.IOException;

public class DialogFlow {

    static SessionName session = SessionName.of("mihira-step-2020-3", "1");
    QueryResult queryResult;

    public DialogFlow(String text, String languageCode, SessionsClient sessionsClient) {
            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

            // Build the query with the TextInput
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

            // Performs the detect intent request
            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

            // Display the query result
            queryResult = response.getQueryResult();
    }

    public QueryResult getResponse() {
        return queryResult;
    }
}