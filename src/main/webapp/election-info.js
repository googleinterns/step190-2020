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

}


