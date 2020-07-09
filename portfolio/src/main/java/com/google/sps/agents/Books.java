package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
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

      if (resultsReturned > 0) {
        // Delete stored BookQuery, Book results, totalResults, resultsReturned
        deleteAllStoredBookInformation();
        // Store BookQuery, Book results, totalResults, resultsReturned
        storeBooks(results);
        storeBookQuery(query);
        storeIndices();

        // Get output information from stored Book results

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

  private void storeBooks(ArrayList<Book> books) {
    for (int i = 0; i < books.size(); ++i) {
      long timestamp = System.currentTimeMillis();
      Book currentBook = books.get(i);
      Entity bookEntity = new Entity("Book");
      bookEntity.setProperty("title", currentBook.getTitle());
      bookEntity.setProperty("publishedDate", currentBook.getPublishedDate());
      bookEntity.setProperty("authors", currentBook.getAuthors());
      bookEntity.setProperty("averageRating", currentBook.getRating());
      bookEntity.setProperty("infoLink", currentBook.getInfoLink());
      bookEntity.setProperty("thumbnailLink", currentBook.getThumbnailLink());
      bookEntity.setProperty("buyLink", currentBook.getBuyLink());
      bookEntity.setProperty("embeddable", currentBook.getEmbeddable());
      bookEntity.setProperty("isbn", currentBook.getISBN());
      bookEntity.setProperty("timestamp", timestamp);

      Text textDescription = new Text(currentBook.getDescription());
      Text textSnippet = new Text(currentBook.getTextSnippet());
      bookEntity.setProperty("description", textDescription);
      bookEntity.setProperty("textSnippet", textSnippet);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(bookEntity);
    }
  }

  private void storeBookQuery(BookQuery query) {
    long timestamp = System.currentTimeMillis();
    Entity bookQueryEntity = new Entity("BookQuery");
    bookQueryEntity.setProperty("userInput", query.getUserInput());
    bookQueryEntity.setProperty("type", query.getType());
    bookQueryEntity.setProperty("categories", query.getCategories());
    bookQueryEntity.setProperty("authors", query.getAuthors());
    bookQueryEntity.setProperty("title", query.getTitle());
    bookQueryEntity.setProperty("order", query.getOrder());
    bookQueryEntity.setProperty("language", query.getLanguage());
    bookQueryEntity.setProperty("queryString", query.getQueryString());
    bookQueryEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(bookQueryEntity);
  }

  private void storeIndices() {
    long timestamp = System.currentTimeMillis();
    Entity indicesEntity = new Entity("Indices");
    indicesEntity.setProperty("startIndex", this.startIndex);
    indicesEntity.setProperty("resultsReturned", this.resultsReturned);
    indicesEntity.setProperty("totalResults", this.totalResults);
    indicesEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(indicesEntity);
  }

  private void deleteAllStoredBookInformation() {
    deleteFromDatastore("BookQuery");
    deleteFromDatastore("Book");
    deleteFromDatastore("Indices");
  }

  private void deleteFromDatastore(String entityName) {
    Query query = new Query(entityName).addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      datastore.delete(entity.getKey());
    }
  }

  /*private String getDisplayTableFromArrayList(ArrayList<Book> books, int numToDisplay) {
    for (int i = 0; i < numToDisplay; ++i) {
        Book book = getB
        if (all == false && i >= userQuantity){
            break;
        }
        else{
            String userComment = (String) entity.getProperty("comment");
            String name = (String) entity.getProperty("name");
            String userEmail = (String) entity.getProperty("email");

            Comment comment = new Comment(name, userComment, userEmail);
            comments.add(comment);
        }
        ++i;
    }

    // Send the JSON as the response
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }*/
}
