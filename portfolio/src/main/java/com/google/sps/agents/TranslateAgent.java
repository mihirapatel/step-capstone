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
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.protobuf.Value;
import com.google.sps.utils.AgentUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Translate Agent */
public class TranslateAgent implements Agent {

  private static Logger log = LoggerFactory.getLogger(TranslateAgent.class);

  private final String intentName;
  private String text;
  private String languageTo;
  private String languageFrom;
  private String languageToCode;
  private String languageFromCode;
  private String translatedString;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;

  /**
   * Translate agent constructor that uses intent and parameter to determnine fulfillment for user
   * request.
   *
   * @param intentName String containing the specific intent within memory agent that user is
   *     requesting.
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public TranslateAgent(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  /**
   * Method that handles parameter assignment for fulfillment text and display based on the user's
   * input intent and extracted parameters
   *
   * @param parameters Map containing the detected entities in the user's intent.
   */
  public void setParameters(Map<String, Value> parameters) {
    text = parameters.get("text").getStringValue();
    languageTo = parameters.get("lang-to").getStringValue();
    languageFrom = parameters.get("lang-from").getStringValue();
    languageToCode = AgentUtils.getLanguageCode(languageTo).substring(0, 2);
    languageFromCode = AgentUtils.getLanguageCode(languageFrom).substring(0, 2);
    Translation translation = translate(text, languageFromCode, languageToCode);
    translatedString = translation.getTranslatedText();

    if (languageToCode == null && languageFromCode == null) {
      fulfillment = null;
    } else {
      text = text.substring(0, 1).toUpperCase() + text.substring(1);
      fulfillment = text + " in " + languageTo + " is: " + translatedString;
    }
  }

  public static Translation translate(String text, String languageFromCode, String languageToCode) {
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    Translation translation =
        translate.translate(
            text,
            Translate.TranslateOption.sourceLanguage(languageFromCode),
            Translate.TranslateOption.targetLanguage(languageToCode),
            // Use "base" for standard edition, "nmt" for the premium model.
            Translate.TranslateOption.model("nmt"));

    return translation;
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
    return redirect;
  }
}
