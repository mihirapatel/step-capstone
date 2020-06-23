package com.google.sps.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UserUtils {

  static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  static UserService userService = UserServiceFactory.getUserService();

  public static String getDisplayName() {
    try {
      Entity entity = datastore.get(KeyFactory.createKey("UserInfo", getUserID()));
      return getProperName(entity);
    } catch (Exception e) {
      return "";
    }
  }

  private static String getProperName(Entity entity) {
    String name = (String) entity.getProperty("nickname");
    if (name == null) {
      name = (String) entity.getProperty("first name");
    }
    if (name == null) {
      name = userService.getCurrentUser().getEmail();
    }
    return name;
  }

  public static String getUserID() {
    if (userService.isUserLoggedIn()) {
      return userService.getCurrentUser().getUserId();
    } else {
      return null;
    }
  }

  public static void saveName(String nameType, String name) {
    String userID = getUserID();
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
}