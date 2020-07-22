var commentToConvo = new Map();
var commentDivToEntity = new Map();
var listNameDivToEntity = new Map();

/**
* Creates the media container for memory.keyword display.
* 
* @param jsonOutput JSON string of the Conversation Output object returned from 
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
* Iteratively adds each comment in the surrounding conversation list corresponding to the selected comment 
* in the comment div into the conversation side display.
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

function populateConversationScreen(conversationDiv, conversationList, keywordEntity) {
  var commentEntity;
  var prevTime = 0;
  for (commentEntity of conversationList) {
    if (commentEntity.timestamp - prevTime > 300000) {  // 5 minute difference
      makeTimestamp(commentEntity.timestamp, conversationDiv);
    }
    prevTime = commentEntity.timestamp;
    var conversationCommentDiv = document.createElement('div');
    var className = commentEntity.isUser ? 'user-side' : 'assistant-side';
    conversationCommentDiv.classList.add(className);
    if (_.isEqual(keywordEntity, commentEntity)) {
      conversationCommentDiv.innerHTML = "<p style='color: black'><span style='background-color: yellow'>" + commentEntity.comment + "</span></p>";
    } else {
      conversationCommentDiv.innerHTML = "<p style='color: black'>" + commentEntity.comment + "</p>";
    }
    conversationDiv.appendChild(conversationCommentDiv);
  }
}

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
  var menuDiv = container.firstChild;
  $(menuDiv).on('click', 'li', function() {
    $('li').removeClass('active');
    $(this).addClass('active');
    displayFunction(this);
  });
}

function makeListContainer(listDisplayObject) {
  var memoryContainer = document.createElement('div');
  memoryContainer.classList.add('memory');
  var listContentContainer = document.createElement('div');
  listContentContainer.classList.add('content-panel');
  if (listDisplayObject.multiList) {
    var userLists = listDisplayObject.allLists;
    var listNameContainer = document.createElement('ul');
    listNameContainer.classList.add('left-panel');
    var firstListObject = null;
    var firstListNameDiv = null;
    for (lst of userLists) {
      var listNameDiv = document.createElement('li');
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
    populateListContentScreen(listDisplayObject, listContentContainer);
  }
  memoryContainer.appendChild(listContentContainer);
  return memoryContainer;
}

function populateListContentScreen(listDisplayObject, listContentContainer) {
  var headerContainer = document.createElement('div');
  headerContainer.innerHTML = "<h1 style='color: black'>" + listDisplayObject.listName + " list</h1>";
  listContentContainer.appendChild(headerContainer);
  if (listDisplayObject.items) {
    for (listItem of listDisplayObject.items) {
        listContentContainer.appendChild(makeBulletedElement(listItem));
    }
  }
}

function getListContentScreen(listNameDiv) {
  var listEntity = listNameDivToEntity.get(listNameDiv);
  var listContentDiv = listNameDiv.parentElement.nextSibling;
  listContentDiv.innerHTML = "";
  populateListContentScreen(listEntity, listContentDiv);
}

function makeBulletedElement(itemString) {
  var bulletDiv = document.createElement('li');
  bulletDiv.innerHTML = itemString;
  return bulletDiv;
}