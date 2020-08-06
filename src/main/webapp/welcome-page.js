function onWelcomePageLoad(){
  let links = [document.getElementById("home-link"), document.getElementById("elections-list-link")];
  updateLinksWithQueryParams(links);
}
