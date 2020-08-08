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
 
/**
 * Creates all frontend display for user and assistant comments and specialized displays for each agent.
 *
 * @param stream JSON output from dialogflow containing all necessary display information.
 */
function displayResponse(stream) {
  console.log(stream);
  var outputAsJson = JSON.parse(stream);
  if (!outputAsJson.intent.includes("books") || !outputAsJson.display) {
    placeUserInput(outputAsJson.userInput, "convo-container");
    placeFulfillmentResponse(outputAsJson.fulfillmentText);
  }
  if (outputAsJson.display) {
    if (outputAsJson.intent.includes("reminders.snooze")) {
      convoContainer = placeObjectContainer(outputAsJson.display, "media-display timer-display", "convo-container");
      var allTimers = document.getElementsByClassName("timer-display");
      if (existingTimer) {
        terminateTimer(allTimers[0]);
      }
      existingTimer = true;
    } else if (outputAsJson.intent.includes("name.user.change")) {
      updateName(outputAsJson.display);
    } else if (outputAsJson.intent.includes("maps.search")) {
      mapContainer = locationMap(outputAsJson.display);
      appendDisplay(mapContainer);
    } else if (outputAsJson.intent.includes("maps.find")) {
      if (moreButton) {
        moreButton.style.display = "none";
      }
      mapContainer = nearestPlacesMap(outputAsJson.display);
      appendDisplay(mapContainer);
    } else if (isBooksIntent(outputAsJson.intent)) {
      // Clear previous display if user is not making a new query
      if (!isNewQuery(outputAsJson.intent)){
        clearPreviousDisplay(outputAsJson.redirect);
      }
      // Place user input and fulfillment
      placeBooksUserInput(outputAsJson.userInput, "convo-container", outputAsJson.redirect);
      placeBooksFulfillment(outputAsJson.fulfillmentText, outputAsJson.redirect);
      
      // Get appropriate book container
      var bookContainer;
      if (outputAsJson.fulfillmentText.includes("would you like to see?")) {
        bookContainer = createNameContainer(outputAsJson.display, outputAsJson.intent);
      } else if (isBookInformationIntent(outputAsJson.intent)) {
        bookContainer = createBookInfoContainer(outputAsJson.display, outputAsJson.intent, outputAsJson.redirect, "");
      } else {
        bookContainer = createBookContainer(outputAsJson.display, outputAsJson.redirect);
      }
      //Place appropriate book container
      placeBookDisplay(bookContainer, "convo-container", outputAsJson.redirect);
      if (outputAsJson.intent.includes("preview")) {
        loadPreview(outputAsJson.display);
      }
    } else if (outputAsJson.intent.includes("workout.find")) {
      workoutContainer = workoutVideos(outputAsJson.display);
      appendDisplay(workoutContainer);
    } else if (outputAsJson.intent.includes("workout.plan")) {
      workoutPlanContainer = workoutPlanner(outputAsJson.display);
      appendDisplay(workoutPlanContainer);
    } else if (outputAsJson.intent.includes("memory.keyword")) {
      memoryContainer = createKeywordContainer(outputAsJson.display);
      appendDisplay(memoryContainer);
      addDisplayListeners(memoryContainer, getConversationScreen);
    } else if (outputAsJson.intent.includes("memory.time")) {
      memoryTimeContainer = makeConversationDiv(outputAsJson.display);
      appendDisplay(memoryTimeContainer);
    } else if (outputAsJson.intent.includes("memory.list - show")) {
      listContainer = makeListContainer(JSON.parse(outputAsJson.display));
      appendDisplay(listContainer);
      addDisplayListeners(listContainer, getListContentScreen);
    }
  }
  outputAudio(stream);
}

/** 
 * Creates user's chat display with given book query ID
 *
 * @param text User's chat input content
 * @param container Div to place user's chat display
 * @param queryID Unique identification for the user's current query session
 */
function placeBooksUserInput(text, container, queryID) {
  if (container == "convo-container") {
    streamingContainer.innerHTML = "";
    streamingContainer.style.display = "none";
  }
  if (text != " (null) "){
    var formattedInput = text.substring(0, 1).toUpperCase() + text.substring(1); 
    placeChatContainer("<p style=\'color: white\'>" + formattedInput + "</p>", "user-side-" + queryID + " talk-bubble-user round", "right", document.getElementsByName("convo-container")[0], 0);
  }
}

/** 
 * Creates assistant's chat display with given book query ID
 *
 * @param text Assistant's chat input content
 * @param queryID Unique identification for the user's current query session
 */
function placeBooksFulfillment(text, queryID) {
  placeChatContainer("<p>" + text + "</p>", "assistant-side-" + queryID + " talk-bubble-assistant round", "left", document.getElementsByName("convo-container")[0], 15);
  if (text.includes("Switching conversation language")) {
    window.sessionStorage.setItem("language", getLastWord(text));
  }
}

/** 
 * Creates assistant's chat display
 *
 * @param text Assistant's chat output content
 */
function placeFulfillmentResponse(text) {
  placeChatContainer("<p>" + text + "</p>", "assistant-side talk-bubble-assistant round", "left", document.getElementsByName("convo-container")[0], 0);
  console.log(text);
  if (text.includes("Switching conversation language")) {
    window.sessionStorage.setItem("language", getLastWord(text));
  } else if (text.includes("Please allow me to access your Google Books account first.")) {
    fetch("/auth", {
      method: 'POST',
      mode: 'no-cors'});
  }
}

/**
 * Updates the frontend javascript to include the user's name (or nickname) in the title.
 *
 * @param name The name used to refer to the user.
 */
function updateName(name) {
  var greetingContainer = document.getElementsByName("greeting")[0];
  if (name.length > 1) {
      greetingContainer.innerHTML = "<h1>Hi " + name + ", how can I help you?</h1>";
  } else {
      greetingContainer.innerHTML = "<h1>Hi, how can I help you?</h1>";
  }
}

/**
 * Checks if intent is a book intent
 *
 * @param intentName Full name of the intent
 * @return boolean indicating if provided intent name is a book intent
 */
function isBooksIntent(intentName) {
  return (intentName.includes("books."));
}

/**
 * Checks if intent is a book display intent
 *
 * @param intentName Full name of the intent
 * @return boolean indicating if provided intent name is a book display intent
 */
function isBooksDisplayIntent(intentName) {
  return (intentName.includes("books.search") ||
    intentName.includes("books.more") ||
    intentName.includes("books.previous") || 
    intentName.includes("books.library") ||
    intentName.includes("books.results") ||
    intentName.includes("books.friends"));
}

/**
 * Checks if intent is a new query
 *
 * @param intentName Full name of the intent
 * @return boolean indicating if provided intent name indicates a new query
 */
function isNewQuery(intentName) {
  return (intentName.includes("books.search") || 
         intentName.includes("books.library") ||
         intentName.includes("books.friends"));
}

/**
 * Checks if intent is for book information
 *
 * @param intentName Full name of the intent
 * @return boolean indicating if provided intent name is a request for book information
 */
function isBookInformationIntent(intentName) {
  return (intentName.includes("books.description") ||
          intentName.includes("books.preview"));
}