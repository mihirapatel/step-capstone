/*
 * Copyright 2019 Google LLC
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

package com.google.sps.utils;

import com.google.api.services.books.v1.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.gson.*;
import com.google.sps.data.Book;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains methods to help set-up the AI-ssistant for demos by populating datastore with
 * book information and "likes" of test accounts.
 */
public class BooksSetUpHelper {
  private static Logger log = LoggerFactory.getLogger(BooksSetUpHelper.class);

  /**
   * This function sets up Datastore with liked books for test users, for demo purposes. Input is
   * read from the file likedbooksloader.txt, which is formatted as followed: #volume1Id
   * emailToLikeVolume1, idToLikeVolume1 emailToLikeVolume1, idToLikeVolume1 #volume2Id
   * emailToLikeVolume2, idToLikeVolume2.
   *
   * @param datastore DatastoreService instance used to access Book info from database
   */
  public static void setUpBookLikes(DatastoreService datastore) throws FileNotFoundException {
    BufferedReader br =
        new BufferedReader(
            new FileReader(
                BooksSetUpHelper.class.getResource("/files/likedbooksloader.txt").getFile()));
    BookUtils bookHelper = new BookUtils();
    try {
      String line;
      Book book;
      // Get first book
      if ((line = br.readLine()) != null) {
        book = getBookFromLine(line, bookHelper);
        // Read following lines
        while ((line = br.readLine()) != null) {
          // Line is an email to like book for
          if (!line.startsWith("#")) {
            if (book != null) {
              // Reset liked book
              String[] splitLine = line.split(",");
              String userEmail = splitLine[0];
              String userID = splitLine[1];
              BooksMemoryUtils.unlikeBook(book, userID, userEmail, datastore);
              BooksMemoryUtils.likeBook(book, userID, userEmail, datastore);
            }
          } else { // Line is a new volume id
            book = getBookFromLine(line, bookHelper);
          }
        }
        br.close();
      }
    } catch (IOException e) {
      log.error("Could not like books in set up");
    }
  }

  private static Book getBookFromLine(String line, BookUtils bookHelper) throws IOException {
    String id = line.substring(1);
    return bookHelper.getBook(id);
  }
}
