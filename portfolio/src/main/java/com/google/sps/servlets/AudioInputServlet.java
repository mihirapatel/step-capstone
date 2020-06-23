// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
package com.google.sps.servlets;

import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.translate.*;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.sps.utils.AgentUtils;
import com.google.sps.utils.AudioUtils;
import com.google.sps.data.Output;
import com.google.sps.utils.SpeechUtils;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletInputStream;
 
/** Servlet that takes in audio stream and retrieves
 ** user input string to display. */
 
@WebServlet("/audio-input")
public class AudioInputServlet extends HttpServlet {
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
 
    // Convert input stream into bytestring for DialogFlow API input
    ServletInputStream stream = request.getInputStream();
    ByteString bytestring = ByteString.readFrom(stream);
    String language = request.getParameter("language");
    Output output = null;

    if (language.equals("English")) {
      output = handleEnglishQuery(bytestring, null);
    } else {
      try {
        output = handleForeignQuery(bytestring, language);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
 
    //Convert to JSON string
    String json = new Gson().toJson(output);
    response.getWriter().write(json);
  }

  private Output handleEnglishQuery(ByteString bytestring, String languageCode) {
    QueryResult result = AudioUtils.detectIntentStream(bytestring);
    if (result == null) {
      return null;
    }
    return AgentUtils.getOutput(result, languageCode);
  }

  private Output handleForeignQuery(ByteString bytestring, String language) {
    System.out.println("LANGUAGE : " + language);
    String languageCode = AgentUtils.getLanguageCode(language);
    System.out.println("CODE: " + languageCode);
    String detectedUserInputString = AudioUtils.detectSpeechLanguage(bytestring.toByteArray(), languageCode);
    System.out.println("TODO: handle foreign language inputs.");

    //Google Translate API - convert detectedUserInputString from language to English
    Translation inputTranslation = translateToEnglish(detectedUserInputString, languageCode);

    //call handleEnglishQuery
    String translatedInputText = inputTranslation.getTranslatedText(); 
    ByteString inputByteString = null;
    try {
        inputByteString = SpeechUtils.synthesizeText(translatedInputText, languageCode);
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    Output englishOutput = handleEnglishQuery(inputByteString, languageCode);

    // Google Translate API - convert Output.userInput and Output.fulfillment to appropriate language
    String userInput = englishOutput.getUserInput();
    String fulfillment = englishOutput.getFulfillmentText();
    String userInputTranslation = translateFromEnglish(userInput, languageCode).getTranslatedText();
    String fulfillmentTranslation = translateFromEnglish(userInput, languageCode).getTranslatedText();
    byte[] byteArray = AgentUtils.getByteStringToByteArray(fulfillmentTranslation, languageCode);
    Output languageOutput = new Output(userInputTranslation, fulfillmentTranslation, byteArray);
    return languageOutput;
  }

  private Translation translateToEnglish(String text, String languageCode) {

    Translate translate = TranslateOptions.getDefaultInstance().getService();

    Translation translation =
      translate.translate(
        text,
        Translate.TranslateOption.sourceLanguage(languageCode),
        Translate.TranslateOption.targetLanguage("en-US"),
        // Use "base" for standard edition, "nmt" for the premium model.
        Translate.TranslateOption.model("nmt"));

    System.out.printf("TranslatedText:\nText: %s\n", translation.getTranslatedText());
    return translation;
  }

  private Translation translateFromEnglish(String text, String languageCode) {
    
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    Translation translation =
      translate.translate(
        text,
        Translate.TranslateOption.sourceLanguage("en-US"),
        Translate.TranslateOption.targetLanguage(languageCode),
        // Use "base" for standard edition, "nmt" for the premium model.
        Translate.TranslateOption.model("nmt"));

    System.out.printf("TranslatedText:\nText: %s\n", translation.getTranslatedText());
    return translation;
  }
}