function onElectionInfoLoad(){
  let searchParams = new URLSearchParams(window.location.search);
  let selectedElection = searchParams.get("electionName"); 

  if (selectedElection != null) {
    let source = document.getElementById('election-name-template').innerHTML;
    console.log("gud")
    let template = Handlebars.compile(source);
    let context = { electionName: selectedElection };

    let titleTextElement = document.getElementById('election-info-page-title');
    console.log("double gud")
    titleTextElement.innerHTML = template(context);
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

  let searchParams = new URLSearchParams(window.location.search);
  
  callInfoCardServlet(searchParams.get("electionId"), searchParams.get("address"));
}

/**
 * Call PUT on the Info Card Servlet to retrieve the information needed to populate
 * this page
 */
function callInfoCardServlet(electionId, address){
  let servletUrl = "/info-cards?electionId=" + electionId + "&address=" + address;
  fetch(servletUrl, {
    method: 'PUT'
  }).then((response) => {
      if (response.ok) { // if HTTP-status is 200-299
        console.log('Called Info Card servlet successfully');
        populateClassesForTemplate(electionId);
      } else {
        alert("HTTP-Error: " + response.status);
      }
  });
}

/**
 * Make a GET request to the ContestsServlet and then update the values of the arrays
 * used by the Handlebars template.
 */
function populateClassesForTemplate(electionId){
  let contests = [];
  let candidates = [];
  let referendums = [];

  let servletUrl = "/contests?electionId=" + electionId;

  fetch(servletUrl)
    .then(response => response.json(servletUrl))
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

      let collapsibles = document.getElementsByClassName("collapsible");

      for (let i = 0; i < collapsibles.length; i++) {
        collapsibles[i].addEventListener("click", function() {
          this.classList.toggle("active");
          let content = this.nextElementSibling;
          if (content.style.maxHeight){
            content.style.maxHeight = null;
          } else {
            content.style.maxHeight = content.scrollHeight + 36 + "px";
          }
        });
      }
  });
}

/*$(document).ready(function(){
    $('#address-input-js').on('click', initializeMap)
});

var geocoder;
var map;

function initializeMap() {
  geocoder = new google.maps.Geocoder();

  const urlParams = new URLSearchParams(window.location.search);
  const address = urlParams.get('address');
  
  geocoder.geocode( { 'address': address}, function(results, status) {
    if (status == 'OK') {
      map = new google.maps.Map(document.getElementById("polling-stations-map"), {
        center: results[0].geometry.location,
        zoom: 8
      });
    } else {
      alert('Geocode was not successful for the following reason: ' + status);
    }
  });

  let servletUrl = "/polling-stations?electionId=" + urlParams.get("electionId");

  fetch(servletUrl)
    .then(response => response.json())
    .then((pollingStationList) => {
      pollingStationList.forEach((pollingStation) => {
        var type;
        if (pollingStation.locationType == "dropOffLocation") {
          type = "Drop off only.";
        } else if (pollingStation.locationType == "earlyVoteSite") {
          type = "Early vote site only.";
        } else if (pollingStation.locationType == "pollingLocation") {
          type = "Standard polling location.";
        }

        const description =
          '<div id="content">' +
          '<div id="siteNotice">' +
          "</div>" +
          '<h1 id="firstHeading" class="firstHeading">'+ pollingStation.name + '</h1>' +
          '<div id="bodyContent">' +
          "<p>Hours: Open " + pollingStation.pollingHours + " beginning " + pollingStation.startDate + 
          " and until " + pollingStation.endDate + ".</p>" +
          "<p>" + type + "</p>" +
          "</div>" +
          "</div>";

        addPollingStation(pollingStation.address, map, pollingStation.name, description);
      });
    });
}*/

/**
 * Adds a polling station as a marker on the map, with an info card
 * that displays upon click to show more information about the station
 */
/*function addPollingStation(address, map, title, description) {
  geocoder.geocode({
    componentRestrictions: {country: 'US'},
    'address': address}, function(results, status) {
      if (status == 'OK') {
        addPollingStationMarker(map, results[0].geometry.location, title, description);
      } else {
        alert('Geocode was not successful for the following reason: ' + status);
      }
  });
}*/

/** 
 * Adds a marker that shows an info window when clicked. 
 */
/*function addPollingStationMarker(map, position, title, description) {
  const marker = new google.maps.Marker(
      {position: position, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: description});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}*/
