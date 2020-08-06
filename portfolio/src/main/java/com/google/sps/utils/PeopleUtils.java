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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.google.gson.*;
import com.google.sps.data.Friend;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class contains methods to access the Google People API.
 */
public class PeopleUtils {
  private static Logger log = LoggerFactory.getLogger(PeopleUtils.class);

  /**
   * This function returns all Friend objects in a user's contact list that match the specified
   * friendName based on the userID's connections from the Google People API.
   *
   * @param userID String containing current user's unique ID
   * @param friendName friend to look for
   * @param oauthHelper OAuthHelper instance used to access OAuth methods
   * @return ArrayList<Friend> of matches
   */
  public ArrayList<Friend> getMatchingFriends(
      String userID, String friendName, OAuthHelper oauthHelper) throws IOException {
    ArrayList<Friend> matches = new ArrayList<Friend>();
    for (Friend friend : getFriends(userID, oauthHelper)) {
      if (friendName.toLowerCase().equals(friend.getName().toLowerCase())
          || friend.getEmails().contains(friendName)) {
        if (!matches.contains(friend)) {
          matches.add(friend);
        }
      }
    }
    return matches;
  }

  /**
   * This function returns a list of the user's friends, based on their connections from the Google
   * People API and throws an exception otherwise.
   *
   * @param userID String containing current user's unique ID
   * @param helper OAuthHelper instance used to access OAuth methods
   * @return ArrayList<Friend> list of friends
   */
  public ArrayList<Friend> getFriends(String userID, OAuthHelper helper) throws IOException {
    return getFriendList(getContactsFromAPI(userID, helper));
  }

  /**
   * This function builds and returns a PeopleService object that can access a list of the Google
   * People API and throws an exception otherwise.
   *
   * @param credential Valid credential for authenticated user
   * @return PeopleService object
   */
  private PeopleService getPeopleService(Credential credential) throws IOException {
    GsonFactory gsonFactory = new GsonFactory();
    UrlFetchTransport transport = new UrlFetchTransport();
    PeopleService service =
        new PeopleService.Builder(transport, gsonFactory, credential)
            .setApplicationName("AI-ssistant")
            .build();
    return service;
  }

  /**
   * This function returns an ArrayList of Friend objects returned from the Google People API.
   *
   * <p>If no valid People objects are returned, it returns an empty ArrayList.
   *
   * @param connections list of Person objects from Google Books API
   * @return ArrayList<Friend>
   */
  private ArrayList<Friend> getFriendList(List<Person> connections) {
    ArrayList<Friend> friends = new ArrayList<Friend>();
    if (connections != null && connections.size() > 0) {
      for (Person person : connections) {
        try {
          friends.add(Friend.createFriend(person));
        } catch (IOException e) {
          log.error("Person with no email was not added to the list.");
        }
      }
    }
    return friends;
  }

  /**
   * This function returns a Friend object of the requested resourceName including name, emails, and
   * a photo, and null if one could not be created.
   *
   * @param userID String containing current user's unique ID
   * @param resourceName unique contact resourceName to retrieve
   * @param helper OAuthHelper instance used to access OAuth methods
   * @return Friend object
   */
  public Friend getUserInfo(String userID, String resourceName, OAuthHelper helper) {
    try {
      Person person = getPerson(userID, resourceName, helper);
      return Friend.createFriend(person);
    } catch (IOException e) {
      log.error("User with no email was not created.");
    }
    return null;
  }

  /**
   * This function returns a List of Person objects containing information about Person connections
   * from the Google People API for the authenticated user and t throws an exception otherwise.
   *
   * @param userID String containing current user's unique ID
   * @param helper OAuthHelper instance used to access OAuth methods
   * @return List<Person> list of results
   */
  public List<Person> getContactsFromAPI(String userID, OAuthHelper helper)
      throws IOException, GoogleJsonResponseException {
    Credential credential = helper.loadUpdatedCredential(userID);
    PeopleService service = getPeopleService(credential);
    ListConnectionsResponse response =
        service
            .people()
            .connections()
            .list("people/me")
            .setPersonFields("names,emailAddresses,photos")
            .setPageSize(2000)
            .execute();
    List<Person> connections = response.getConnections();
    return connections;
  }

  /**
   * This function returns a Person object of the specified resourceName from the Google People API
   * including name, emails, and a photo, and throws an exception otherwise.
   *
   * @param userID String containing current user's unique ID
   * @param resourceName unique contact resourceName to retrieve
   * @param helper OAuthHelper instance used to access OAuth methods
   * @return Person object
   */
  public Person getPerson(String userID, String resourceName, OAuthHelper helper)
      throws IOException, GoogleJsonResponseException {
    Credential credential = helper.loadUpdatedCredential(userID);
    PeopleService service = getPeopleService(credential);
    Person response =
        service.people().get(resourceName).setPersonFields("names,emailAddresses,photos").execute();
    return response;
  }
}
