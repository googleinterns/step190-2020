// The config file contains the unrestricted key for use on localhost
var mapKey = (typeof config === 'undefined' || config === null) 
                ? 'AIzaSyCBrkGl891qgFHNFz3VGi3N-CEXGLp-DIU' : config.PLACES_API_KEY;

var script = document.createElement('script');
script.type = 'text/javascript';
script.src = 'https://maps.googleapis.com/maps/api/js?key='
             + mapKey + '&callback=initializeMap&libraries=&v=weekly';
script.defer = true;

document.head.appendChild(script);

function onElectionInfoLoad(){
  let searchParams = new URLSearchParams(window.location.search);
  let selectedElection = searchParams.get("electionName"); 

  if(selectedElection != null){
    const chosenElection = document.getElementById('chosen-election');
    chosenElection.innerText = selectedElection;
  }
}

/**
 * Add the user's address (autocompleted by the Places API) to the 
 * query URL
 */
function logAddressInput(){
  let streetNumber = document.getElementById('street_number').value;
  let route = document.getElementById('route').value;
  let city = document.getElementById('locality').value;
  let state = document.getElementById('administrative_area_level_1').value;
  let zipCode = document.getElementById('postal_code').value;
  let country = document.getElementById('country').value;

  addQueryParameter("address", 
                    `${streetNumber} ${route} ${city} ${state} ${zipCode} ${country}`);

  callInfoCardServlet();
  populateClassesForTemplate();
}

/**
 * Call GET on the Info Card Servlet to retrieve the information needed to populate
 * this page
 */
function callInfoCardServlet(){
  let response = await fetch('/info-cards');

  if (response.ok) { // if HTTP-status is 200-299
    console.log('Called Info Card servlet successfully');
  } else {
    alert("HTTP-Error: " + response.status);
  }
}

/**
 * Make a GET request to the ContestsServlet and then update the values of the arrays
 * used by the Handlebars template.
 */
function populateClassesForTemplate(){
  let contests = [];
  let candidates = [];
  let referendums = [];

  fetch('/contests')
    .then(response => response.json())
    .then((JSONobject) => {
      JSONobject.contests.forEach((contest) => {
        contests.push(contest);

        contest.candidates.forEach((candidate) => {
          candidates.push(candidate);
        });
      });

      JSONobject.referendums.forEach((referendum) => {
          referendums.push(referendum);
      });

      let source = document.getElementById('contests-referendums-template').innerHTML;
      let template = Handlebars.compile(source);
      let context = { contests: contests, 
                      candidates: candidates, 
                      referendums: referendums };

      let infoCardContainerElement = document.getElementById('contests-referendums-content');
      infoCardContainerElement.innerHTML = template(context);
  });
}

$(document).ready(function(){
    $('#address-input-js').on('click',initializeMap)
});

var geocoder;
function initializeMap() {
  geocoder = new google.maps.Geocoder();

  const urlParams = new URLSearchParams(window.location.search);
  const address = urlParams.get('address');
  
  map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: 37.3382, lng: -121.8863 },
    zoom: 8
  });

  fetch('/polling-stations')
    .then(response => response.json())
    .then((pollingStationList) => {
      pollingStationList.forEach((pollingStation) => {
        // addPollingStation call
      });
    });
}

/**
 * Adds a polling station as a marker on the map, with an info card
 * that displays upon click to show more information about the station
 */
function addPollingStation(address, map, title, description) {
  geocoder.geocode({
    componentRestrictions: {country: 'US'},
    'address': address}, function(results, status) {
      if (status == 'OK') {
        addLandmark(map, results[0].geometry.location, title, description);
      } else {
        alert('Geocode was not successful for the following reason: ' + status);
      }
  });
}

/** 
 * Adds a marker that shows an info window when clicked. 
 */
function addLandmark(map, position, title, description) {
  const marker = new google.maps.Marker(
      {position: position, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: description});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}
