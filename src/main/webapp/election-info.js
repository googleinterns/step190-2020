function onElectionInfoLoad(){
  fetch('/election')
  .then(response => response.json())
  .then((electionList) => {
    let searchParams = new URLSearchParams(window.location.search);
    let electionId = searchParams.get("electionId");

    let titleTextElement = document.getElementById('election-info-page-title');
    let electionInfoWrapperElement = document.getElementById('election-info-wrapper');
    let source = document.getElementById('election-name-template').innerHTML;
    let template = Handlebars.compile(source);
    let context = null;

    electionList.forEach((election) => {
      if (electionId == election.id) {
        context = { 
          electionIdInQuery: true,
          electionName: election.name 
        };
        electionInfoWrapperElement.style.removeProperty('display');
        return;
      }
    });

    if (context == null) {
      context = { 
        electionIdInQuery: false,
      };
      electionInfoWrapperElement.style.display = 'none';
    }

    titleTextElement.innerHTML = template(context);
  });
}

/**
 * Enable the submit button if this element contains text.
 */
setInterval(function() {
  if(document.getElementById('street_number').value == '' 
      || document.getElementById('route').value == ''
      || document.getElementById('locality').value == ''
      || document.getElementById('administrative_area_level_1').value == ''
      || document.getElementById('country').value == ''
      || document.getElementById('postal_code').value == '') { 
    document.getElementById('submit-address-button').disabled = true;
  } else { 
    document.getElementById('submit-address-button').disabled = false;
  }
}, 500);

/**
 * Add the user's address (autocompleted by the Places API) to the 
 * query URL and call functions to populate the info cards
 */
function logAddressInput() {
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

function showSpinner() {
  let spinner = document.getElementById("spinner");
  spinner.className = "show";
}

function hideSpinner() {
  let spinner = document.getElementById("spinner");
  spinner.className = spinner.className.replace("show", "");
}

/**
 * Call PUT on the Info Card Servlet to retrieve the information needed to populate
 * this page
 *
 * @param {String} electionId the id of the user's chosen election
 * @param {String} address the user's address
 */
function callInfoCardServlet(electionId, address){
  showSpinner();
  let servletUrl = "/info-cards?electionId=" + electionId + "&address=" + address;
  fetch(servletUrl, {
    method: 'PUT'
  }).then((response) => {
      if (response.ok) { // if HTTP-status is 200-299
        console.log('Called Info Card servlet successfully');
        populateClassesForTemplate(electionId);
        initializeMap();
        hideSpinner();
      } else {
        alert("HTTP-Error: " + response.status);
      }
  });
}

/**
 * Make a GET request to the ContestsServlet and then update the values of the arrays
 * used by the Handlebars template.
 *
 * @param {String} electionId the id of the user's chosen election
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

      let infoCardContainerElement = document.getElementById('election-info-results');
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


var geocoder;
var map;

/**
 * Add a map to the page centered on the user's address and 
 * containing markers for valid polling stations that display
 * info markers when clicked.
 */
function initializeMap() {
  geocoder = new google.maps.Geocoder();

  const urlParams = new URLSearchParams(window.location.search);
  let servletUrl = "/polling-stations?electionId=" + urlParams.get("electionId");

  fetch(servletUrl)
    .then(response => response.json())
    .then((pollingStationList) => {
      if (pollingStationList === undefined || pollingStationList.length == 0) {
        document.getElementById("polling-stations-map").style.display = "none";
        document.getElementById("no-polling-stations").innerText = 
          "Sorry, we can't find any polling stations for this election near your address.";
        console.log("displayed polling station message");
        return;
      }

      document.getElementById("no-polling-stations").innerText = "";
      document.getElementById("polling-stations-map").style.display = "block";
      
      const address = urlParams.get('address');
      
      geocoder.geocode( { 'address': address}, function(results, status) {
        if (status == 'OK') {
          map = new google.maps.Map(document.getElementById("polling-stations-map"), {
            center: results[0].geometry.location,
            zoom: 13
          });
          console.log("Created map");
          
          const homeMarker = new google.maps.Marker({
            position:results[0].geometry.location, 
            map:map,
            icon: "https://img.icons8.com/ios-filled/50/000000/home.png",
            draggable: false
          });
          console.log("Created user address marker");
          
          pollingStationList.forEach((pollingStation) => {

            let descriptionTemplate =  
              `<div id="content">
                <div id="siteNotice"></div> 
                <h3 id="firstHeading" class="firstHeading"> {{pollingStation.name}} </h3> 
                <div id="bodyContent">
                <p> {{pollingStation.address}} </p>
                  <p>Open {{pollingStation.pollingHours}} beginning {{pollingStation.startDate}}
                   and until {{pollingStation.endDate}}.</p>
                  <p> {{findType pollingStation.locationType}} </p>
                  {{#if pollingStation.sources}}
                    <p><i> Sources: {{withCommas pollingStation.sources}}</i></p>
                  {{/if}}
                </div>
              </div>`;

            let template = Handlebars.compile(descriptionTemplate);
            let context = {pollingStation: pollingStation};
            let description = template(context);
    
            addPollingStation(pollingStation.address, map, pollingStation.name, description);
            console.log("Added polling station marker");
          });
        } else {
          alert('Geocode was not successful for the following reason: ' + status);
        }
      });
    });
}

/**
 * Adds a polling station as a marker on the map, with an info card
 * that displays upon click to show more information about the station
 *
 * @param {String} address the user's address
 * @param {google.maps.Map} map the map to add polling stations to
 * @param {String} title the title of the polling station marker
 * @param {String} description info window content for the marker
 */
function addPollingStation(address, map, title, description) {
  geocoder.geocode({
    componentRestrictions: {country: 'US'},
    'address': address}, function(results, status) {
      if (status == 'OK') {
        addPollingStationMarker(map, results[0].geometry.location, title, description);
      } else {
        alert('Geocode was not successful for the following reason: ' + status);
      }
  });
}

/** 
 * Adds a marker that shows an info window when clicked. 
 *
 * @param {google.maps.Map} map the map to add polling stations to
 * @param {Position} position the latitude and longitude of the marker's location
 * @param {String} title the title of the polling station marker
 * @param {String} description info window content for the marker
 */
function addPollingStationMarker(map, position, title, description) {
  const marker = new google.maps.Marker(
      {position: position, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: description});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/**
 * Handlebars helper that removes the beginning "http://", "https://" and the trailing "/"
 * from the supplied URL string.
 * 
 * @param {String} URL to be stripped.
 */
Handlebars.registerHelper('stripUrl', function(urlString){
  urlString = urlString.replace(/(^\w+:|^)\/\/(www.)?/, '');
  if(urlString[urlString.length - 1] == '/'){
    urlString = urlString.slice(0, -1);
  }
  return urlString;
})

/** 
 * Handlebars helper that converts an array into a comma-separated string
 * 
 * @param {Array} array to be converted
 */
Handlebars.registerHelper('withCommas', function(sourcesList){
  return sourcesList.join(", ");
})

/**
 * Handlebars helper that convverts the locationType 
 * of a PollingStation object into a user-friendly string
 * 
 * @param {String} the locationType
 */
Handlebars.registerHelper('findType', function(locationType){
  if (locationType == "dropOffLocation") {
    return "Drop off only.";
  } else if (locationType == "earlyVoteSite") {
    return "Early vote site only.";
  } else if (locationType == "pollingLocation") {
    return "Standard polling location.";
  }
})
