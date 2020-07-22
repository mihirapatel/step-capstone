const streamingContainer = document.getElementsByName('streaming')[0];

/**
* Creates all frontend display for user and assistant comments and specialized displays for each agent.
*
* @param stream JSON output from dialogflow containing all necessary display information.
*/
function displayResponse(stream) {
  var outputAsJson = JSON.parse(stream);
  placeUserInput(outputAsJson.userInput, "convo-container");
  placeFulfillmentResponse(outputAsJson.fulfillmentText);
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
    } else if (outputAsJson.intent.includes("books.search") ||
        outputAsJson.intent.includes("books.more") ||
        outputAsJson.intent.includes("books.previous") ||
        outputAsJson.intent.includes("books.results")){
      bookContainer = createBookContainer(outputAsJson.display);
      placeBookDisplay(bookContainer, "convo-container");
    } else if (outputAsJson.intent.includes("books.description")) {
      descriptionContainer = createBookInfoContainer(outputAsJson.display, "description");
      placeBookDisplay(descriptionContainer, "convo-container");
    } else if (outputAsJson.intent.includes("books.preview")) {
      previewContainer = createBookInfoContainer(outputAsJson.display, "preview");
      placeBookDisplay(previewContainer, "convo-container");
    } else if (outputAsJson.intent.includes("workout.find")) {
      workoutContainer = workoutVideos(outputAsJson.display);
      appendDisplay(workoutContainer);
    } else if (outputAsJson.intent.includes("memory.keyword")) {
      memoryContainer = createKeywordContainer(outputAsJson.display);
      appendDisplay(memoryContainer);
      addDisplayListeners(memoryContainer);
    } else if (outputAsJson.intent.includes("memory.time")) {
      memoryTimeContainer = makeConversationDiv(outputAsJson.display);
      appendDisplay(memoryTimeContainer);
    }
  }
  outputAudio(stream);
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
