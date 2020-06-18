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
import java.util.*;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.protobuf.ByteString;
import com.google.sps.utils.AudioUtils;

/** Servlet that takes in audio stream and retrieves 
 ** user input string to display. */

@WebServlet("/audio-input")
public class AudioInputServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    System.out.println(request);
    InputStream inputStream = request.getInputStream();
    ByteString bytestring =  ByteString.readFrom(inputStream);
    AudioUtils.detectIntentStream(bytestring);

  }
}