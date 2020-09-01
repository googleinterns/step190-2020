/* eslint-env node */
/* eslint no-console: off */

module.exports = function(config) {
	config.set({
		client: {
			jasmine: {
				random: false
			}
		},

		frameworks : [
			'jasmine-es6',
			'jasmine'
		],

		reporters : [
			'progress',
			'coverage',
		],

		files : [
			{ pattern: 'tests/module.js', watched: true, served: true, included: true, type: 'module' },
			{ pattern: 'tests/module2.js', watched: true, served: true, included: false, type: 'module' },
			'tests/test.js',
			{ pattern: '*.js', watched: true, served: false, included: false }
		],

		autoWatch : true,

		browsers: [
			// 'FirefoxHeadless',
			'ChromeHeadless'
		],

		// // https://github.com/karma-runner/karma-firefox-launcher/issues/76
		// customLaunchers: {
		// 	FirefoxHeadless: {
		// 		base: 'Firefox',
		// 		flags: [ '-headless' ],
		// 	},
		// },

		preprocessors: {
			'*.js': [ 'coverage' ],
		},

		coverageReporter: {
			type :  'lcov',
			dir :   __dirname + '/',
			subdir: 'target/'
		},
	});
};
