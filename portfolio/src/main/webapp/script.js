// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
/**
 * Starts listening to user input and fetches user input string
 */
 
 
const record = document.querySelector('.record');
const stop = document.querySelector('.stop');
const canvas = document.querySelector('.visualizer');
const mainSection = document.querySelector('.main-controls');
 
// disable stop button while not recording
 
stop.disabled = true;
 
// visualiser setup - create web audio api context and canvas
 
let audioCtx;
const canvasCtx = canvas.getContext("2d");
 
//main block for doing the audio recording
 
if (navigator.mediaDevices.getUserMedia) {
  console.log('getUserMedia supported.');
 
  const constraints = { audio: true };
  let chunks = [];
 
  let onSuccess = function(stream) {
    const mediaRecorder = new MediaRecorder(stream);
 
    visualize(stream);
 
    record.onclick = function() {
      mediaRecorder.start(1000);
      console.log(mediaRecorder.state);
      console.log("recorder started");
      record.style.background = "red";
 
      stop.disabled = false;
      record.disabled = true;
    }
 
    stop.onclick = function() {
      mediaRecorder.stop();
      console.log(mediaRecorder.state);
      console.log("recorder stopped");
      record.style.background = "";
      record.style.color = "";
 
      stop.disabled = true;
      record.disabled = false;
    }
 
    mediaRecorder.onstop = function(e) {
      console.log("data available after MediaRecorder.stop() called.");
      const blob = new Blob(chunks, { 'type' : 'audio/ogg; codecs=opus' });
 
      blob.lastModifiedDate = new Date();
      blob.name = "name";
      getResponseFromAudio(blob);
 
      chunks = [];
      console.log("recorder stopped");
    }
 
    mediaRecorder.ondataavailable = function(e) {
      chunks.push(e.data);
    }
  }
 
  let onError = function(err) {
    console.log('The following error occured: ' + err);
  }
 
  navigator.mediaDevices.getUserMedia(constraints).then(onSuccess, onError);
 
} else {
   console.log('getUserMedia not supported on your browser!');
}
 
function visualize(stream) {
  if(!audioCtx) {
    audioCtx = new AudioContext();
  }
 
  const source = audioCtx.createMediaStreamSource(stream);
 
  const analyser = audioCtx.createAnalyser();
  analyser.fftSize = 2048;
  const bufferLength = analyser.frequencyBinCount;
  const dataArray = new Uint8Array(bufferLength);
 
  source.connect(analyser);
 
  draw()
 
  function draw() {
    const WIDTH = canvas.width
    const HEIGHT = canvas.height;
 
    requestAnimationFrame(draw);
 
    analyser.getByteTimeDomainData(dataArray);
 
    canvasCtx.fillStyle = "rgba(42, 42, 42, 0)";
    canvasCtx.fillRect(0, 0, WIDTH, HEIGHT);
    canvasCtx.clearRect(0, 0, WIDTH, HEIGHT);

    var gradient = canvasCtx.createLinearGradient(0, 0, WIDTH, 0);
    gradient.addColorStop("0", "#DB4437");
    gradient.addColorStop("0.33", "#F4B400");
    gradient.addColorStop("0.66", "#0F9D58");
    gradient.addColorStop("1.0", "#4285F4");

    canvasCtx.lineWidth = 2;
    canvasCtx.strokeStyle = gradient;
 
    canvasCtx.beginPath();
 
    let sliceWidth = WIDTH * 1.0 / bufferLength;
    let x = 0;
 
 
    for(let i = 0; i < bufferLength; i++) {
 
      let v = dataArray[i] / 128.0;
      let y = v * HEIGHT/2;
 
      if(i === 0) {
        canvasCtx.moveTo(x, y);
      } else {
        canvasCtx.lineTo(x, y);
      }
 
      x += sliceWidth;
    }
 
    canvasCtx.lineTo(canvas.width, canvas.height/2);
    canvasCtx.stroke();
 
  }
}
 
window.onresize = function() {
  canvas.width = mainSection.offsetWidth;
}
 
window.onresize();
 
function getResponseFromAudio(blob) {
  const formData = new FormData();
  formData.append('audio-file', blob);
 
  fetch('/audio-input', {
    method: 'POST',
    body: blob
  }).then(response => response.text()).then(stream => displayChat(stream));
}
function getResponseFromText() {
  var input = document.getElementById('text-input').value;
 
  fetch('/text-input?request-input=' + input, {
    method: 'POST'
  }).then(response => response.text()).then(stream => displayChat(stream));
  var frm = document.getElementsByName('input-form')[0];
  frm.reset();
}

function displayChat(stream) {
  var outputAsJson = JSON.parse(stream);
  placeUserInput(outputAsJson.userInput);
  placeFulfillmentResponse(outputAsJson.fulfillmentText);
}

function placeUserInput(text) {
  placeObject("<p>" + text + "</p>", "user-side");
}

function placeFulfillmentResponse(text) {
  placeObject("<p>" + text + "</p>", "assistant-side");
}

function placeDisplay(text) {
  placeObject(text, "media-display");
}

function placeObject(text, type) {
  var container = document.getElementsByName("convo-container")[0];
  container.innerHTML += ("<div class='" + type + "'>" + text + "</div><br>")
  updateScroll();
}

function updateScroll() {
  var element = document.getElementById("content");
  element.scrollTop = element.scrollHeight;
}