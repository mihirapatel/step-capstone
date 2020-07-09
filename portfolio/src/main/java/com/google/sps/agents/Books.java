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
  private int totalResults;
  private int resultsReturned;

  public Books(String intentName, String userInput, Map<String, Value> parameters)
      throws IOException {
    this.intentName = intentName;
    this.userInput = userInput;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) throws IOException {
    if (intentName.equals("search")) {

      // Create new BookQuery request, sets startIndex at 0
      BookQuery query = BookQuery.createBookQuery(this.userInput, parameters);

      // Retrieve books
      this.startIndex = 0;
      ArrayList<Book> results = BookUtils.getRequestedBooks(query, startIndex);
      this.totalResults = BookUtils.getTotalVolumesFound(query, startIndex);
      this.resultsReturned = results.size();
      System.out.println(totalResults);
      System.out.println(resultsReturned);

      // Make output information

      // Delete stored BookQuery, Book results, totalResults, resultsReturned
      // Store BookQuery, Book results, totalResults, resultsReturned
    } else if (intentName.equals("more")) {
      // Load BookQuery, totalResults, resultsReturned

      // Increment startIndex
      // Retrieve books
      // Make output information

      // Delete stored BookQuery, Book results, totalResults, resultsReturned
      // Store BookQuery, Book results, totalResults, resultsReturned
    } else if (intentName.equals("about")) {
      // Load Book results, totalResults, resultsReturned

      // Get information about requested Book
      // Make output information

      // Don't change any stored information
    } else if (intentName.equals("preview")) {
      // Load Book results, totalResults, resultsReturned

      // Get information about requested Book
      // Make output information

      // Don't change any stored information
    } else if (intentName.equals("results")) {
      // Load Book results, totalResults, resultsReturned

      // Set display and fulfillment for original table

      // Don't change any stored information
    }
  }

  @Override
  public String getOutput() {
    return this.output;
  }

  @Override
  public String getDisplay() {
    this.display = "hello";
    return this.display;
  }

  @Override
  public String getRedirect() {
    return this.redirect;
  }
}
