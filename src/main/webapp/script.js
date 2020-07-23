const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
                'August', 'September', 'October', 'November', 'December'];
const OCD_STATE_TITLE = 'state:';
const OCD_DISTRICT_TITLE = 'district:';
const STATE_CODE_LENGTH = 2;
const ELECTION_LIST_TITLE = 'Elections coming up in ';
const COUNTRY_NAME = ' the United States';
const NO_ELECTIONS_TEXT = 'No elections coming up soon.';
const LEARN_MORE_BUTTON_TEXT = 'Learn more';
const INVALID_DATE_TEXT = 'Invalid date';

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
          // If the state text is found in ocdDivisionId, then this is a state election.
          if (ocdId.substring(stateId, stateId + OCD_STATE_TITLE.length + STATE_CODE_LENGTH)
              == selectedStateId) {
            stateElections.push(election);
          }
        } else if (districtId !== -1) {
          // If the district text is found in ocdDivisionId, then this is an election in DC.
          if (ocdId.substring(districtId, districtId + OCD_DISTRICT_TITLE.length + STATE_CODE_LENGTH) 
              == selectedStateId) {
            stateElections.push(election);
          }
        } else {
          // No state or district text found, so this is a national election
          nationalElections.push(election);
        }
      });
      
      const electionListContainerElement = document.getElementById('election-list-content');
      electionListContainerElement.innerHTML = '';
      
      const electionsInStateHeadingElement = document.createElement('h3');
      electionsInStateHeadingElement.innerHTML = ELECTION_LIST_TITLE + selectedStateName;
      electionListContainerElement.appendChild(electionsInStateHeadingElement);

      if (stateElections.length == 0) {
        buildEmptyListText(electionListContainerElement);
      } else {
        buildElectionListElements(electionListContainerElement, stateElections);
      }

      const electionsInNationHeadingElement = document.createElement('h3');
      electionsInNationHeadingElement.innerHTML = ELECTION_LIST_TITLE + COUNTRY_NAME;
      electionListContainerElement.appendChild(electionsInNationHeadingElement);

      if (nationalElections.length == 0) {
        buildEmptyListText(electionListContainerElement);
      } else {
        buildElectionListElements(electionListContainerElement, nationalElections);
      }
  });
}

/**
 * Build the HTML elements that make up the list of upcoming elections.
 * @param {HTMLDivElement} containerElement div container for election list
 * @param {JSON} elections JSON object containing the list of relevant elections.
 */
function buildElectionListElements(containerElement, elections) {
  const electionListElement = document.createElement('ul');
  electionListElement.className = 'list-group list-group-flush';
  electionListElement.id = 'election-list';

  elections.forEach((election) => {
    const electionItemElement = document.createElement('li');
    electionItemElement.className = 'list-group-item';
    electionItemElement.id = 'election-item';

    const electionDetailsElement = document.createElement('div');
    electionDetailsElement.className = 'election-quick-details';

    const electionNameElement = document.createElement('p');
    electionNameElement.innerHTML = election.name;
    electionDetailsElement.appendChild(electionNameElement);

    const electionDateElement = document.createElement('p');
    const electionDateEmphasisElement = document.createElement('em');
    electionDateEmphasisElement.innerHTML = formatDate(election.electionDay);
    electionDateElement.appendChild(electionDateEmphasisElement);
    electionDetailsElement.appendChild(electionDateElement);

    electionItemElement.appendChild(electionDetailsElement);

    const learnMoreButtonElement = document.createElement('div');
    learnMoreButtonElement.className = 'learn-more-button';
    learnMoreButtonElement.innerHTML = LEARN_MORE_BUTTON_TEXT;

    electionItemElement.appendChild(learnMoreButtonElement);

    electionListElement.appendChild(electionItemElement);
  });

  containerElement.appendChild(electionListElement);
}

/**
 * Build text that informs the user there are no elections to display.
 * @param {HTMLDivElement} containerElement div container for election list
 */
function buildEmptyListText(containerElement) {
  const emptyTextElement = document.createElement('p');
  const textEmphasisElement = document.createElement('em');
  textEmphasisElement.innerHTML = NO_ELECTIONS_TEXT;
  emptyTextElement.appendChild(textEmphasisElement);
  containerElement.appendChild(emptyTextElement);
}

/**
 * Change the format of the date returned by the Google Civic Information
 * API to be the standard date format (ex. 2020-08-16 ==> August 16, 2020).
 * @param {String} apiDate date in the form YYYY-MM-DD
 * @return {String} the formatted date in the form Month DD, YYYY
 */
function formatDate(apiDate) {
  if (apiDate == undefined) {
    return INVALID_DATE_TEXT;
  }

  let dateParts = apiDate.split('-');

  if (dateParts.length !== 3) {
    return INVALID_DATE_TEXT;
  }

  let monthNum = parseInt(dateParts[1]);

  if (monthNum > MONTHS.length) {
    return INVALID_DATE_TEXT;
  }

  return MONTHS[monthNum - 1] + ' ' + parseInt(dateParts[2]) + ', ' + dateParts[0];
}