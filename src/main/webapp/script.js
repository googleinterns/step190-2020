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
