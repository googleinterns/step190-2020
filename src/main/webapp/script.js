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
      electionList.forEach((election) => {
        let ocdId = election.ocdDivisionId;
        let stateId = ocdId.indexOf(OCD_STATE_TITLE);
        let districtId = ocdId.indexOf(OCD_DISTRICT_TITLE);

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
      
      const electionListContainerElement = document.getElementById('election-list-content');
      electionListContainerElement.innerHTML = '';
      
      const electionsInStateHeadingElement = document.createElement('h3');
      electionsInStateHeadingElement.innerHTML = 'Elections coming up in ' + selectedStateName;
      electionListContainerElement.appendChild(electionsInStateHeadingElement);

      if (stateElections.length == 0) {
        console.log('no elections for ' + selectedStateName);
        buildEmptyListText(electionListContainerElement);
      } else {
        buildElectionListElements(electionListContainerElement, stateElections);
      }

      const electionsInNationHeadingElement = document.createElement('h3');
      electionsInNationHeadingElement.innerHTML = 'Elections coming up in the United States';
      electionListContainerElement.appendChild(electionsInNationHeadingElement);

      if (nationalElections.length == 0) {
        buildEmptyListText(electionListContainerElement);
      } else {
        buildElectionListElements(electionListContainerElement, nationalElections);
      }
  });
}

/**
 * 
 * @param {*} containerElement 
 * @param {*} elections 
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
    learnMoreButtonElement.innerHTML = 'Learn more';

    electionItemElement.appendChild(learnMoreButtonElement);

    electionListElement.appendChild(electionItemElement);
  });

  containerElement.appendChild(electionListElement);
}

/**
 * Build the text that 
 * @param {*} containerElement 
 */
function buildEmptyListText(containerElement) {
  const emptyTextElement = document.createElement('p');
  const textEmphasisElement = document.createElement('em');
  textEmphasisElement.innerHTML = 'No elections coming up soon.';
  emptyTextElement.appendChild(textEmphasisElement);
  containerElement.appendChild(emptyTextElement);
}

/**
 * 
 * @param {String} apiDate the date returned by the Google Civic Information
 *                         API in the form YYYY-MM-DD.
 * @return {String} the formatted date in the form Month DD, YYYY
 */
function formatDate(apiDate) {
  if (apiDate == undefined) {
    return 'Invalid date';
  }

  let dateParts = apiDate.split('-');

  if (dateParts.length !== 3) {
    return 'Invalid date';
  }

  let monthNum = parseInt(dateParts[1]);

  if (monthNum > MONTHS.length) {
    return 'Invalid month';
  }

  return MONTHS[monthNum - 1] + ' ' + parseInt(dateParts[2]) + ', ' + dateParts[0];
}