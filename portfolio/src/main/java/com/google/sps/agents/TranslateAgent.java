package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.translate.*;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Output;
import com.google.sps.agents.Agent;
import java.io.IOException;
import java.util.Map;
 
/**
 * Translate Agent
 */
public class TranslateAgent implements Agent {
    private final String intentName;
  	private String text;
    private String languageTo;
    private String langaugeFrom;
 
    public TranslateAgent(String intentName, Map<String, Value> parameters) {
      this.intentName = intentName;
      setParameters(parameters);
    }

	@Override 
	public void setParameters(Map<String, Value> parameters) {
        System.out.println(parameters);
        text = parameters.get("text").getStringValue();
        languageTo = parameters.get("lang-to").getStringValue();
        langaugeFrom = parameters.get("lang-from").getStringValue();
 
        if (langaugeFrom == "") {
            langaugeFrom = "English";
            System.out.println(langaugeFrom);
        }

	}
	
	@Override
	public String getOutput() {
        
	    return null;
	}

	@Override
	public String getDisplay() {
		return null;
	}

	@Override
	public String getRedirect() {
		return null;
    }

    public static Translation translateToEnglish(String text, String languageCode) {
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

    public static Translation translateFromEnglish(String text, String languageCode) {
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