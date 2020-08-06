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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.sps.agents.TranslateAgent;
import com.google.sps.data.DialogFlowClient;
import com.google.sps.data.Output;
import com.google.sps.data.RecommendationsClient;
import com.google.sps.utils.AgentUtils;
import com.google.sps.utils.AudioUtils;
import com.google.sps.utils.SpeechUtils;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that takes in audio stream and retrieves user input string to display. */
@WebServlet("/audio-input")
public class AudioInputServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private UserService userService = UserServiceFactory.getUserService();
  private RecommendationsClient recommender = new RecommendationsClient();

  /**
   * POST method that handles http request for dialogflow response to audio user input
   *
   * @param request HTTP request containing user's input audio, language, and sesion ID
   * @param response Writer to return http response to input request
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    // Convert input stream into bytestring for DialogFlow API input
    ServletInputStream stream = request.getInputStream();
    ByteString bytestring = ByteString.readFrom(stream);
    String sessionID = request.getParameter("session-id");
    String language = request.getParameter("language");
    Output output = null;

    if (language.equals("English")) {
      output = handleEnglishQuery(bytestring, sessionID);
    } else {
      try {
        output = handleForeignQuery(bytestring, language, sessionID);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // Convert to JSON string
    String json = new Gson().toJson(output);
    response.getWriter().write(json);
  }

  /**
   * Handles English input audio into dialogflow and returns English response.
   *
   * @param bytestring Bytestring containing user input audio recording
   * @param sessionID The unique identifier for the current session
   * @return Output object containing all output audio, text, and display information.
   */
  private Output handleEnglishQuery(ByteString bytestring, String sessionID) {
    DialogFlowClient result = AudioUtils.detectIntentStream(bytestring);
    if (result == null) {
      return null;
    }
    return AgentUtils.getOutput(result, "en-US", userService, datastore, sessionID, recommender);
  }

  /**
   * Handles foreign language input audio into dialogflow and returns response in corresponding
   * language.
   *
   * @param bytestring Bytestring containing user input audio recording
   * @param language String containing the lanugage of input audio
   * @param sessionID The unique identifier for the current session
   * @return Output object containing all output audio, text, and display information.
   */
  private Output handleForeignQuery(ByteString bytestring, String language, String sessionID) {
    String languageCode = AgentUtils.getLanguageCode(language);
    String detectedUserInputString =
        AudioUtils.detectSpeechLanguage(bytestring.toByteArray(), languageCode);
    String englishLanguageCode = AgentUtils.getLanguageCode("English");
    // Google Translate API - convert detectedUserInputString from language to English
    Translation inputTranslation =
        TranslateAgent.translate(detectedUserInputString, languageCode, englishLanguageCode);

    String translatedInputText = inputTranslation.getTranslatedText();
    ByteString inputByteString = null;
    try {
      inputByteString = SpeechUtils.synthesizeText(translatedInputText, languageCode);
    } catch (Exception e) {
      e.printStackTrace();
    }

    DialogFlowClient englishOutput =
        (new TextInputServlet()).detectIntentStream(translatedInputText, englishLanguageCode);

    // Google Translate API - convert input and fulfillment to appropriate language
    String userInput = englishOutput.getQueryText();
    String fulfillment = englishOutput.getFulfillmentText();
    String userInputTranslation =
        TranslateAgent.translate(userInput, englishLanguageCode, languageCode).getTranslatedText();
    String fulfillmentTranslation =
        TranslateAgent.translate(fulfillment, englishLanguageCode, languageCode)
            .getTranslatedText();
    byte[] byteArray = AgentUtils.getByteStringToByteArray(fulfillmentTranslation, languageCode);
    Output languageOutput =
        new Output(
            userInputTranslation,
            fulfillmentTranslation,
            byteArray,
            englishOutput.getIntentName(),
            sessionID);
    return languageOutput;
  }
}
