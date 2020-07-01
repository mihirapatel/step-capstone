package com.google.sps.agents;

public class UserObject {

  String userID;
  String email;

  UserObject(String userID, String email) {
    this.userID = userID;
    this.email = email;
  }

  String getUserId() {
    return userID;
  }

  String getEmail() {
    return email;
  }
}
