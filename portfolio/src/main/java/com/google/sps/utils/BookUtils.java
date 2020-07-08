package com.google.sps.utils;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.books.*;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.google.gson.*;
import com.google.sps.data.Book;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BookUtils {

  String filter = "";
  String langRestrict = "";
  String orderBy = "";
  String printType = "";
  String q = "";

  public static void getCurl() throws Exception {
    // The fluent API relieves the user from having to deal with manual
    // deallocation of system resources at the cost of having to buffer
    // response content in memory in some cases.
    String apiKey =
        new String(
            Files.readAllBytes(
                Paths.get(BookUtils.class.getResource("/files/apikey.txt").getFile())));

    GsonFactory gsonFactory = new GsonFactory();
    UrlFetchTransport transport = new UrlFetchTransport();

    Books books =
        new Books.Builder(transport, gsonFactory, null)
            .setApplicationName("Test Application name")
            .setGoogleClientRequestInitializer(new BooksRequestInitializer(apiKey))
            .build();

    String bookQuery = String.join("+", "the fault in our stars".split(" "));

    try {
      List list = books.volumes().list(bookQuery);
      list.setMaxResults(Long.valueOf(10));
      list.setStartIndex(Long.valueOf(0));
      // list.setFields("totalItems,items(volumeInfo(title,authors,publishedDate,description,averageRating,infoLink,imageLinks/thumbnail),id)");

      Volumes volumes = list.execute();
      ArrayList<Book> results = volumesToBookList(volumes);

      System.out.println(results);
    } catch (IOException e) {
      System.out.println("Failed to retrieve volumes from Books API.");
    }
    /*
    String bookQuery = String.join("+", "the fault in our stars".split(" "));

    String filter = "";
    String langRestrict = "";
    String orderBy = "";
    String printType = "";
    String q = "";

    int maxResults = 10;
    int startIndex = 0;
    System.out.println(bookQuery);
    String bookCurl = "https://www.googleapis.com/books/v1/volumes?q="
            + bookQuery
            + "&key="
            + apiKey;
    HashMap<String, Object> responseMap =
        new Gson()
            .fromJson(
                Request.Get(bookCurl).execute().returnContent().toString(), HashMap.class);
    //System.out.println(responseMap);
    System.out.println(responseMap.keySet());
    System.out.println(responseMap.get("items").getClass());
    //String itemsList = responseMap.get("items").toString();
    //System.out.println(itemsList);

    int numResults = (int)responseMap.get("totalItems");

    //ArrayList<String> itemsList = new Gson().fromJson(responseMap.get("items").toString(), ArrayList.class);
    System.out.println(itemsList);*/
    /*
    for (int i = 0; i < itemsList.size(); ++i){
        HashMap<String, Object> itemMap = new Gson().fromJson(itemsList.get(i).toString(), HashMap.class);
        HashMap<String, Object> volumeInfo = new Gson().fromJson(itemMap.get("volumeInfo").toString(), HashMap.class);
        String title = volumeInfo.get("title").toString();

        ArrayList<String> authors = new Gson().fromJson(volumeInfo.get("authors").toString(), ArrayList.class);;
        String publishedDate = volumeInfo.get("publishedDate").toString();
        String description = volumeInfo.get("description").toString();
        String averageRating = volumeInfo.get("averageRating").toString();
        String infoLink = volumeInfo.get("infoLink").toString();

        String isbn13 = "";
        String isbn10 = "";
        //TODO: check if null first
        if (volumeInfo.get("industryIdentifiers") != null){
            ArrayList<String> industryIdentifiers = new Gson().fromJson(volumeInfo.get("industryIdentifiers").toString(), ArrayList.class);
            for (int j = 0; j < industryIdentifiers.size(); ++j){
                HashMap<String, String> identifierMap = new Gson().fromJson(industryIdentifiers.get(j).toString(), HashMap.class);
                if (identifierMap.get("type").toString().equals("ISBN_13")){
                    isbn13 = identifierMap.get("identifier").toString();
                }
                else if (identifierMap.get("type").toString().equals("ISBN_10")){
                    isbn10 = identifierMap.get("identifier").toString();
                }
            }
        }

        String thumbnailLink = "";
        if (volumeInfo.get("imageLinks") != null){
            HashMap<String, String> imageLinks = new Gson().fromJson(volumeInfo.get("imageLinks").toString(), HashMap.class);
            if (imageLinks.containsKey("thumbnail")){
                thumbnailLink = imageLinks.get("thumbnail").toString();
            }
        }

        HashMap<String, String> saleInfo = new Gson().fromJson(itemMap.get("saleInfo").toString(), HashMap.class);
        String buyLink = saleInfo.get("buyLink").toString();

        HashMap<String, String> accessInfo = new Gson().fromJson(itemMap.get("accessInfo").toString(), HashMap.class);
        String embeddable = accessInfo.get("embeddable").toString();

        HashMap<String, String> searchInfo = new Gson().fromJson(itemMap.get("searchInfo").toString(), HashMap.class);
        String textSnippet = searchInfo.get("textSnippet").toString();

        System.out.println(title);
        System.out.println(authors);
        System.out.println(description);
        System.out.println(averageRating);
        System.out.println(infoLink);
        System.out.println(thumbnailLink);
        System.out.println(buyLink);
        System.out.println(embeddable);
        System.out.println(textSnippet);
        System.out.println(" ");
    }*/
  }

  private static ArrayList<Book> volumesToBookList(Volumes volumes) {
    if (volumes != null && volumes.getItems() != null) {
      ArrayList<Volume> vols = new ArrayList<Volume>(volumes.getItems());
      ArrayList<Book> books = new ArrayList<Book>();
      for (Volume vol : vols) {
        try {
          Book book = Book.create(vol);
          System.out.println(book.getTitle());
          System.out.println(book.getAuthors());
          System.out.println(book.getPublishedDate());
          System.out.println(book.getDescription());
          System.out.println(book.getRating());
          System.out.println(book.getInfoLink());
          System.out.println(book.getThumbnailLink());
          System.out.println(book.getBuyLink());
          System.out.println(book.getEmbeddable());
          System.out.println(book.getISBN());
          books.add(book);
        } catch (IllegalArgumentException e) {
          continue;
        }
      }
      return books;
    }
    return new ArrayList<>();
  }
}
