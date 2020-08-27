// The config file contains the unrestricted key for use on localhost
var mapKey = (typeof config === 'undefined' || config === null) 
                ? 'AIzaSyCBrkGl891qgFHNFz3VGi3N-CEXGLp-DIU' : config.PLACES_API_KEY;

var script = document.createElement('script');
script.type = 'text/javascript';
script.src = 'https://maps.googleapis.com/maps/api/js?key='
             + mapKey + '&libraries=places,geometry&callback=initAutocomplete';
script.defer = true;

document.head.appendChild(script);

var placeSearch, autocomplete;

/**
 * Initializes an autocomplete object for the address input form
 */
function initAutocomplete() {
  // Create the autocomplete object, restricting the search predictions to
  // geographical location types.
  autocomplete = new google.maps.places.Autocomplete(
      document.getElementById('autocomplete'), {types: ['geocode']});

  // Avoid paying for data that you don't need by restricting the set of
  // place fields that are returned to just the address components.
  autocomplete.setFields(['address_component']);
}

/**
 * Based on the user's location, set bounds for the addresses that
 * can be suggested based on their distance from the location
 */
function positionCallback(position) {
  var geolocation = {
    lat: position.coords.latitude,
    lng: position.coords.longitude
  };

  var circle = new google.maps.Circle({
    center: geolocation, 
    radius: position.coords.accuracy});
  
  autocomplete.setBounds(circle.getBounds());
}

/** 
 * Bias the autocomplete object to the user's geographical location,
 * as supplied by the browser's 'navigator.geolocation' object.
 */
function geolocate() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(positionCallback);
  }
}