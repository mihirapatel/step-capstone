package com.google.sps.data;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import java.util.List;

public class ConversationOutput {
  private String keyword;
  List<Pair<Entity, List<Entity>>> conversationPairList;
  List<Entity> conversationList;

  public ConversationOutput(List<Entity> conversationList) {
    this.conversationList = conversationList;
  }

  public ConversationOutput(String keyword, List<Pair<Entity, List<Entity>>> conversationPairList) {
    this.keyword = keyword;
    this.conversationPairList = conversationPairList;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
