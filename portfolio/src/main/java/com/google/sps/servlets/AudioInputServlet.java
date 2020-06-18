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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletInputStream;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.ByteString;
import com.google.sps.data.Output;
import com.google.gson.Gson;
import com.google.sps.utils.AudioUtils;

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
    QueryResult result = AudioUtils.detectIntentStream(bytestring);

    if (result == null) {
      response.getWriter().write(new Gson().toJson(null));
      return;
    }

    // Retrieve detected input and AI response from DialogFlow result.
    String inputDetected = result.getQueryText();
    String fulfillment = result.getFulfillmentText();
    inputDetected = inputDetected.equals("") ? " (null) " : inputDetected;
    fulfillment = fulfillment.equals("") ? " (null) " : fulfillment;

    // Create output object
    Output output = new Output(inputDetected, fulfillment);
    String json = new Gson().toJson(output);
    response.getWrite().write(json);
    // Convert to JSON string
  }
}
