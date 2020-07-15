indexStart = 0;
indexEnd = 5;
numTotalVideos = 25;

function workoutVideos(videoQuery) {
  videos = JSON.parse(videoQuery);
  let workoutDiv = createVideoDivs(videos, indexStart, indexEnd);
  return workoutDiv;
}

/**
* Creates video page divs and adds extra video pages if possible
*
* @param videos JSON object of all videos
* @param indexStart index of first video on page
* @param indexEnd index of last video on page + 1
*/
function createVideoDivs(videos, indexStart, indexEnd) {
  
  workoutDiv = document.createElement("div");
  workoutDiv.classList.add("media-display");

  videosDiv = document.createElement("div");
  videosDiv.id = "videos";
  workoutDiv.appendChild(videosDiv);

  //Creating previous button
  previousButton = document.createElement("BUTTON");
  previousButton.classList.add("video-buttons");
  previousButton.classList.add("video-previous-button");
  var buttonText = document.createTextNode("Previous");
  previousButton.appendChild(buttonText); 

  //Creating next button 
  nextButton = document.createElement("BUTTON");
  nextButton.classList.add("video-buttons");
  nextButton.classList.add("video-next-button");
  var buttonText = document.createTextNode("Next");
  nextButton.appendChild(buttonText);

  for (var i = indexStart; i < indexEnd; i++) {
    video = videos[i];
    channelName = video.channelTitle;

    title = video.title;
    if (title.length > 80) { 
        title = title.substring(0, 80) + "..."; 
    }

    description = video.description;
    if (description.length > 170) {
        description = description.substring(0, 170) + "...";
    }

    currentIndex = video.currentIndex;
    videosDisplayedPerPage = video.videosDisplayedPerPage;
    currentPage = video.currentPage;
    totalPages = video.totalPages;
    replaceUnicode();

    var videoContainer = document.createElement("div");
    videoContainer.classList.add("video-container");

    //Video Thumbnail
    var videoThumbnail = document.createElement("div");
    videoThumbnail.classList.add("video-thumbnail");

    var videoLink = document.createElement("a");
    videoLink.title = title;
    videoLink.href = video.videoURL.replace(/"/g, "");
    videoLink.target = "_blank";    

    var thumbnailImage = document.createElement("img");
    thumbnailImage.src = video.thumbnail.replace(/"/g, "");
    thumbnailImage.setAttribute("width", "320");
    thumbnailImage.setAttribute("height", "180");
    videoLink.appendChild(thumbnailImage);
    
    videoThumbnail.appendChild(videoLink);
    videoContainer.appendChild(videoThumbnail);

    //Video Information
    var videoInfo = document.createElement("div");
    videoInfo.classList.add("video-info");

    var videoTitleLink = document.createElement("a");
    videoTitleLink.title = title;
    videoTitleLink.href = video.videoURL.replace(/"/g, "");
    videoTitleLink.target = "_blank"; 

    var videoTitle = document.createElement("h3");
    videoTitle.classList.add("video-title");
    videoTitle.innerHTML = title.replace(/"/g, "");
    videoTitleLink.appendChild(videoTitle);
    videoInfo.appendChild(videoTitleLink);

    var channelLink = document.createElement("a");
    channelLink.title = channelName;
    channelLink.href = video.channelURL.replace(/"/g, "");
    channelLink.target = "_blank"; 

    var channelTitle = document.createElement("p");
    channelTitle.classList.add("channel-title");
    channelTitle.innerHTML = channelName.replace(/"/g, "");
    channelLink.appendChild(channelTitle)
    videoInfo.appendChild(channelLink);

    var videoDescription = document.createElement("p");
    videoDescription.classList.add("video-description");
    videoDescription.innerHTML = description.replace(/"/g, "");
    videoInfo.appendChild(videoDescription);

    videoContainer.appendChild(videoInfo);
    videosDiv.appendChild(videoContainer);


    footerDisplay = ((currentIndex + 1) % videosDisplayedPerPage == 0);
    //Create footer with page numbers and buttons under correct video div
    if (footerDisplay) {
        footer = document.createElement("div");
        footer.classList.add("footer");
        videosDiv.appendChild(footer);
        
        //Add page numbers to footer
        var pageNumbers = document.createElement("p");
        pageNumbers.classList.add("video-page-number");
        pageNumbers.innerHTML = currentPage + "/" + totalPages;
        footer.appendChild(pageNumbers);

        //Display previous button if not on first page
        if (currentPage != 1) {
          footer.appendChild(previousButton);
          footer.getElementsByClassName("video-buttons video-previous-button").item(0).onclick = function() {showNewVideosPage(-5)};
        }
        //Display next button if not on last page
        if (currentPage != totalPages) {
          footer.appendChild(nextButton);
          footer.getElementsByClassName("video-buttons video-next-button").item(0).onclick = function() {showNewVideosPage(5)};
        }
    }
  }

  return workoutDiv;
}

function showNewVideosPage(numShiftIndex) {

  //Remove existing divs 
  var mediaDisplayDivs = document.getElementsByClassName('media-display');
  var mediaDiv;
  for (mediaDiv of mediaDisplayDivs) {
      while (mediaDiv.firstChild) {
        mediaDiv.removeChild(mediaDiv.firstChild);
      }
  }
  
  indexStart += numShiftIndex
  indexEnd += numShiftIndex
  let workoutDiv = createVideoDivs(videos, indexStart, indexEnd);
  appendDisplay(workoutDiv);
}

/** Replaces unicode strings with actual characters */
function replaceUnicode() {

    //Properly format apostrophes
    channelName = channelName.replace("\\u0027", "'");
    title = title.replace("\\u0027", "'");
    desciption = description.replace("\\u0027", "'");    

    //Properly format ampersands
    channelName = channelName.replace("\\u0026", "&").replace("\\u0026amp;", "&");
    title = title.replace("\\u0026", "&").replace("\\u0026amp;", "&");
    description = description.replace("\\u0026", "&").replace("\\u0026amp;", "&");
}
