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

The dev server run for the integration tests will use `integration_db.bin` as the
file for datastore.  The file is copied automatically to a tmp folder and deleted
after the integration test run completes.  

**This means that changes made to datastore during the integration test will NOT be persisted.**

In order to update the data that the dev server starts with for integration tests, replace the `integration_db.bin` file.
You can do this by:
 1) Starting up a local dev server pointed to that file for datastore.
       * Add this setting to the `configuration` under `appengine-maven-plugin`:
    ```
    <jvmFlags>
      <jvmFlag>
        -Ddatastore.backing_store=${integrationTest.datastoreFileDirTmp}/${integrationTest.datastoreFile}
      </jvmFlag>
    </jvmFlags>
    ```
 2) Make necessary changes to datastore by running some servlet code.
 3) Stop the local dev server. Don't forget to remove the local datastore file override when you're done.
 
