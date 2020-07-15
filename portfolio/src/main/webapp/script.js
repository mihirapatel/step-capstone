// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
/**
 * Starts listening to user input and fetches user input string.
 * Defines sessionId global variable and function to call when window closes.
 */
 
const mainSection = document.querySelector('.main-controls');
const formContainer = document.getElementsByName('input-form')[0];
const textInputContainer = document.getElementById("text-input");
var sessionId = "";
window.onbeforeunload = deleteSessionInformation;

formContainer.onkeyup = function(e){
  if(e.keyCode == 13 && textInputContainer.value.length != 0) { //return key and non-empty input
    getResponseFromText();
  }
};

window.onresize = function() {
  canvas.width = mainSection.offsetWidth;
}
 
window.onresize();

function getLanguage() {
  var language = window.sessionStorage.getItem("language");
  language = language == null ? "English" : language;
  return language;
}

function getAudioStream(blob) {
  fetch('/audio-stream' + '?language=' + getLanguage(), {
    method: 'POST',
    body: blob
  }).then(response => response.text()).then(stream => {
    streamingContainer.innerHTML = "";
    stream = (stream.includes(null)) ? "" : stream;
    placeUserInput(stream + "...", "streaming");
  });
}
 
function getResponseFromAudio(blob) {
  const formData = new FormData();
  formData.append('audio-file', blob);
  fetch('/audio-input' + '?language=' + getLanguage() + '&session-id=' + sessionId, {
    method: 'POST',
    body: blob
  }).then(response => response.text()).then(stream => displayResponse(stream));
}
 
function getResponseFromText(){
  var input = textInputContainer.value;
  fetch('/text-input?request-input=' + input + '&language=' + getLanguage() + '&session-id=' + sessionId, {
      method: 'POST'
  }).then(response => response.text()).then(stream => displayResponse(stream));
  formContainer.reset(); 
}

function sendRedirect(URL){
  window.open(URL);
}

function authSetup() {
  fetch("/auth").then((response) => response.json()).then((displayText) => {
    var authContainer = document.getElementsByClassName("auth-link")[0];
    authContainer.innerHTML = "<a class=\"link\" href=\"" + displayText.authText + "\">" + displayText.logButton + "</a>";
    updateName(displayText.displayName);
    getSessionID();
  });
}

function updateName(name) {
  var greetingContainer = document.getElementsByName("greeting")[0];
  greetingContainer.innerHTML = "<h1>Hi " + name + ", what can I help you with?</h1>";
}

/**
 * Retrieves Output object created by BookAgent for specified intent 
 * triggered by a button the display 
 * 
 * @param intent name of book intent
 */
function getBooksFromButton(intent){
  fetch('/book-agent?intent=' + intent + '&language=' + getLanguage() + '&session-id=' + sessionId, {
      method: 'POST'
  }).then(response => response.text()).then(stream =>displayBooksFromButton(stream));
}

/**
 * Retrieves Output object created by BookAgent for specified information intent 
 * triggered by a button the display. Number parameter specifies the Book object 
 * number to retrieve.
 * 
 * @param intent name of book intent
 * @param number index of book to retrieve information for
 */
function getBookInformation(intent, number){
  fetch('/book-agent?intent=' + intent + '&language=' + getLanguage() + '&session-id=' + sessionId +
    '&number=' + number, {
      method: 'POST'
  }).then(response => response.text()).then(stream =>displayBookInfo(stream));
}

/**
 * Returns userID, if user is logged in, or guestID for the session otherwise
 */
function getSessionID(){
  fetch('/id').then(response => response.text()).then((id) => {
      window.sessionId = id;
  });
}

/**
 * Deletes all stored Entitys that match current sessionID before
 * users close the page
 */
function deleteSessionInformation(){
  fetch('/id' + '?session-id=' + sessionId, {
      method: 'POST'
  }).then(response => response.text()).then(() => {
      console.log('Deleted comments for ' + sessionId)
  });
  return null;
}