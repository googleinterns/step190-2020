const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
                'August', 'September', 'October', 'November', 'December'];

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

  if (selectedStateId === undefined || selectedStateName === undefined) {
    return;
  }

  fetch('/election')
    .then(response => response.json())
    .then((textResponse) => {
      let electionList = JSON.parse(textResponse).elections;
      // ocdDivisionId is in the form "ocd-division/country:us/state:<state_code>"
      // For example, Lousiana's ID is "ocd-division/country:us/state:la"
      let regex = /ocd\-division\/country\:us\/(state|district)\:([a-z][a-z])/;

      // For every election returned, store it if the state matches or if national election,
      // to later display the details to the user.
      electionList.forEach((election) => {
        let ocdId = election.ocdDivisionId;

        // Match the OCD Division ID with the regular expressions to check if this election
        // is a state election, district election, or national election.
        let stateMatch = regex.exec(ocdId);
        let districtMatch = regex.exec(ocdId);

        if (stateMatch != null) {
          if (stateMatch[1] == selectedStateId) {
            stateElections.push(election);
          }
        } else if (districtMatch != null) {
          if (districtMatch[1] == selectedStateId) {
            stateElections.push(election);
          }
        } else {
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
}

/**
 * Change the format of the date returned by the Google Civic Information
 * API to be the standard date format (ex. 2020-08-16 ==> August 16, 2020).
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