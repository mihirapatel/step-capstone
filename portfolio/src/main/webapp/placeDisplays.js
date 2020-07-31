const streamingContainer = document.getElementsByName('streaming')[0];
var isUserLoggedIn = false;

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

function getLastWord(words) {
    var split = words.split(/[ ]+/);
    console.log(split);
    return split[split.length - 1];
}
 
function placeDisplay(text, type) {
  placeObjectContainer(text, type, "convo-container");
}

function placeObjectContainer(text, type, container) {
  var container = document.getElementsByName(container)[0];
  var newDiv = document.createElement('div');
  newDiv.innerHTML = "<div class='" + type + "'>" + text + "</div>";
  container.appendChild(newDiv);
  updateScroll();
  return container;
}

function placeChatContainer(text, type, side, container, marginBottom) {
  var newDiv = document.createElement('div');
  newDiv.setAttribute("style", "float: " + side + "; width: 100%; margin-bottom:" + marginBottom + "px;");
  newDiv.innerHTML = "<div class='" + type + "'>" + text + "</div>";
  container.appendChild(newDiv);
  updateScroll();
  return newDiv;
}

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

function appendDisplay(div) {
  var container = document.getElementsByName("convo-container")[0];
  container.appendChild(div);
  updateScroll();
  return container;
}

function updateScroll() {
  var element = document.getElementById("content");
  element.scrollTop = element.scrollHeight;
}