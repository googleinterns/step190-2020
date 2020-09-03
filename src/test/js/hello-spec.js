$("body").append(window.__html__.electionlist);
$("body").append(window.__html__.electionInfo);

describe( 'hello module', function () {
    'use strict';
    it( 'speak()', function () {
        expect( hello.speak() ).toBe( 'Hello!' );
    } );
} );