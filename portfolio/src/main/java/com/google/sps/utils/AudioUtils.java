package com.google.sps.utils;

// Imports the Google Cloud client library
import com.google.api.gax.rpc.BidiStream;
import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.StreamingDetectIntentRequest;
import com.google.cloud.dialogflow.v2.StreamingDetectIntentResponse;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * DialogFlow API Detect Intent sample with audio files processes as an audio stream.
 */
public class AudioUtils {

  static SessionName session = SessionName.of("fair-syntax-280601", "1");

  public static QueryResult detectIntentStream(ByteString bytestring, String languageCode) {
    QueryResult queryResult = null;

    try (SessionsClient sessionsClient = SessionsClient.create()) {
      InputAudioConfig inputAudioConfig = InputAudioConfig.newBuilder()
          .setAudioEncoding(AudioEncoding.AUDIO_ENCODING_LINEAR_16)
          .setLanguageCode(languageCode)
          .setSampleRateHertz(44100)
          .build();
      QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

      BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> bidiStream = 
        makeBidiStream(sessionsClient, queryInput, bytestring);

      for (StreamingDetectIntentResponse response : bidiStream) {
        queryResult = response.getQueryResult();
        printResult(queryResult);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return queryResult;
  }

  public static void detectIntentStream(String projectId, String audioFilePath, String sessionId) {
    try (SessionsClient sessionsClient = SessionsClient.create()) {
      InputAudioConfig inputAudioConfig = InputAudioConfig.newBuilder()
          .setAudioEncoding(AudioEncoding.AUDIO_ENCODING_LINEAR_16)
          .setLanguageCode("en-US") // languageCode = "en-US"
          .setSampleRateHertz(16000) // sampleRateHertz = 16000
          .build();
      QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

      BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> bidiStream =
          sessionsClient.streamingDetectIntentCallable().call();

      bidiStream.send(StreamingDetectIntentRequest.newBuilder()
          .setSession(session.toString())
          .setQueryInput(queryInput)
          .build());

      try (FileInputStream audioStream = new FileInputStream(audioFilePath)) {
        byte[] buffer = new byte[4096];
        int bytes;
        while ((bytes = audioStream.read(buffer)) != -1) {
          bidiStream.send(
              StreamingDetectIntentRequest.newBuilder()
                  .setInputAudio(ByteString.copyFrom(buffer, 0, bytes))
                  .build());
        }
      }
      bidiStream.closeSend();

      for (StreamingDetectIntentResponse response : bidiStream) {
        QueryResult queryResult = response.getQueryResult();
        printResult(queryResult);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> makeBidiStream(SessionsClient sessionsClient, QueryInput queryInput, ByteString bytestring) {
    BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse> bidiStream =
          sessionsClient.streamingDetectIntentCallable().call();
      bidiStream.send(StreamingDetectIntentRequest.newBuilder()
          .setSession(session.toString())
          .setQueryInput(queryInput)
          .build());
      bidiStream.send(
          StreamingDetectIntentRequest.newBuilder()
              .setInputAudio(bytestring)
              .build());
      bidiStream.closeSend();
      return bidiStream;
  }

  private static void printResult(QueryResult queryResult) {
    System.out.println("====================");
    System.out.format("Intent Display Name: %s\n", queryResult.getIntent().getDisplayName());
    System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
    System.out.format("Detected Intent: %s (confidence: %f)\n",
      queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
    System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
  }
}