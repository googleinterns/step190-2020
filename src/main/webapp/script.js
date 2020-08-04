const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
                'August', 'September', 'October', 'November', 'December'];


function onElectionListLoad(){
  let searchParams = new URLSearchParams(window.location.search);
  let selectedState = searchParams.get("state");
  if(selectedState != null){
    document.getElementById('select-state').value = selectedState;
    listElections();
  }
}

function onElectionInfoLoad(){
  let searchParams = new URLSearchParams(window.location.search);
  let selectedElection = searchParams.get("electionName"); 

  if(selectedElection != null){
    const chosenElection = document.getElementById('chosen-election');
    chosenElection.innerText = selectedElection;
  }
}

function onWelcomePageLoad(){
  let links = [document.getElementById("home-link"), document.getElementById("elections-list-link")];
  updateLinksWithQueryParams(links);
}

/**
 * Fetch the list of upcoming elections and display relevant ones according
 * to the state selected by the user.
 * 
 * TODO: Write tests to check that the expected election information shows up
 */
function listElections() {
  let dropdownSelection = document.getElementById('select-state');
  let selectedStateId = dropdownSelection.options[dropdownSelection.selectedIndex].value;
  let selectedStateName = dropdownSelection.options[dropdownSelection.selectedIndex].text;
  let nationalElections = [];
  let stateElections = [];
 
  // Registering state selection as a query parameter
  addQueryParameter("state", selectedStateId);

  if (selectedStateId === undefined || selectedStateName === undefined) {
    return;
  }

  fetch('/election')
    .then(response => response.json())
    .then((electionList) => {
      // ocdDivisionId is in the form "ocd-division/country:us/state:<state_code>"
      // For example, Lousiana's ID is "ocd-division/country:us/state:la"
      let regex = /ocd\-division\/country\:us(\/(state|district)\:([a-z][a-z]))?/;

      // For every election returned, store it if the state matches or if national election,
      // to later display the details to the user.
      electionList.forEach((election) => {
        let ocdId = election.scope;

        // Match the OCD Division ID with the regular expressions to check if this election
        // is a state election, district election, or national election.
        let regexMatch = regex.exec(ocdId);

        if (regexMatch != null && regexMatch[3] == selectedStateId) {
          stateElections.push(election);
        } else if (regexMatch != null && regexMatch[1] == undefined) {
          nationalElections.push(election);
        }
      });

      let source = document.getElementById('election-list-template').innerHTML;
      let template = Handlebars.compile(source);
      let context = { state: selectedStateName, 
                      stateElections: stateElections, 
                      nationalElections: nationalElections };

      let electionListContainerElement = document.getElementById('election-list-content');
      electionListContainerElement.innerHTML = template(context);
  });
  updateLinksWithQueryParams(document.links);
}

/**
 * Update the current URL with a query parameter defined by the 
 * provided key/value pair.
 * 
 * @param {String} key of the query parameter
 * @param {String} value of the query parameter
 */
function addQueryParameter(key, value){
  let currentURL = new URL(window.location.href);
  let searchParameters = currentURL.searchParams;
  searchParameters.set(key, value);
  currentURL.search = searchParameters.toString();
  let newURL = currentURL.toString();
  window.history.pushState({path: newURL},'',newURL);
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
                    `${streetNumber} ${route} ${city} ${state} ${zipcode} ${country}`);
}

/**
 * Redirect to a given URL while maintaining the query string
 * and tracking the name, ID of the chosen state election
 */
function redirectToStateElectionPage(electionId){
  let electionName = document.getElementById('state-election-name').innerText;
  goToElectionPage(electionName, electionId);
}

/**
 * Redirect to a given URL while maintaining the query string
 * and tracking the name, ID of the chosen national election
 */
function redirectToNationalElectionPage(electionId){
  let electionName = document.getElementById('national-election-name').innerText;
  goToElectionPage(electionName, electionId);
}

/**
 * Redirect to third page and update the query string with election info.
 */
function goToElectionPage(electionName, electionId){
  addQueryParameter("electionName", electionName);
  addQueryParameter("electionId", electionId);

  var query = window.location.search;
  window.location.href = "electionInfo.html" + query;
}

/**
 * Updates provided list of links on the page to have query parameters in the current URL.
 * 
 * @param {List} anchorsList list of links to apply changes to  
 */
function updateLinksWithQueryParams(anchorsList){
  let currentURL = new URL(window.location.href);
  let searchParams = currentURL.searchParams;
  for(let i = 0; i < anchorsList.length; i++){
    let linkURL = new URL(anchorsList[i].href);
    let linkSearchParams = linkURL.searchParams;
    searchParams.forEach(function(value, key){
      linkSearchParams.set(key, value);
    });
    linkURL.search = linkSearchParams.toString();
    anchorsList[i].href = linkURL.toString();
  }
}

/**
 * Change the format of the date returned by the Google Civic Information
 * API to be the standard date format (ex. 2020-08-16 ==> August 16, 2020).
 *
 * @param {String} apiDate date in the form YYYY-MM-DD
 * @return {String} the formatted date in the form Month DD, YYYY
 */
Handlebars.registerHelper("formatDate", function(apiDate) {
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

  return MONTHS[monthNum - 1] + ' ' + parseInt(dateParts[2]) + ', ' + dateParts[0];
});
