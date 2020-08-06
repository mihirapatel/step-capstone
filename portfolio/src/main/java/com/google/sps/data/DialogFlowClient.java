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

package com.google.sps.data;

import com.google.api.gax.rpc.BidiStream;
import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.StreamingDetectIntentRequest;
import com.google.cloud.dialogflow.v2.StreamingDetectIntentResponse;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.protobuf.ByteString;
import com.google.protobuf.Value;
import java.util.Map;

public class DialogFlowClient {

  static SessionName session = SessionName.of("mihira-step-2020-3", "1");
  QueryResult queryResult;

  /**
   * Dialogflow Client constructor for text inputs
   *
   * @param text Input text to dialogflow
   * @param languageCode Two-letter representation of input language
   * @param sessionsClient Instance of the current dialogflow session
   */
  public DialogFlowClient(String text, String languageCode, SessionsClient sessionsClient) {
    TextInput.Builder textInput =
        TextInput.newBuilder().setText(text).setLanguageCode(languageCode);
    QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
    DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
    queryResult = response.getQueryResult();
  }

  /**
   * Dialogflow Client constructor for audio inputs
   *
   * @param sessionsClient Instance of the current dialogflow session
   * @param audioByteString ByteString containing the audio input recording
   * @param sampleRate Sample hertz frequency of the audio byte string recording
   */
  public DialogFlowClient(
      SessionsClient sessionsClient, ByteString audioBytestring, int sampleRate) {
    InputAudioConfig inputAudioConfig =
        InputAudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.AUDIO_ENCODING_LINEAR_16)
            .setLanguageCode("en-US")
            .setSampleRateHertz(sampleRate)
            .build();
    QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

    BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> bidiStream =
        makeBidiStream(sessionsClient, queryInput, audioBytestring);
    for (StreamingDetectIntentResponse response : bidiStream) {
      queryResult = response.getQueryResult();
    }
  }

  /**
   * Creates streaming ability to handle audio input streams to Dialogflow.
   *
   * @param sessionsClient Instance of the current dialogflow session
   * @param queryInput Configured audio query to handle input stream
   * @param audioBytestring ByteString containing the input audio stream
   * @return BidiStream that contains the stream of audio data in a readable format
   */
  private static BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse>
      makeBidiStream(
          SessionsClient sessionsClient, QueryInput queryInput, ByteString audioBytestring) {
    BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> bidiStream =
        sessionsClient.streamingDetectIntentCallable().call();
    bidiStream.send(
        StreamingDetectIntentRequest.newBuilder()
            .setSession(session.toString())
            .setQueryInput(queryInput)
            .build());
    bidiStream.send(
        StreamingDetectIntentRequest.newBuilder().setInputAudio(audioBytestring).build());
    bidiStream.closeSend();
    return bidiStream;
  }

  public String getQueryText() {
    return queryResult.getQueryText();
  }

  public String getIntentName() {
    return queryResult.getIntent().getDisplayName();
  }

  public float getIntentConfidence() {
    return queryResult.getIntentDetectionConfidence();
  }

  public String getFulfillmentText() {
    return queryResult.getFulfillmentText();
  }

  public Map<String, Value> getParameters() {
    return queryResult.getParameters().getFieldsMap();
  }

  public Boolean getAllRequiredParamsPresent() {
    return queryResult.getAllRequiredParamsPresent();
  }
}
