package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.cloud.translate.*;
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

  public TranslateAgent(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  public void setParameters(Map<String, Value> parameters) {
    System.out.println(parameters);
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

  public static Translation translate(String text, String languageFromCode, String languageToCode) {
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    Translation translation =
        translate.translate(
            text,
            Translate.TranslateOption.sourceLanguage(languageFromCode),
            Translate.TranslateOption.targetLanguage(languageToCode),
            // Use "base" for standard edition, "nmt" for the premium model.
            Translate.TranslateOption.model("nmt"));

    System.out.printf("TranslatedText:\n", translation.getTranslatedText());
    return translation;
  }
}
