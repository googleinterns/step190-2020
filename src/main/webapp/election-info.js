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
