var existingTimer = false;
var timer = null;

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

function subtract(split, startIndex) {
  for(var i = startIndex; i < split.length; i++) {
    split[i] = subtractOne(split[i]);
  }
}

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

function terminateTimer(timeContainer) {
  timeContainer.classList.remove('timer-display');
  timeContainer.innerHTML = "<p style=\'display: inline-block\'>Timer has ended.</p>";
  clearInterval(timer);
  existingTimer = false;
}

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

function getTime(timeString) {
  var splitTimes = timeString.split(':');
  var totalTime = 0;
  for (var i = 0; i < splitTimes.length; i++) {
    totalTime += parseInt(splitTimes[i]) * Math.pow(60, splitTimes.length - 1 - i);
  }
  return totalTime * 1000;
}