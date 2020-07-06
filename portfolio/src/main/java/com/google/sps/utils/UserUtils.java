package com.google.sps.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtils {

  private static Logger log = LoggerFactory.getLogger(UserUtils.class);

  /**
   * Returns a string containing the name we should use to address the user. The name returned is
   * determined by info the user has provided in order of: 1. nickname 2. first name (if no
   * nickname) 3. email (if no name is provided) This method assumes that the user is logged in.
   *
   * @return a string containing the name the conversational AI will use to address the user
   */
  public static String getDisplayName(UserService userService, DatastoreService datastore) {
    try {
      Entity entity =
          datastore.get(KeyFactory.createKey("UserInfo", userService.getCurrentUser().getUserId()));
      return getProperName(userService, entity);
    } catch (EntityNotFoundException e) {
      return userService.getCurrentUser().getEmail();
    }
  }

  private static String getProperName(UserService userService, Entity entity) {
    String name = (String) entity.getProperty("nickname");
    if (name == null) {
      name = (String) entity.getProperty("first name");
    }
    if (name == null) {
      name = userService.getCurrentUser().getEmail();
    }
    return name;
  }

  /**
   * Saves name information into user database. This method assumes that the user is logged in or
   * else it will throw an IllegalStateException error.
   *
   * @param nameType String that specifies which part of user's name the given name refers to:
   *     "first name", "middle name", or "last name"
   * @param name String containing the name to be saved
   */
  public static void saveName(
      UserService userService, DatastoreService datastore, String nameType, String name) {
    String userID = userService.getCurrentUser().getUserId();
    Entity entity;
    try {
      entity = datastore.get(KeyFactory.createKey("UserInfo", userID));
    } catch (Exception e) {
      entity = new Entity("UserInfo", userID);
      entity.setProperty("id", userID);
    }
    entity.setProperty(nameType, name);
    datastore.put(entity);
  }

  /**
   * Saves comment information into comment history database if the user is logged in.
   *
   * @param userComment The comment written by the user.
   * @param assistantComment The fulfillment comment returned by the assistant.
   */
  public static void saveComment(String userComment, String assistantComment) {
    String userID = getUserID();
    if (userID != null) {
      makeCommentEntity(userID, userComment, true);
      makeCommentEntity(userID, assistantComment, false);
    }
  }

  private static void makeCommentEntity(String userID, String comment, boolean isUser) {
    Entity entity = new Entity("CommentHistory", String.valueOf(System.currentTimeMillis()));
    entity.setProperty("id", userID);
    entity.setProperty("isUser", isUser);
    entity.setProperty("comment", comment);
    datastore.put(entity);
  }
}
