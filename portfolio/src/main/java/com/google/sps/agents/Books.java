package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import com.google.sps.data.Book;
import com.google.sps.data.BookQuery;
import com.google.sps.utils.BookUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/** Books Agent */
public class Books implements Agent {
  private final String intentName;
  private final String userInput;
  private String output;
  private String display;
  private String redirect;
  private BookQuery query;
  private int startIndex;

  public Books(String intentName, String userInput, Map<String, Value> parameters)
      throws IOException {
    this.intentName = intentName;
    this.userInput = userInput;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) throws IOException {
    if (intentName.equals("search")) {
      this.startIndex = 0;
      BookQuery query = BookQuery.createBookQuery(this.userInput, parameters);
      ArrayList<Book> results = BookUtils.getRequestedBooks(query, startIndex);
    }
  }

  @Override
  public String getOutput() {
    return this.output;
  }

  @Override
  public String getDisplay() {
    return this.display;
  }

  @Override
  public String getRedirect() {
    return this.redirect;
  }
}
