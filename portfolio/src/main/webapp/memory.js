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
 
var commentToConvo = new Map();
var commentDivToEntity = new Map();
var listNameDivToEntity = new Map();

var textFile = null;

/**
 * Creates the media container for memory.keyword display.
 * 
 * @param jsonOutput JSON string of the Conversation Output object from backend
 * @return Div containing the keyword display container
 */
function createKeywordContainer(jsonOutput) {
  var conversationOutputObject = JSON.parse(jsonOutput);
  var keyword = conversationOutputObject.keyword;
  var conversationPairList = conversationOutputObject.conversationPairList;
  let keywordComments = conversationPairList.map(function(conversationObject) {
    comment = conversationObject.key.propertyMap;
    var surroundingConvo = conversationObject.value.map(function(entity) {
      return entity.propertyMap;
    });
    commentToConvo.set(comment, surroundingConvo);
    return comment;
  });

  var memoryContainer = document.createElement('div');
  memoryContainer.classList.add('memory');
  addIdentifiedComments(keyword, keywordComments, commentToConvo, memoryContainer);
  return memoryContainer;
}

/**
 * Bolds the keyword in each identified comment and adds it as a list element inside the memory container.
 * 
 * @param keyword The keyword that the user requested.
 * @param keywordComments A list of comments all with the identified keyword (aka. keyset of commentToConvo)
 * @param commentToConvo Map containing each individual keywordComment as a key with a list of surrounding comments as its value
 * @param memoryContainer Div container that holds the entire display.
 */
function addIdentifiedComments(keyword, keywordComments, commentToConvo, memoryContainer) {
  var comment;
  var keywordCommentContainer = document.createElement('ul');
  keywordCommentContainer.classList.add('left-panel');
  var firstCommentDiv = null;
  for (commentEntity of keywordComments) {
    var commentString = commentEntity.comment;
    var boldedCommentString = makeBold(commentString, keyword);
    var commentDiv = document.createElement('li');
    commentDiv.classList.add('clickable');
    if (firstCommentDiv == null) {
        firstCommentDiv = commentDiv;
    }
    commentDivToEntity.set(commentDiv, commentEntity);
    var timeString = new Date(commentEntity.timestamp).toLocaleString()
    commentDiv.innerHTML = "<p class='timestamp'>" + timeString + "</p><p class='comment-text'>" + boldedCommentString + "</p>";
    keywordCommentContainer.appendChild(commentDiv);
  }
  memoryContainer.appendChild(keywordCommentContainer);
  var conversationContainer = document.createElement('div');
  conversationContainer.classList.add('content-panel');
  memoryContainer.appendChild(conversationContainer);
  $(firstCommentDiv).addClass('active');
  getConversationScreen(firstCommentDiv);
}

/**
 * Creates the conversation screen corresponding to the selected comment div.
 *
 * @param commentDiv Selected commentDiv that contains the identified comment used to determine the surrounding conversation
 * to be displayed on the conversation side of the display.
 */
function getConversationScreen(commentDiv) {
  var keywordEntity = commentDivToEntity.get(commentDiv);
  var conversationList = commentToConvo.get(keywordEntity);
  var conversationDiv = commentDiv.parentElement.nextSibling;
  conversationDiv.innerHTML = "";
  populateConversationScreen(conversationDiv, conversationList, keywordEntity);
}

/**
 * Creates the conversation display for memory.time intent
 *
 * @param jsonOutput JSON string of the Conversation Output object from backend.
 * @return Div container for the conversation display
 */
function makeConversationDiv(jsonOutput) {
  var conversationOutputList = JSON.parse(jsonOutput).conversationList;
  var conversationList = conversationOutputList.map(function(entity) {
      return entity.propertyMap;
    });
  var conversationContainer = document.createElement('div');
  conversationContainer.classList.add('memory');
  var conversationDiv = document.createElement('div');
  conversationDiv.classList.add('conversation-div');
  populateConversationScreen(conversationDiv, conversationList, null);
  conversationContainer.appendChild(conversationDiv);
  return conversationContainer;
}

/**
 * Iteratively adds each comment in the surrounding conversation list corresponding to the selected comment 
 * in the comment div into the conversation side display.
 *
 * @param conversationDiv Div to place surrounding conversation entities
 * @param conversationList List of comment entities in the surrounding conversation
 * @param keyWordEntity Specific entity within the conversationList which contains the keyword
 */
function populateConversationScreen(conversationDiv, conversationList, keywordEntity) {
  var commentEntity;
  var prevTime = 0;
  for (commentEntity of conversationList) {
    if (commentEntity.timestamp - prevTime > 300000) {  // 5 minute difference
      makeTimestamp(commentEntity.timestamp, conversationDiv);
    }
    prevTime = commentEntity.timestamp;
    var text;
    if (_.isEqual(keywordEntity, commentEntity)) {
      text = "<p style='color: black'><span style='background-color: yellow'>" + commentEntity.comment + "</span></p>";
    } else {
      if (commentEntity.isUser) {
        text = "<p style='color: white'>" + commentEntity.comment + "</p>";
      } else {
        text = "<p style='color: black'>" + commentEntity.comment + "</p>";
      }
    }
    if (commentEntity.isUser) {
      placeChatContainer(text, "user-side talk-bubble-user round", "right", conversationDiv, 0);
    } else {
      placeChatContainer(text, "assistant-side talk-bubble-assistant round", "left", conversationDiv, 0);
    }
  }
}

/**
 * Creates a small centered timestamp symbol in the conversation div.
 *
 * @param time The time to display represented as time since 1970
 * @param conversationDiv Div to place the time display into
 */
function makeTimestamp(time, conversationDiv) {
  var timeString = new Date(time).toLocaleString()
  timeDiv = document.createElement('div');
  timeDiv.innerHTML = "<p class='time-centered'>" + timeString + "</p>";
  conversationDiv.appendChild(timeDiv);
}

/**
 * Bolds all instances of boldedWord found in the text string input.
 *
 * @param text String that contains the identified comment
 * @param boldedWord keyword to be bolded
 */
function makeBold(text, boldedWord) {
  return text.replace(new RegExp("(" + boldedWord + ")",'ig'), '<b>$1</b>');
}

/**
 * Creates on-click listeners for each bulleted element in the left-side panel.
 *
 * @param container The div containing the media display to be populated with click listeners.
 */
function addDisplayListeners(container, displayFunction) {
    if ($(container).children().length < 2) {
      return;
    }
    var menuDiv = container.firstChild;
    $(menuDiv).on('click', 'li', function() {
      $('li').removeClass('active');
      $(this).addClass('active');
      displayFunction(this);
    });
}

/**
 * Creates list display according to the list display object retrieved from backend.
 *
 * @param listDisplayObject Object from backend that contains all necessary list display info
 */
function makeListContainer(listDisplayObject) {
    var memoryContainer = document.createElement('div');
    if (listDisplayObject.multiList) {
        memoryContainer.classList.add('memory');
        var listContentContainer = document.createElement('div');
        listContentContainer.classList.add('content-panel');
        var userLists = listDisplayObject.allLists;
        var listNameContainer = document.createElement('ul');
        listNameContainer.classList.add('left-panel');
        var firstListObject = null;
        var firstListNameDiv = null;
        for (lst of userLists) {
            var listNameDiv = document.createElement('li');
            listNameDiv.classList.add('clickable');
            if (firstListNameDiv == null) {
                firstListObject = lst;
                firstListNameDiv = listNameDiv;
            }
            listNameDivToEntity.set(listNameDiv, lst);
            listNameDiv.innerHTML = "<p class='comment-text'>" + lst.listName + "</p>";
            listNameContainer.appendChild(listNameDiv);
        }
        memoryContainer.appendChild(listNameContainer);
        $(firstListNameDiv).addClass('active');
        populateListContentScreen(firstListObject, listContentContainer);
    } else {
        memoryContainer.classList.add('list-container');
        var listContentContainer = document.createElement('div');
        listContentContainer.classList.add('list-content');
        populateListContentScreen(listDisplayObject, listContentContainer);
    }
    memoryContainer.appendChild(listContentContainer);
    return memoryContainer;
}

/**
 * Displays all list content onto the given container.
 *
 * @param listDisplayObject Object from backend that contains all necessary list display info
 * @param listContentContainer Div container to place list contents onto
 */
function populateListContentScreen(listDisplayObject, listContentContainer) {
  var headerContainer = document.createElement('div');
  headerContainer.innerHTML = "<h1 style='color: black'>" + listDisplayObject.listName + " list</h1>";
  listContentContainer.appendChild(headerContainer);
  if (listDisplayObject.items) {
    for (listItem of listDisplayObject.items) {
        listContentContainer.appendChild(makeBulletedElement(listItem));
    }
  }
  var downloadButton = document.createElement('a');
  downloadButton.classList.add('download');
  downloadButton.innerHTML = "<img src = \"images/download.png\" class=\"download-image\" alt = \"Download\">";
  downloadButton.addEventListener("click", function() {
    var contentDiv = this.parentNode;
    var regexp1 = /<[a-z]*?>(.*?)<\/[a-z]*?>/g;
    var regexp2 = /<.*?>(.*?)<.*?>/g;
    var listItems = [...contentDiv.innerHTML.matchAll(regexp1)];
    var title = [...listItems[0][1].matchAll(regexp2)][0][1];
    var listText = title + "\n\n";
    for (var i = 1; i < listItems.length; i++) {
        listText += "- " + listItems[i][1] + "\n";
    }
    var blob = new Blob([listText], {type : "text/plain;charset=utf-8"});
    if (textFile !== null) {
        window.URL.revokeObjectURL(textFile);
    }
    textFile = window.URL.createObjectURL(blob);

    this.setAttribute("href", textFile);
    this.download = title + ".txt";
  })
  listContentContainer.appendChild(downloadButton);
}

/**
 * Creates the list content screen corresponding to the selected list name.
 *
 * @param commentDiv Selected commentDiv that contains the identified comment used to determine the surrounding conversation
 * to be displayed on the conversation side of the display.
 */
function getListContentScreen(listNameDiv) {
  var listEntity = listNameDivToEntity.get(listNameDiv);
  var listContentDiv = listNameDiv.parentElement.nextSibling;
  listContentDiv.innerHTML = "";
  populateListContentScreen(listEntity, listContentDiv);
}

/**
 * Creates the bulleted items in list content screen.
 *
 * @param itemString Text for the bulleted item
 * @return Div containing the bulleted text
 */
function makeBulletedElement(itemString) {
    var bulletDiv = document.createElement('li');
    bulletDiv.classList.add('plain');
    bulletDiv.innerHTML = itemString;
    return bulletDiv;
}
