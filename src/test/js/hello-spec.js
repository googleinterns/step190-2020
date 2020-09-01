$("body").append(window.__html__.index);
$("body").append(window.__html__.election-list);
$("body").append(window.__html__.election-info);

describe( 'hello module', function () {
    'use strict';
    it( 'speak()', function () {
        expect( hello.speak() ).toBe( 'Hello!' );
    } );
} );