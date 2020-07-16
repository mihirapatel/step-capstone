var commentToConvo = new Map();
var commentDivToEntity = new Map();

/**
* Creates the media container for memory.keyword display.
* 
* @param jsonOutput JSON string of the Conversation Output object returned from 
*/
function createKeywordContainer(jsonOutput) {
  var conversationOutputObject = JSON.parse(jsonOutput);
  var keyword = conversationOutputObject.keyword;
  var conversationPairList = conversationOutputObject.conversationList;
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
  keywordCommentContainer.classList.add('keyword-screen');
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
  conversationContainer.classList.add('conversation-screen');
  memoryContainer.appendChild(conversationContainer);
  $(firstCommentDiv).addClass('active');
  populateConversationScreen(firstCommentDiv);
}

/**
* Iteratively adds each comment in the surrounding conversation list corresponding to the selected comment 
* in the comment div into the conversation side display.
*
* @param commentDiv Selected commentDiv that contains the identified comment used to determine the surrounding conversation
* to be displayed on the conversation side of the display.
*/
function populateConversationScreen(commentDiv) {
  var keywordEntity = commentDivToEntity.get(commentDiv);
  var conversationList = commentToConvo.get(keywordEntity);
  var conversationDiv = commentDiv.parentElement.nextSibling;
  conversationDiv.innerHTML = "";
  var commentEntity;
  for (commentEntity of conversationList) {
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
* Creates on-click listeners for each bulleted element in the identified comments list.
*
* @param memoryContainer The div containing the media display to be populated with click listeners.
*/
function addDisplayListeners(memoryContainer) {
    var keywordScreenDiv = memoryContainer.firstChild;
    $(keywordScreenDiv).on('click', 'li', function() {
      $('li').removeClass('active');
      $(this).addClass('active');
      populateConversationScreen(this);
    });
}