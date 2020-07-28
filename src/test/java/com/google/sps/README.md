<h1>Testing</h1>
There are two types of tests:

* Unit tests
* Integration tests


Unit tests should follow the filename pattern: `**/**Test.java`
Integration tests should follow one of the following patterns:

* `**/*IT.java`
* `**/IT*.java`
* `**/*ITCase.java`

Unit tests will be automatically run whenever you try to build / run the server.
For example: `mvn package` or `mvn appengine:run`.

Integration tests can be run using `mvn verify`.  Note that this will automatically
build and run unit tests as well. The integration test will start up a local dev
server bound to port 9876, and automatically stop the server after the tests are
done running.
