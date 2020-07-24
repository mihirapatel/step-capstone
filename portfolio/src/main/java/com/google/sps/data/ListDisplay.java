package com.google.sps.data;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import java.util.List;

public class ListDisplay {
  private String listName;
  List<String> items;
  boolean multiList;
  List<ListDisplay> allLists;

  public ListDisplay(String listName, List<String> items) {
    this.listName = listName;
    this.items = items;
    this.multiList = false;
  }

  public ListDisplay(List<ListDisplay> lists) {
    allLists = lists;
    this.multiList = true;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
