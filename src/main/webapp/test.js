(function(){
    'use strict';
    
    /**
     * Function used to unit test our JavaScript code for gVote.
     * If a test passes, a green check mark appears on the JS 
     * console of /test.html; if not, a red cross appears.
     * 
     * @param {string} desc description of the test
     * @param {function} fn function being tested
     */
    function it(desc, fn) {
      try {
        fn();
        console.log('\x1b[32m%s\x1b[0m', '\u2714 ' + desc);
      } catch (error) {
        console.log('\n');
        console.log('\x1b[31m%s\x1b[0m', '\u2718 ' + desc);
        console.error(error);
      }
    }

    /**
     * Asserts whether or not a given statement evaluates
     * to true.
     * 
     * @param {boolean} isTrue statement being evaluated
     */
    function assert(isTrue) {
      if (!isTrue) {
        throw new Error();
      }
    }

    /**
     * Basic test that should fail
     */
    it('should fail', function() {
      assert(1 !== 1);
    });
    
    /**
     * Basic test that should pass
     */
    it('should pass', function() {
      assert(1 === 1);
    });

    /**
     * Checks for presence of dates and deadlines for national elections
     * taking place in CA by calling populateDeadlines()
     */
    it('should add dates and deadlines to the HTML', function () {
      var selector = document.querySelector('#selector');
      selector.innerHTML =
      `<div id="dates-and-deadlines" style="display: none"></div>
      <script id="deadlines-template" type="text/x-handlebars-template">
        <div class="row" id="deadlines-wrapper">
          <div class="col-lg-12">
            {{#if primaryDeadlines}}
              <h3 id="primary-deadlines-title"><b>Key deadlines in {{this.state}} for this primary election: </b></h3>
              {{#each primaryDeadlines}}
              <p>
                {{processRule this.rule this.voting-request-type}} <b>{{formatDate this.date}}</b>
              </p>
              {{/each}}
            {{/if}}
            {{#if runOffDeadlines}}
              <h3 id="runoff-deadlines-title"><b>Key deadlines in {{this.state}} for this primary runoff election: </b></h3>
              {{#each runOffDeadlines}}
              <p>
                {{processRule this.rule this.voting-request-type}} <b>{{formatDate this.date}}</b>
              </p>
              {{/each}}
            {{/if}}
            {{#if generalDeadlines}}
              <h3 id="general-deadlines-title"><b>Key deadlines in {{this.state}} for this national election: </b></h3>
              {{#each generalDeadlines}}
              <p>
                {{processRule this.rule this.voting-request-type}} <b>{{formatDate this.date}}</b>
              </p>
              {{/each}}
            {{/if}}
          </div>
        </div>
      </script>`;
  
      populateDeadlines("ca", "ocd-division/country:us");

      setTimeout(function(){
        var generalDeadlinesTitle = document.getElementById("general-deadlines-title");
        console.log(generalDeadlinesTitle.innerText);
        assert(generalDeadlinesTitle.innerText.includes('Key deadlines in California for this national election:'));
        
        //cleanup
        selector.innerHTML = '';
      }, 3000);
    });

  })();

  