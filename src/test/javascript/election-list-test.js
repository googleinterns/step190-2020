describe("Election-list test suite", function() {
    beforeEach(function() {
       jasmine.Ajax.install();
    });
    afterEach(function() {
       jasmine.Ajax.uninstall();
    });
    var a;

    it("no-op test", function() {
        a = true;

        expect(a).toBe(true);
    });

    // it("test listElections", function() {
    //     spyOn(document, "getElementById").and.returnValue({
    //         selectedIndex: 0,
    //         options: [
    //             {
    //                 value: 'stateId',
    //                 text: 'stateName'
    //             }
    //         ]
    //     });
    //     spyOn(window.history, "pushState");
    //
    //     listElections();
    //     expect(true).toBe(true);
    //     let expectedUrl = 'http://localhost:8234/?state=stateId';
    //     expect(window.history.pushState).toHaveBeenCalledWith({path: expectedUrl}, '', expectedUrl);
    // })
});
