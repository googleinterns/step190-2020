const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
                'August', 'September', 'October', 'November', 'December'];

function onElectionInfoLoad(){
  fetch('/election')
  .then(response => {
    if (response.ok) { // if HTTP-status is 200-299
      return response.json();
    } else {
      Promise.reject(response.text());
    }
  })
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
      }
    });

    if (context == null) {
      context = { 
        electionIdInQuery: false,
      };
      electionInfoWrapperElement.style.display = 'none';
    }

    titleTextElement.innerHTML = template(context);
  })
  .catch(() => alert('There has been an error.'));
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
  
  callInfoCardServlet(searchParams.get("electionId"), searchParams.get("address"), 
                      searchParams.get("state"));
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
function callInfoCardServlet(electionId, address, state){
  showSpinner();
  let servletUrl = "/info-cards?electionId=" + electionId + "&address=" + address;
  fetch(servletUrl, {
    method: 'PUT'
  }).then((response) => {
      let errorTextElement = document.getElementById('address-error-text');
      if (response.ok) { // if HTTP-status is 200-299
        console.log('Called Info Card servlet successfully');
        errorTextElement.style.display = "none";
        populateDeadlines(state);
        populateClassesForTemplate(electionId);
        initializeMap();
      } else {
        errorTextElement.style.display = "block";
        document.getElementById('polling-stations-map').style.height = '0';
        document.getElementById('dates-and-deadlines').innerHTML = '';
        document.getElementById('dates-and-deadlines').style.display = 'none';
        document.getElementById('no-polling-stations').innerHTML = '';
        document.getElementById('election-info-results').innerHTML = '';
        hideSpinner();
      }
  });
}

/**
 * Call GET on the Deadlines Servlet to retrieve the registration and mail-in 
 * deadlines for the user and populate the information used in the Handlebars template
 * 
 * @param {String} state the user's state
 */
function populateDeadlines(state) {
  let primaryDeadlines = [];
  let runOffDeadlines = [];
  let generalDeadlines = [];

  let servletUrl = `/deadlines?state=${state}`;

  fetch(servletUrl) 
    .then(response => response.json(servletUrl))
    .then((JSONobject) => {
      JSONobject.myArrayList.forEach((deadline) => {
        let electionType = deadline['map']['election-type'];
        if(electionType == "General Election") {
          generalDeadlines.push(deadline.map);
        } else if(electionType == "State Primary") {
          primaryDeadlines.push(deadline.map);
        } else if(electionType == "State Primary Runoff") {
          runOffDeadlines.push(deadline.map);
        }
      });

      let source = document.getElementById('deadlines-template').innerHTML;
      let template = Handlebars.compile(source);
      let context = {generalDeadlines : generalDeadlines,
                     runOffDeadlines : runOffDeadlines,
                     primaryDeadlines : primaryDeadlines};

      let deadlinesContainerElement = document.getElementById('dates-and-deadlines');
      deadlinesContainerElement.innerHTML = template(context);
      deadlinesContainerElement.style.display = 'block';
      console.log("processed deadlines");

      hideSpinner();
      window.scrollTo({
        top: window.innerHeight - 40,
        left: 0,
        behavior: 'smooth'
      });
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

      referendums.sort(function(a, b){return (a.title < b.title) ? -1 : ((a.title > b.title) ? 1 : 0)});

      let source = document.getElementById('contests-referendums-template').innerHTML;
      let template = Handlebars.compile(source);
      let context = { contests: contests, 
                      candidates: candidates, 
                      referendums: referendums };

      let infoCardContainerElement = document.getElementById('election-info-results');
      infoCardContainerElement.innerHTML = template(context);

      document.getElementById('autocomplete').value = '' ;
      document.getElementById('street_number').value = '' ;
      document.getElementById('route').value = '';
      document.getElementById('locality').value = '';
      document.getElementById('administrative_area_level_1').value = '';
      document.getElementById('country').value = '';
      document.getElementById('postal_code').value = '';

      let collapsibles = document.getElementsByClassName("collapsible");

      for (let i = 0; i < collapsibles.length; i++) {
        collapsibles[i].addEventListener("click", function() {
          this.classList.toggle("active");
          let content = this.nextElementSibling;
          if (content.style.height && content.style.height !== '0px'){
            content.style.height = '0px';
          } else {
            content.style.height = '100%'; 
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

  document.getElementById('polling-stations-map').style.height = '600px';

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

/**
 * Handlebars helper that processes information from the FVAP API
 * into a more understandable deadline for the user
 * 
 * @param {String} rule the rule for the deadline, which includes
 *                      how information should be submitted and
 *                      what the due date is for (postmarked, etc.)
 * @param {String} votingType the type of action the voter is taking
 * 
 * @return {String} a statement summarizing the rule for the user
 */
Handlebars.registerHelper('processRule', function(rule, votingType){

  let submissionType = "";
  let dueDateType =  "";

  if(rule.includes(":")) {
    let splitRule = rule.split(":");
    submissionType = splitRule[0].toLowerCase();
    dueDateType = splitRule[1].toLowerCase();
  } else {
    dueDateType = rule.toLowerCase();
    submissionType = "";
  }

  if(votingType == "Ballot Return") {
    votingType = "a ballot";
  }
  if(votingType ==   "Ballot Request") {
    votingType = "a ballot request";
  }
  if(votingType == "Registration") {
    votingType = "voter registration"
  }

  votingType = votingType.toLowerCase();
  votingType = votingType.charAt(0).toUpperCase() + votingType.slice(1);

  dueDateType = dueDateType.replace("*", "");

  return `${votingType} ${submissionType} must be ${dueDateType}`;
})

/**
 * Formats the date returned by the FVAP API
 * (ex. 2020-10-19T00:00:00 to October 19, 2020)
 * 
 * @param {String} date the date value provided by the API
 * 
 * @return {String} a formatted version of the date
 */
Handlebars.registerHelper('formatDate', function(date){
  let apiDate = date.substring(0, date.length - 9);

  if (apiDate == undefined) {
    return 'Invalid date';
  }

  let dateParts = apiDate.split('-');

  if (dateParts.length !== 3) {
    return 'Invalid date';
  }

  let monthNum = parseInt(dateParts[1]);

  if (monthNum > MONTHS.length) {
    return 'Invalid date';
  }

  return `${MONTHS[monthNum - 1]} ${parseInt(dateParts[2])}, ${dateParts[0]}`;
})
