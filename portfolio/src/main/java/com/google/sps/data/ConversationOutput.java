/*
 * Copyright 2019 Google Inc.
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

import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import java.util.List;

public class ConversationOutput {
  private String keyword;
  List<Pair<Entity, List<Entity>>> conversationPairList;
  List<Entity> conversationList;

  /**
   * Conversation output constructor for memory agent's conversation history duration display.
   *
   * @param conversationList List of conversation entities
   */
  public ConversationOutput(List<Entity> conversationList) {
    this.conversationList = conversationList;
  }

  /**
   * Conversation output constructor for memory agent's conversation history keyword display.
   *
   * @param keyword String representing the keyword that was searched
   * @param conversationPairList List of pairs corresponding each found comment with the given
   *     keyword with list of surrounding comment entities
   */
  public ConversationOutput(String keyword, List<Pair<Entity, List<Entity>>> conversationPairList) {
    this.keyword = keyword;
    this.conversationPairList = conversationPairList;
  }

  /** Converts conversation output object to JSON string form. */
  public String toString() {
    return new Gson().toJson(this);
  }
}
