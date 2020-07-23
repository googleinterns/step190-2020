function listElections() {
  let stateSelected = document.getElementById('select-state').value;
  let nationalElections = [];
  let stateElections = [];

  if (stateSelected === null) {
    return;
  }

  fetch('/election')
    .then(response => response.json())
    .then((textResponse) => {
      let electionList = JSON.parse(textResponse).elections;
      electionList.forEach((election) => {
        if (election.ocdDivisionId == stateSelected) {
          // TODO: update ocdDivisionId to cater to state vs. national elections
          stateElections.append(election);
        }
      });
      
      buildElectionListElements(stateElections);
  });
}

function buildElectionListElements(elections) {
  const electionListContainerElement = document.getElementById('election-list-content');

  const electionsInStateHeadingElement = document.createElement('h3');
  electionsInStateHeadingElement.innerHTML = 'Elections coming up in Washington';
  electionListContainerElement.appendChild(electionsInStateHeadingElement);

  const electionsInStateList
}