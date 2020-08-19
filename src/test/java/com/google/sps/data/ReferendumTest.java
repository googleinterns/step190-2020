package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public class ReferendumTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Test putting API JSON response for one election in an Election object and reading from it.
  @Test
  public void allFieldsPresent_testFromJSONObject() throws Exception {
    JSONObject referendumJsonObject =
        new JSONObject(
            "{\"type\": \"Referendum\",\"referendumTitle\": \"Proposition 1\","
                + "\"referendumSubtitle\": \"Water Bond. Funding for Water Quality, Supply, Treatment, and Storage Projects.\","
                + "\"sources\": [{\"name\": \"Voter Information Project\"},{\"name\": \"Ballot Information Project\"}]}");

    Referendum referendum = Referendum.fromJSONObject(referendumJsonObject);

    Assert.assertEquals(referendum.getTitle(), "Proposition 1");
    Assert.assertEquals(
        referendum.getDescription(),
        "Water Bond. Funding for Water Quality, Supply, Treatment, and Storage Projects.");
    Assert.assertEquals(
        referendum.getSource(), "Voter Information Project, Ballot Information Project");
  }

  @Test
  public void missingDescription_testFromJSONObject() throws Exception {
    JSONObject referendumJsonObject =
        new JSONObject("{\"type\": \"Referendum\",\"referendumTitle\": \"Proposition 1\"}");

    Referendum referendum = Referendum.fromJSONObject(referendumJsonObject);

    Assert.assertEquals(referendum.getTitle(), "Proposition 1");
    Assert.assertEquals(referendum.getDescription(), "");
  }

  @Test
  public void missingTitle_throwJSONException_testFromJSONObject() throws Exception {
    JSONObject referendumJsonObject =
        new JSONObject(
            "{\"type\": \"Referendum\", \"referendumSubtitle\": \"Water Bond. Funding for Water Quality, Supply, Treatment, and Storage Projects.\"}");

    exceptionRule.expect(JSONException.class);
    exceptionRule.expectMessage("Malformed referendum JSONObject: referendumTitle does not exist.");
    Referendum referendum = Referendum.fromJSONObject(referendumJsonObject);
  }

  // Test updating an existing Entity in the Datastore.
  @Test
  public void testAddToDatastore() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Referendum referendum =
        Referendum.builder()
            .setTitle("myTitle")
            .setDescription("myDescription")
            .setSource("mySource")
            .setUrl("myUrl")
            .build();

    long entityKeyId = referendum.addToDatastore(ds);
    Entity entity = ds.get(KeyFactory.createKey("Referendum", entityKeyId));

    Assert.assertEquals(entity.getProperty("title"), "myTitle");
    Assert.assertEquals(entity.getProperty("description"), "myDescription");
  }

  // Test converting a Referendum Entity into a Referendum object.
  @Test
  public void testFromEntityToElectionObject() throws Exception {
    Entity entity = new Entity("Referendum");
    entity.setProperty("title", "myTitle");
    entity.setProperty("description", "myDescription");
    entity.setProperty("source", "mySource");
    entity.setProperty("url", "myUrl");

    Referendum referendum = Referendum.fromEntity(entity);

    Assert.assertEquals(referendum.getTitle(), "myTitle");
    Assert.assertEquals(referendum.getDescription(), "myDescription");
  }
}
