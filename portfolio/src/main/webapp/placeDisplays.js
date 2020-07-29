var isUserLoggedIn = false;

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

function placeChatContainer(text, type, side, container) {
  var newDiv = document.createElement('div');
  newDiv.setAttribute("style", "float: " + side + "; width: 100%;");
  newDiv.innerHTML = "<div class='" + type + "'>" + text + "</div>";
  container.appendChild(newDiv);
  updateScroll();
  return container;
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