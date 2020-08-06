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
 
var existingTimer = false;
var timer = null;

/**
 * Decreases the time display by 1 second.
 *
 * @param timeContainer Div containing the remaining time in the timer.
 */
function decrementTime(timeContainer) {
  var splitTimes = timeContainer.innerText.split(':');
  var last = splitTimes.length - 1;
  while(splitTimes[last] == "00" || splitTimes[last] == "0") {
    if (last == 0) {
      terminateTimer(timeContainer);
      return;
    }
    last -= 1;
  }
  subtract(splitTimes, last);
  var timeString = "";
  for (var i = 0; i < splitTimes.length; i++) {
    timeString += splitTimes[i];
    if (i != splitTimes.length - 1) {
      timeString += ":";
    }
  }
  timeContainer.innerText = timeString;
}

/**
 * Subtracts 1 from each element in the split array of strings.
 *
 * @param split Array of string numbers for ["HH", "MM". "SS"] of the time
 * @param startIndex Index to start decrementing by 1.
 */
function subtract(split, startIndex) {
  for(var i = startIndex; i < split.length; i++) {
    split[i] = subtractOne(split[i]);
  }
}

/**
 * Subtracts 1 second from a string representation of time
 *
 * @param timeString Input string represntation of time
 * @return String representation of time that is one second less than input
 */
function subtractOne(timeString) {
  if (timeString == "00") {
    return "59";
  }
  var timeInt = parseInt(timeString) - 1;
  if (timeInt < 10) {
    return "0" + timeInt.toString();
  }
  return timeInt.toString();
}

/**
 * Stops the timer and changes the timer display to show it has ended.
 *
 * @param timeContainer Div containing the time display.
 */
function terminateTimer(timeContainer) {
  timeContainer.classList.remove('timer-display');
  timeContainer.innerHTML = "<p style=\'display: inline-block\'>Timer has ended.</p>";
  clearInterval(timer);
  existingTimer = false;
}

/**
 * Starts the timer with time indicated in the JSON output
 *
 * @param outputAsJson JSON output containing all timer information from backend
 */
function initiateTimer(outputAsJson) { 
  var allTimers = document.getElementsByClassName("timer-display");
  var timeContainer = allTimers[allTimers.length - 1];
  var audio = new Audio('audio/timerStart.wav');
  audio.play();
  timer = setInterval(decrementTime, 1000, timeContainer);
  setTimeout(function(){
    var audio = new Audio('audio/timerEnd.wav');
    audio.play();
  }, getTime(timeContainer.innerText));
}

/**
 * Finds the numerical time in seconds based on the input time string.
 *
 * @param timeString Time represented as a string
 * @return Time represented as an integer for number of seconds.
 */
function getTime(timeString) {
  var splitTimes = timeString.split(':');
  var totalTime = 0;
  for (var i = 0; i < splitTimes.length; i++) {
    totalTime += parseInt(splitTimes[i]) * Math.pow(60, splitTimes.length - 1 - i);
  }
  return totalTime * 1000;
}