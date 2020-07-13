function workoutVideos(videoQuery) {
  var videos = JSON.parse(videoQuery);
  let workoutDiv = createVideoDivs(videos);
  return workoutDiv;
}

function createVideoDivs(videos) {
  
  workoutDiv = document.createElement('div');
  workoutDiv.classList.add('media-display');

  videosDiv = document.createElement('div');
  videosDiv.id = 'videos';
  workoutDiv.appendChild(videosDiv);

  for (var i = 0; i < videos.length; i++) {
    var video = videos[i];
    var channelName = video.channelTitle;
    var title = video.title;
    var description = video.description;

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

    var videoTitle = document.createElement('h3');
    videoTitle.classList = 'video-title';
    videoTitle.innerHTML = title.replace(/"/g, '');;
    videoInfo.appendChild(videoTitle);

    var channelTitle = document.createElement('p');
    channelTitle.classList = 'channel-title';
    channelTitle.innerHTML = channelName.replace(/"/g, '');;
    videoInfo.appendChild(channelTitle);

    var videoDescription = document.createElement('p');
    videoDescription.classList = 'video-description';
    videoDescription.innerHTML = description.replace(/"/g, '');;
    videoInfo.appendChild(videoDescription);

    videoContainer.appendChild(videoInfo);
    videosDiv.appendChild(videoContainer);
  }

  return workoutDiv;
}
