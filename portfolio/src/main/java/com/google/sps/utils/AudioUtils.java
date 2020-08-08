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

package com.google.sps.utils;

// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import com.google.sps.data.DialogFlowClient;
import java.io.IOException;

/** DialogFlow API Detect Intent sample with audio files processes as an audio stream. */
public class AudioUtils {

  /**
   * Detects the appropriate intent corresponding to the user's audio input stream
   *
   * @param bytestring Bytestring containing user input audio recording
   * @return DialogFlow Client instance containing dialogflow result
   */
  public static DialogFlowClient detectIntentStream(ByteString bytestring) {
    DialogFlowClient queryResult = null;

    try (SessionsClient sessionsClient = SessionsClient.create()) {
      try {
        queryResult = new DialogFlowClient(sessionsClient, bytestring, 48000);
      } catch (Exception e) {
        queryResult = new DialogFlowClient(sessionsClient, bytestring, 44100);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return queryResult;
  }

  /**
   * Detects the appropriate intent corresponding to the user's audio input stream for the specified
   * sample hertz header for the recording wav file.
   *
   * @param bytestring Bytestring containing user input audio recording
   * @param sampleHertz Sample frequency of audio recording in hertz
   * @return DialogFlow Client instance containing dialogflow result
   */
  public static DialogFlowClient detectIntentStream(ByteString bytestring, int sampleHertz) {
    DialogFlowClient queryResult = null;

    try (SessionsClient sessionsClient = SessionsClient.create()) {
      queryResult = new DialogFlowClient(sessionsClient, bytestring, sampleHertz);
      printResult(queryResult);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return queryResult;
  }

  /**
   * Transcribe a short audio file using synchronous speech recognition
   *
   * @param data Input audio file recording
   * @param languageCode Two-letter representation of input audio language
   * @return transcribed text of audio input
   */
  public static String detectSpeechLanguage(byte[] data, String languageCode) {
    try (SpeechClient speechClient = SpeechClient.create()) {
      try {
        return getAudioLanguage(speechClient, data, languageCode, 48000);
      } catch (Exception e) {
        return getAudioLanguage(speechClient, data, languageCode, 44100);
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    return null;
  }

  /**
   * Helper function for transcribing a short audio file using synchronous speech recognition
   *
   * @param speechClient Speech client instance for audio transcription
   * @param data Input audio file recording
   * @param languageCode Two-letter representation of input audio language
   * @param sampleRate Sample frequency of audio recording in hertz
   * @return transcribed text of audio input
   */
  private static String getAudioLanguage(
      SpeechClient speechClient, byte[] data, String languageCode, int sampleRate) {
    RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;
    RecognitionConfig config =
        RecognitionConfig.newBuilder()
            .setLanguageCode(languageCode)
            .setSampleRateHertz(sampleRate)
            .setEncoding(encoding)
            .build();
    ByteString content = ByteString.copyFrom(data);
    RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(content).build();
    RecognizeRequest request =
        RecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();
    RecognizeResponse response = speechClient.recognize(request);
    for (SpeechRecognitionResult result : response.getResultsList()) {
      SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
      return alternative.getTranscript();
    }
    return null;
  }

  private static void printResult(DialogFlowClient queryResult) {
    System.out.println("====================");
    System.out.format("Intent Display Name: %s\n", queryResult.getIntentName());
    System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
    System.out.format(
        "Detected Intent: %s (confidence: %f)\n",
        queryResult.getIntentName(), queryResult.getIntentConfidence());
    System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
  }
}
