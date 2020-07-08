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
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volume.VolumeInfo.IndustryIdentifiers;
import com.google.gson.*;
import java.util.ArrayList;

/**
 * A Book object contains specific properties about a Volume object that will be outputted to the
 * user
 */
public class Book {

  private String title;
  private ArrayList<String> authors;
  private String publishedDate;
  private String description;
  private String averageRating;
  private String infoLink;
  private String thumbnailLink;
  private String buyLink;
  private Boolean embeddable;
  private String isbn;
  private String textSnippet;

  /**
   * Creates a Book object, only if the Volume object is valid and has a title
   *
   * @param volume Volume Object
   * @return Book object
   */
  public static Book create(Volume volume) throws IllegalArgumentException {
    if (volume == null || !hasValidTitle(volume)) {
      throw new IllegalArgumentException();
    } else {
      Book book = new Book(volume);
      return book;
    }
  }

  /**
   * Private Book constructor, can only be called by create() if Volume parameter is not null
   *
   * @param volume Volume Object
   */
  private Book(Volume volume) {
    setTitle(volume);
    setAuthorList(volume);
    setPublishedDate(volume);
    setDescription(volume);
    setRating(volume);
    setInfoLink(volume);
    setThumbnailLink(volume);
    setBuyLink(volume);
    setEmbeddable(volume);
    setISBN(volume);
    setTextSnippet(volume);
  }

  private void setTitle(Volume volume) {
    this.title = volume.getVolumeInfo().getTitle();
  }

  private void setAuthorList(Volume volume) {
    try {
      this.authors = new ArrayList<String>(volume.getVolumeInfo().getAuthors());
      if (this.authors == null) {
        this.authors = new ArrayList<String>();
      }
    } catch (Exception e) {
      this.authors = new ArrayList<String>();
    }
  }

  private void setPublishedDate(Volume volume) {
    try {
      this.publishedDate = volume.getVolumeInfo().getPublishedDate();
      if (this.publishedDate == null) {
        this.publishedDate = "";
      }
    } catch (Exception e) {
      this.publishedDate = "";
    }
  }

  private void setDescription(Volume volume) {
    try {
      this.description = volume.getVolumeInfo().getDescription();
      if (this.description == null) {
        this.description = "";
      }
    } catch (Exception e) {
      this.description = "";
    }
  }

  private void setRating(Volume volume) {
    try {
      this.averageRating = String.valueOf(volume.getVolumeInfo().getAverageRating());
      if (this.averageRating == null) {
        this.averageRating = "";
      }
    } catch (Exception e) {
      this.averageRating = "";
    }
  }

  private void setInfoLink(Volume volume) {
    try {
      this.infoLink = volume.getVolumeInfo().getInfoLink();
      if (this.infoLink == null) {
        this.infoLink = "";
      }
    } catch (Exception e) {
      this.infoLink = "";
    }
  }

  private void setThumbnailLink(Volume volume) {
    try {
      this.thumbnailLink = volume.getVolumeInfo().getImageLinks().getThumbnail();
      if (this.thumbnailLink == null) {
        this.thumbnailLink = "";
      }
    } catch (Exception e) {
      this.thumbnailLink = "";
    }
  }

  private void setBuyLink(Volume volume) {
    try {
      this.buyLink = volume.getSaleInfo().getBuyLink();
      if (this.buyLink == null) {
        this.buyLink = "";
      }
    } catch (Exception e) {
      this.buyLink = "";
    }
  }

  private void setEmbeddable(Volume volume) {
    try {
      this.embeddable = volume.getAccessInfo().getEmbeddable();
      if (this.embeddable == null) {
        this.embeddable = false;
      }
    } catch (Exception e) {
      this.embeddable = false;
    }
  }

  private void setISBN(Volume volume) {
    try {
      ArrayList<IndustryIdentifiers> industryIdentifiers =
          new ArrayList<IndustryIdentifiers>(volume.getVolumeInfo().getIndustryIdentifiers());
      for (int i = 0; i < industryIdentifiers.size(); ++i) {
        if (industryIdentifiers.get(i).getType().contains("ISBN")) {
          this.isbn = industryIdentifiers.get(i).getIdentifier();
        }
      }
      if (this.isbn == null) {
        this.isbn = "";
      }
    } catch (Exception e) {
      this.isbn = "";
    }
  }

  private void setTextSnippet(Volume volume) {
    try {
      this.textSnippet = volume.getSearchInfo().getTextSnippet();
      if (this.textSnippet == null) {
        this.textSnippet = "";
      }
    } catch (Exception e) {
      this.textSnippet = "";
    }
  }

  public String getTitle() {
    return this.title;
  }

  public ArrayList<String> getAuthors() {
    return this.authors;
  }

  public String getPublishedDate() {
    return this.publishedDate;
  }

  public String getDescription() {
    return this.description;
  }

  public String getRating() {
    return this.averageRating;
  }

  public String getInfoLink() {
    return this.infoLink;
  }

  public String getThumbnailLink() {
    return this.thumbnailLink;
  }

  public String getBuyLink() {
    return this.buyLink;
  }

  public Boolean getEmbeddable() {
    return this.embeddable;
  }

  public String getISBN() {
    return this.isbn;
  }

  public String getTextSnippet() {
    return this.textSnippet;
  }

  public static boolean hasValidTitle(Volume volume) {
    try {
      if (volume.getVolumeInfo().getTitle() != null) {
        return true;
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }
}
