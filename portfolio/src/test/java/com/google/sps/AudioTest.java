package com.google.sps.servlets;

import static com.google.common.truth.Truth.assertThat;

import com.google.protobuf.ByteString;
import com.google.sps.utils.AudioUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class AudioTest {

  private ByteArrayOutputStream bout;

  private static String PROJECT_ID = "fair-syntax-280601";
  private static String SESSION_ID = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testStreamingDetectIntentCallable() {
    String audioFilePath = "resources/book_a_room.wav";
    AudioUtils.detectIntentStream(
            PROJECT_ID, audioFilePath, SESSION_ID);

    String output = bout.toString();

    assertThat(output).contains("book");
    assertThat(output).contains("a");
    assertThat(output).contains("room");
  }

  @Test 
  public void testByteStringAudioInput() {
    String audioFilePath = "resources/book_a_room.wav";
    try {
        File file = new File(audioFilePath);
        byte[] bytesArray = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); //read file into bytes[]
        fis.close();

        ByteString bytestring = ByteString.copyFrom(bytesArray);

        AudioUtils.detectIntentStream(bytestring);

        String output = bout.toString();

        assertThat(output).contains("book");
        assertThat(output).contains("a");
        assertThat(output).contains("room");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}