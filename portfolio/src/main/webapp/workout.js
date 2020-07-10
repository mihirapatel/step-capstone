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
  workoutDiv.append(videosDiv);

  for (var i = 0; i < videos.length; i++) {
    var video = videos[i];
    var channelTitle = video.channelTitle;
    var title = video.title;
    var description = video.description;

    var videoLink = document.createElement('a');
    var thumbnailImage = document.createElement('img');
    console.log(video.thumbnail);
    thumbnailImage.src = video.thumbnail;
    thumbnailImage.setAttribute("width", "320");
    thumbnailImage.setAttribute("height", "180");
    videoLink.appendChild(thumbnailImage);
    videoLink.title = title
    videoLink.href = video.videoURL;
    videoLink.target = "_blank";
    videosDiv.appendChild(videoLink);
  }
  
  return workoutDiv;
}
