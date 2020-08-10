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
  // TODO(anooshree): create map
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

function populateClassesForTemplate(){
  let contests = [];
  let candidates = [];
  let referendums = [];

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
