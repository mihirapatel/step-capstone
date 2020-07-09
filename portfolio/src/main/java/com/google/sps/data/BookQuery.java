/*
 * Copyright 2018 Google Inc.
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

// Imports the Google Cloud client library
import com.google.api.services.books.*;
import com.google.gson.*;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.utils.AgentUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * A BookQuery object contains parameters neccessary to make a call to the Google Books API that
 * will match users' requests to Dialogflow
 *
 * <p>A BookQuery object is only created by createBookQuery() function, ensuring that any BookQuery
 * object is only created with valid parameters. Specifically, it ensures that all BookQuery objects
 * have a valid userInput property, since this is the only field required to make a request to the
 * Google Books API.
 */
public class BookQuery {

  private String userInput;
  private String type;
  private String categories;
  private String authors;
  private String title;
  private String order;
  private String language;
  private String queryString;

  /**
   * Creates a BookQuery object the detected parameters from Dialogflow that will be used to
   * retrieve appropriate Volumes from the Google Books API that match a user input, or throws an
   * exception if the userInput is empty
   *
   * @param String userInput
   * @param parameters parameter Map from Dialogflow
   * @return BookQuery object
   */
  public static BookQuery createBookQuery(String userInput, Map<String, Value> parameters)
      throws IOException {
    if (userInput == null || userInput.isEmpty()) {
      throw new IOException();
    } else {
      BookQuery bookQuery = new BookQuery(userInput, parameters);
      return bookQuery;
    }
  }

  /**
   * Private BookQuery constructor, can only be called by createBookQuery() if user input string is
   * valid
   *
   * <p>If Dialogflow does not detect certain parameters for a user request, then BookQuery
   * properties will remain null
   *
   * @param userInput detected input string
   * @param parameters parameter Map from Dialogflow
   */
  private BookQuery(String userInput, Map<String, Value> parameters) {
    this.userInput = userInput;
    setType(parameters);
    setCategories(parameters);
    setAuthors(parameters);
    setTitle(parameters);
    setOrder(parameters);
    setLanguage(parameters);
    setQueryString(parameters);
  }

  private void setType(Map<String, Value> parameters) {
    if (parameters.get("type") != null && !parameters.get("type").getStringValue().isEmpty()) {
      this.type = parameters.get("type").getStringValue();
    }
  }

  private void setCategories(Map<String, Value> parameters) {
    if (parameters.get("categories") != null
        && !parameters.get("categories").getStringValue().isEmpty()) {
      this.categories = parameters.get("categories").getStringValue();
    }
  }

  private void setAuthors(Map<String, Value> parameters) {
    if (parameters.get("authors") != null) {
      ArrayList<Value> valueList =
          new ArrayList<Value>(parameters.get("authors").getListValue().getValuesList());
      ArrayList<String> authorList = new ArrayList<String>();
      for (int i = 0; i < valueList.size(); ++i) {
        Struct personStruct = valueList.get(i).getStructValue();
        Map<String, Value> personFields = personStruct.getFieldsMap();
        if (personFields.get("name") != null
            && !personFields.get("name").getStringValue().isEmpty()) {
          String authorName =
              String.join("+", personFields.get("name").getStringValue().split(" "));
          authorList.add("inauthor:\"" + authorName + "\'");
        }
      }
      if (!authorList.isEmpty()) {
        this.authors = String.join("+", authorList);
      }
    }
  }

  private void setTitle(Map<String, Value> parameters) {
    if (parameters.get("title") != null && !parameters.get("title").getStringValue().isEmpty()) {
      this.title =
          "intitle:\""
              + String.join("+", parameters.get("title").getStringValue().split(" "))
              + "\"";
    }
  }

  private void setOrder(Map<String, Value> parameters) {
    if (parameters.get("order") != null && !parameters.get("order").getStringValue().isEmpty()) {
      this.order = parameters.get("order").getStringValue();
    }
  }

  private void setLanguage(Map<String, Value> parameters) {
    if (parameters.get("language") != null
        && !parameters.get("language").getStringValue().isEmpty()) {
      String languageName = parameters.get("language").getStringValue();
      this.language = AgentUtils.getLanguageCode(languageName);
    }
  }

  private void setQueryString(Map<String, Value> parameters) {
    String queryText = String.join("+", this.userInput.split(" "));
    if (this.authors != null) {
      queryText += "+" + this.authors;
    }
    if (this.title != null) {
      queryText += "+" + this.title;
    }
    this.queryString = queryText;
  }

  public String getTitle() {
    return this.title;
  }

  public String getAuthors() {
    return this.authors;
  }

  public String getType() {
    return this.type;
  }

  public String getCategories() {
    return this.categories;
  }

  public String getOrder() {
    return this.order;
  }

  public String getLanguage() {
    return this.language;
  }

  public String getUserInput() {
    return this.userInput;
  }

  public String getQueryString() {
    System.out.println(this.queryString);
    return this.queryString;
  }
}
