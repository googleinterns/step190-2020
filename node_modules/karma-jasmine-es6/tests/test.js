/* eslint-env jasmine */

/* Check that karma is still ok */
describe('root test', function() {
	it('works', function() {
		expect(true).toBeTruthy();
	});
});

/* The real test */
describe('in text mode', function() {
	it('should work', function() {
		expect(window.testmodule).toBe(2);
	});
});
