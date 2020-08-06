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
 
 const streamingContainer = document.getElementsByName('streaming')[0];
var isUserLoggedIn = false;

/** 
 * Creates user's chat display
 *
 * @param text User's chat input content
 * @param container Div container to place chat content
 */
function placeUserInput(text, container) {
  if (container == "convo-container") {
    streamingContainer.innerHTML = "";
    streamingContainer.style.display = "none";
  }
  console.log("text: " + text);
  if (text != " (null) "){
    var formattedInput = text.substring(0, 1).toUpperCase() + text.substring(1); 
    placeChatContainer("<p style=\'color: white\'>" + formattedInput + "</p>", "user-side talk-bubble-user round", "right", document.getElementsByName(container)[0]);
  }
}

/**
 * Retrieves the last word in the input string.
 *
 * @param words String of words
 * @return Last word in the input string of words
 */
function getLastWord(words) {
    var split = words.split(/[ ]+/);
    console.log(split);
    return split[split.length - 1];
}
 
/**
 * Places the provided text onto the conversation display in the given class style
 *
 * @param text String content to be displayed.
 * @param type Classes to determine the display style
 */
function placeDisplay(text, type) {
  placeObjectContainer(text, type, "convo-container");
}

/**
 * Places the provided text onto the container in the given class style
 *
 * @param text String content to be displayed.
 * @param type Classes to determine the display style
 * @param container Div container on which to place the text content.
 */
function placeObjectContainer(text, type, container) {
  var container = document.getElementsByName(container)[0];
  var newDiv = document.createElement('div');
  newDiv.innerHTML = "<div class='" + type + "'>" + text + "</div>";
  container.appendChild(newDiv);
  updateScroll();
  return container;
}

/**
 * Places the chat content onto the container.
 *
 * @param text String content to be displayed.
 * @param type Classes to determine the display style
 * @param container Div container on which to place the text content.
 * @param marginBottom Determines the amount of margin below the text display.
 * @return Div containe containing the created chat bubble
 */
function placeChatContainer(text, type, side, container, marginBottom) {
  var newDiv = document.createElement('div');
  newDiv.setAttribute("style", "float: " + side + "; width: 100%; margin-bottom:" + marginBottom + "px;");
  newDiv.innerHTML = "<div class='" + type + "'>" + text + "</div>";
  container.appendChild(newDiv);
  updateScroll();
  return newDiv;
}

/**
 * Converts HTML text into a div and places it into the container
 *
 * @param text HTML content to be displayed.
 * @param type Classes to determine the display style
 * @param container Div container on which to place the text content. 
 */
function appendHTML(text, type, container) {
  var container = document.getElementsByName(container)[0];
  var newDiv = document.createElement('div');
  for (className of type.split(' ')) {
    newDiv.classList.add(className);
  }
  newDiv.innerHTML = text;
  container.appendChild(newDiv);
  updateScroll();
  return container;
}

/**
 *Appends the provided div into the conversation display.
 *
 * @param div Div container to be added to the conversation display
 * @return Conversation Display container
 */
function appendDisplay(div) {
  var container = document.getElementsByName("convo-container")[0];
  container.appendChild(div);
  updateScroll();
  return container;
}

/**
 * Updates the content div to automatically scroll as new items are added.
 */
function updateScroll() {
  var element = document.getElementById("content");
  element.scrollTop = element.scrollHeight;
}