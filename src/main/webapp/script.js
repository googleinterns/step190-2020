const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
                'August', 'September', 'October', 'November', 'December'];
const OCD_STATE_TITLE = 'state:';
const OCD_DISTRICT_TITLE = 'district:';
const STATE_CODE_LENGTH = 2;

/**
 * Fetch the list of upcoming elections and display relevant ones according
 * to the state selected by the user.
 */
function listElections() {
  let dropdownSelection = document.getElementById('select-state');
  let selectedStateId = dropdownSelection.options[dropdownSelection.selectedIndex].value;
  let selectedStateName = dropdownSelection.options[dropdownSelection.selectedIndex].text;
  let nationalElections = [];
  let stateElections = [];

  if (selectedStateId === undefined) {
    return;
  }

  fetch('/election')
    .then(response => response.json())
    .then((textResponse) => {
      let electionList = JSON.parse(textResponse).elections;

      // For every election returned, store it if the state matches or if national election,
      // to later display the details to the user.
      electionList.forEach((election) => {
        let ocdId = election.ocdDivisionId;
        let stateId = ocdId.indexOf(OCD_STATE_TITLE);
        let districtId = ocdId.indexOf(OCD_DISTRICT_TITLE);

        // ocdDivisionId is in the form "ocd-division/country:us/state:<state_code>"
        // For example, Lousiana's ID is "ocd-division/country:us/state:la"
        if (stateId !== -1) {
          if (ocdId.substring(stateId, stateId + OCD_STATE_TITLE.length + STATE_CODE_LENGTH)
              == selectedStateId) {
            stateElections.push(election);
          }
        } else if (districtId !== -1) {
          if (ocdId.substring(districtId, districtId + OCD_DISTRICT_TITLE.length + STATE_CODE_LENGTH) 
              == selectedStateId) {
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