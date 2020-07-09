package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.gson.Gson;
import com.google.protobuf.Value;
import com.google.sps.data.Book;
import com.google.sps.data.BookQuery;
import com.google.sps.utils.BookUtils;
import com.google.sps.utils.BooksMemoryUtils;
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

      if (resultsReturned > 0) {
        // Delete stored BookQuery, Book results, totalResults, resultsReturned
        BooksMemoryUtils.deleteAllStoredBookInformation();

        // Store BookQuery, Book results, totalResults, resultsReturned
        BooksMemoryUtils.storeBooks(results);
        BooksMemoryUtils.storeBookQuery(query);
        BooksMemoryUtils.storeIndices(this.startIndex, this.totalResults, this.resultsReturned);

        // Get output information from stored Book results
        this.display = bookListToString(BooksMemoryUtils.getStoredBooksList(resultsReturned));

        // Set fulfillment
        this.output = "Here's what I found.";
      } else {
        this.output = "I couldn't find any results. Can you try again?";
      }

    } else if (intentName.equals("more")) {
      // Load BookQuery, totalResults, resultsReturned
      // Increment startIndex
      // Retrieve books

      // Delete stored BookQuery, Book results, totalResults, resultsReturned
      // Store BookQuery, Book results, totalResults, resultsReturned

      // Get output information from stored Book results

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

      // Get output information from stored Book results

      // Don't change any stored information
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

  private String bookListToString(ArrayList<Book> books) {
    Gson gson = new Gson();
    return gson.toJson(books);
  }
}
