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

  private static String PROJECT_ID = "mihira-step-2020-3";
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
  public void testByteStringAudioInput() {
    String audioFilePath = "resources/book_a_room.wav";
    try {
      File file = new File(audioFilePath);
      byte[] bytesArray = new byte[(int) file.length()];
      FileInputStream fis = new FileInputStream(file);
      fis.read(bytesArray); // read file into bytes[]
      fis.close();

      ByteString bytestring = ByteString.copyFrom(bytesArray);

      AudioUtils.detectIntentStream(bytestring, 16000);

      String output = bout.toString();

      assertThat(output).contains("book");
      assertThat(output).contains("a");
      assertThat(output).contains("room");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
