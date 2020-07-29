var mapOutputAsJson;
function displayMap(stream) {
  mapOutputAsJson = JSON.parse(stream);
  showMap();
}

function locationMap(placeQuery) {
  var place = JSON.parse(placeQuery);
  var limit = place.limit;
  var mapCenter = new google.maps.LatLng(place.lat, place.lng);
  let {mapDiv, newMap} = createMapDivs(limit, false);

  var map = new google.maps.Map(newMap, {
    zoom: 8,
    center: mapCenter
  });

  var marker = new google.maps.Marker({
    position: mapCenter,
    map: map,
  });

  return mapDiv;
}

var service;
var infowindow;
var limit;
var rightPanel;
var placesList;
var placesDict = new Map();
var markerMap = new Map();
var moreButton;

function nearestPlacesMap(placeQuery) {
  var place = JSON.parse(placeQuery);
  limit = place.limit;
  var mapCenter = new google.maps.LatLng(place.lat, place.lng);

  let {mapDiv, newMap} = createMapDivs(limit, true);
  
  var map = new google.maps.Map(newMap, {
    center: mapCenter,
    zoom: 15
  });

  var request = {
    location: mapCenter,
    radius: '500',
    query: place.attractionQuery
  };

  service = new google.maps.places.PlacesService(map);
  if (place.limit > 0) {
    service.textSearch(request, function(results, status) {
      if (status == google.maps.places.PlacesServiceStatus.OK) {
        createMarkers(results, map, limit);
      }
    });
  } else {
    var getNextPage = null;
    createMoreButton();
    moreButton.onclick = function() {
      moreButton.disabled = true;
      if (getNextPage) getNextPage();
    };
    service.textSearch(request, function(results, status, pagination) {
      if (status !== 'OK') return;
      createMarkers(results, map, results.length);
      moreButton.disabled = !pagination.hasNextPage;
      if (moreButton.disabled) {
        moreButton.style.display = "none";
      }
      getNextPage = pagination.hasNextPage && function() {
        pagination.nextPage();
      };
    });
  }
  return mapDiv;
}

function standardCallback(results, status) {
  if (status == google.maps.places.PlacesServiceStatus.OK) {
    createMarkers(results, map);
  }
}

function createMapDivs(limit, makePanel) {
  mapDiv = document.createElement('div');
  mapDiv.classList.add('media-display');

  newMap = document.createElement('div');
  newMap.id = 'map';
  mapDiv.append(newMap);
  
  if (makePanel) {
    rightPanel = document.createElement('div');
    rightPanel.id = 'right-panel';
    mapDiv.appendChild(rightPanel);

    resultTitle = document.createElement('h3');
    resultText = document.createTextNode('Results');
    resultTitle.appendChild(resultText);
    rightPanel.appendChild(resultTitle);

    placesList = document.createElement('ul');
    placesList.id = 'places';
    rightPanel.appendChild(placesList);
  }

  return {mapDiv, newMap};
}

function createMoreButton() {
  moreButton = document.createElement('button');
  moreButton.id = 'more';
  moreButton.innerHTML = 'More results';
  rightPanel.appendChild(moreButton);
  return moreButton;
}

function createMarkers(places, map, limit) {
  var bounds = new google.maps.LatLngBounds();

  for (var i = 0; i < places.length && i < limit; i++) {
    var place = places[i];
    var infowindow = new google.maps.InfoWindow({content: place.name});
    var marker = new google.maps.Marker({
      map: map,
      position: place.geometry.location,
      info: infowindow
    });
    markerMap.set(marker, map);
    marker.addListener('click', function() {
      if (isInfoWindowOpen(this.info)) {
        this.info.close(markerMap.get(this), this);
      } else {
        this.info.open(markerMap.get(this), this);
      }
    });
    
    var li = document.createElement('li');
    li.classList.add('patterned');
    li.classList.add('clickable');
    li.textContent = place.name;
    placesList.appendChild(li);
    placesDict.set(li, marker);
    li.addEventListener('click', function() {
      var liMarker = placesDict.get(this);
      if (isInfoWindowOpen(liMarker.info)) {
        liMarker.info.close(markerMap.get(liMarker), liMarker);
      } else {
        liMarker.info.open(markerMap.get(liMarker), liMarker);
      }
    });

    bounds.extend(place.geometry.location);
  }
  map.fitBounds(bounds);
}

function isInfoWindowOpen(infoWindow) {
  var map = infoWindow.getMap();
  return (map !== null && typeof map !== "undefined");
}