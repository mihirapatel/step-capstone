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

public class PeopleUtils {
  private static Logger log = LoggerFactory.getLogger(PeopleUtils.class);

  /**
   * This function returns a boolean determining whether the user, specified by userID, contains a
   * friend with the specified name (or email), based on their connections from the Google People
   * API and throws an exception otherwise
   *
   * @param userID ID for authenticated user
   * @param friendName friend to look for
   * @param oauthHelper OAuthHelper instance used to access OAuth methods
   * @return boolean value
   */
  public Boolean hasFriend(String userID, String friendName, OAuthHelper oauthHelper)
      throws IOException {
    for (Friend friend : getFriends(userID, oauthHelper)) {
      if (friendName.toLowerCase().equals(friend.getName().toLowerCase())
          || friend.getEmails().contains(friendName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This function returns a list of the user's friends, based on their connections from the Google
   * People API and throws an exception otherwise
   *
   * @param userID ID for authenticated user
   * @param helper OAuthHelper instance used to access OAuth methods
   * @return ArrayList<Friend> list of friends
   */
  public ArrayList<Friend> getFriends(String userID, OAuthHelper helper) throws IOException {
    return getFriendList(getContactsFromAPI(userID, helper));
  }

  /**
   * This function builds and returns a PeopleService object that can access a list of the Google
   * People API and throws an exception otherwise
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
   * This function returns an ArrayList of Friend objects returned from the Google People API
   *
   * <p>If no valid People objects are returned, it returns an empty ArrayList
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
   * This function returns a Bookshelves object containing the Bookshelves from the Google Books API
   * that match the authenticated user's bookshelves and throws an exception otherwise
   *
   * @param userID unique userID
   * @param helper OAuthHelper instance used to access OAuth methods
   * @return Bookshelves object of results
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
            .setPersonFields("names,emailAddresses")
            .execute();
    List<Person> connections = response.getConnections();
    return connections;
  }
}