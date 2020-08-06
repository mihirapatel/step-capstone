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

package com.google.sps.data;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.Photo;
import com.google.sps.utils.BooksAgentHelper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A Friend object contains specific properties about a Person object that will be used to create
 * output display and text.
 *
 * <p>A Friend object is only created by createFriend() function, ensuring that any Friend object is
 * only created with valid parameters and that all Friend objects have at least one email address.
 */
public class Friend implements Serializable {

  private String name;
  private ArrayList<String> emailAddresses;
  private String photoUrl = "images/blankAvatar.png";
  private String resourceName;

  /**
   * Creates a Friend object from a valid Person object that will be used to access Book likes or
   * throws an exception if the Person object is invalid or missing an email address.
   *
   * @param person Person Object
   * @return Friend object
   */
  public static Friend createFriend(Person person) throws IOException {
    if (person == null || !hasValidParameters(person)) {
      throw new IOException();
    } else {
      Friend friend = new Friend(person);
      return friend;
    }
  }

  /**
   * Private Friend constructor, can only be called by createFriend() if Person parameter is not
   * null and Person object has a valid email. The constructor will set neccessary fields from the
   * Person object.
   *
   * <p>If Person object is missing any other properties, the properties will be set to an empty
   * String, except for photoUrl, which will be a link to a blank avatar.
   *
   * @param person Person Object
   */
  private Friend(Person person) {
    setName(person);
    setEmailAddresses(person);
    setPhotoUrl(person);
    setResourceName(person);
  }

  /**
   * Public Friend constructor, used for testing purposes. The constructor will set the following
   * fields from the parameters. The rest of the properties will be set to an Empty String.
   *
   * @param name String title of book
   * @param emails list of email addresses
   */
  public Friend(String name, ArrayList<String> emails) {
    this.name = name;
    this.emailAddresses = setEmailsFromList(emails);
    this.resourceName = "";
  }

  /**
   * Public Friend constructor called when retrieving all attributes of a valid Friend object from
   * the frontend. Properties of Friend constructor are set with the parameters.
   *
   * @param name name of Friend
   * @param emails emails of Friend
   * @param photoUrl photo of Friend
   */
  public Friend(String name, ArrayList<String> emails, String photoUrl, String resourceName) {
    this(name, emails);
    this.photoUrl = photoUrl;
    this.resourceName = resourceName;
  }

  /**
   * Two friends are considered equal if all of their email addresses and contact name are the same.
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof Friend)) {
      return false;
    }
    Friend otherFriend = (Friend) object;
    return (otherFriend.getName().equals(this.name)
        && otherFriend.getEmails().equals(this.emailAddresses));
  }

  @Override
  public int hashCode() {
    return BooksAgentHelper.listToJson(this.emailAddresses).hashCode();
  }

  private void setName(Person person) {
    List<Name> names = person.getNames();
    if (names != null && !names.isEmpty()) {
      this.name = person.getNames().get(0).getDisplayName();
    } else {
      this.name = "";
    }
  }

  private void setEmailAddresses(Person person) {
    List<EmailAddress> emails = person.getEmailAddresses();
    ArrayList<String> stringEmails = new ArrayList<String>();
    if (emails != null && emails.size() > 0) {
      for (EmailAddress email : emails) {
        if (!email.getValue().isEmpty()) {
          stringEmails.add(email.getValue().toLowerCase());
        }
      }
    }
    this.emailAddresses = stringEmails;
  }

  private void setPhotoUrl(Person person) {
    List<Photo> photos = person.getPhotos();
    if (photos != null && photos.size() > 0 && !photos.get(0).isEmpty()) {
      this.photoUrl = photos.get(0).getUrl();
    }
  }

  private void setResourceName(Person person) {
    this.resourceName = person.getResourceName();
  }

  public String getName() {
    return this.name;
  }

  public ArrayList<String> getEmails() {
    return this.emailAddresses;
  }

  public String getPhotoUrl() {
    return this.photoUrl;
  }

  public String getResourceName() {
    return this.photoUrl;
  }

  public Boolean hasName() {
    return (!this.name.isEmpty());
  }

  /**
   * Checks if Person object has at least email address.
   *
   * @param person Person Object
   * @return boolean
   */
  public static boolean hasValidParameters(Person person) {
    List<EmailAddress> emails = person.getEmailAddresses();
    String resourceName = person.getResourceName();
    return (emails != null && emails.size() > 0 && !resourceName.isEmpty());
  }

  private ArrayList<String> setEmailsFromList(ArrayList<String> emails) {
    for (int i = 0; i < emails.size(); ++i) {
      emails.set(i, emails.get(i).toLowerCase());
    }
    return emails;
  }
}
