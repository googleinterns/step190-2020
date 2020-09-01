/* eslint-env jasmine */
/* exported withHtml */


const initStart = window.__karma__.start;

window.__karma__.start = function() {
	if (window.addEventListener) {
		document.addEventListener('DOMContentLoaded', function() {
			console.info('Modules should be loaded, start it');
			initStart();
		});
	} else {
		// Hack for IE7 and IE8.
		// In IE7 and IE8, we don't care about native es6-module, since they are not implemented anyway
		console.warn('IE7 hacking mode: we are not waiting for modules to be loaded (and they will never be since IE7 does not support it)');
		initStart();
	}
};
