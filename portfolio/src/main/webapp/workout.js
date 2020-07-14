function workoutVideos(videoQuery) {
  var videos = JSON.parse(videoQuery);
  let workoutDiv = createVideoDivs(videos);
  return workoutDiv;
}

/**
* Creates video page divs and adds extra video pages if possible
*
* @param videos JSON object of all videos
*/
function createVideoDivs(videos) {
  
  workoutDiv = document.createElement('div');
  workoutDiv.classList.add('media-display');

  videosDiv = document.createElement('div');
  videosDiv.id = 'videos';
  workoutDiv.appendChild(videosDiv);

  //Creating more button 
  moreButton = document.createElement("BUTTON");
  moreButton.classList = "video-more-button";
  var buttonText = document.createTextNode("More");
  moreButton.appendChild(buttonText);

  for (var i = 0; i < 5; i++) {
    video = videos[i];
    channelName = video.channelTitle;
    title = video.title;
    description = video.description;
    currentIndex = video.currentIndex;
    videosDisplayedPerPage = video.videosDisplayedPerPage;
    currentPage = video.currentPage;
    totalPages = video.totalPages;
    replaceUnicode();

    var videoContainer = document.createElement('div');
    videoContainer.classList = 'video-container';

    //Video Thumbnail
    var videoThumbnail = document.createElement('div');
    videoThumbnail.classList = 'video-thumbnail';

    var videoContainer = document.createElement('div');
    videoContainer.classList = 'video-container';

    //Video Thumbnail
    var videoThumbnail = document.createElement('div');
    videoThumbnail.classList = 'video-thumbnail';

    var videoLink = document.createElement('a');
    videoLink.title = title;
    videoLink.href = video.videoURL.replace(/"/g, '');
    videoLink.target = "_blank";    

    var thumbnailImage = document.createElement('img');
    thumbnailImage.src = video.thumbnail.replace(/"/g, '');
    thumbnailImage.setAttribute("width", "320");
    thumbnailImage.setAttribute("height", "180");
    videoLink.appendChild(thumbnailImage);
    
    videoThumbnail.appendChild(videoLink);
    videoContainer.appendChild(videoThumbnail);

    //Video Information
    var videoInfo = document.createElement('div');
    videoInfo.classList = 'video-info';

    var videoTitleLink = document.createElement('a');
    videoTitleLink.title = title;
    videoTitleLink.href = video.videoURL.replace(/"/g, '');
    videoTitleLink.target = "_blank"; 

    var videoTitle = document.createElement('h3');
    videoTitle.classList = 'video-title';
    videoTitle.innerHTML = title.replace(/"/g, '');
    videoTitleLink.appendChild(videoTitle);
    videoInfo.appendChild(videoTitleLink);

    var channelLink = document.createElement('a');
    channelLink.title = channelName;
    channelLink.href = video.channelURL.replace(/"/g, '');
    channelLink.target = "_blank"; 

    var channelTitle = document.createElement('p');
    channelTitle.classList = 'channel-title';
    channelTitle.innerHTML = channelName.replace(/"/g, '');
    channelLink.appendChild(channelTitle)
    videoInfo.appendChild(channelLink);

    var videoDescription = document.createElement('p');
    videoDescription.classList = 'video-description';
    videoDescription.innerHTML = description.replace(/"/g, '');
    videoInfo.appendChild(videoDescription);

    videoContainer.appendChild(videoInfo);
    videosDiv.appendChild(videoContainer);

    //Check if more button should be displayed
    console.log("currentIndex: " + currentIndex);
    console.log("currentPage: " + currentPage);
    
    displayMoreButton = (currentPage != totalPages) && ((currentIndex + 1) % videosDisplayedPerPage == 0);
    console.log(displayMoreButton);

    //Display more button if not on last page
    if (displayMoreButton) {
      videosDiv.appendChild(moreButton);
    }
  }

  return workoutDiv;
}

/** Replaces unicode strings with actual characters */
function replaceUnicode() {

    //Properly format apostrophes
    channelName = channelName.replace("\\u0027", "'");
    title = title.replace("\\u0027", "'");
    desciption = description.replace("\\u0027", "'");    

    //Properly format ampersands
    channelName = channelName.replace("\\u0026", "&").replace("\\u0026amp;", "&");
    title = title.replace("\\u0026", "&").replace("\\u0026amp;", "&");;
    description = description.replace("\\u0026", "&").replace("\\u0026amp;", "&");
}
