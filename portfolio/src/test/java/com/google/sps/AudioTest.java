package com.google.sps.servlets;

import static com.google.common.truth.Truth.assertThat;

import com.google.sps.utils.AudioUtils;
import java.io.ByteArrayOutputStream;
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
}