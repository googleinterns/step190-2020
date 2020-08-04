package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.sps.data.Election;
import java.io.PrintWriter;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class InfoCardServletTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

  @Mock HttpServletRequest httpServletRequest;
  @Mock HttpServletResponse httpServletResponse;
  @Mock PrintWriter printWriter;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Test putting API JSON response for one election in the Datastore and reading from it.
  @Test
  public void testElectionStoresContestId() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    JSONObject electionQueryJson =
        new JSONObject(
            "{\"elections\": ["
                + "{\"id\": \"9999\","
                + "\"name\": \"myElection\","
                + "\"electionDay\": \"myDate\","
                + "\"ocdDivisionId\": \"myScope\""
                + "}]}");
    JSONArray electionQueryArray = electionQueryJson.getJSONArray("elections");
    JSONObject electionJson = electionQueryArray.getJSONObject(0);
    long entityKeyId = Election.fromElectionQuery(electionJson).addToDatastore(ds);

    when(httpServletResponse.getWriter()).thenReturn(printWriter);
    ElectionServlet electionServlet = new ElectionServlet();
    electionServlet.doGet(httpServletRequest, httpServletResponse);

    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"typeName\",\"office\": \"officeName\","
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\"}]}]}");

    Entity entity = ds.get(KeyFactory.createKey("Election", entityKeyId));
    Election election = Election.fromEntity(entity);
    election.fromVoterInfoQuery(ds, voterInfoQueryJson).putInDatastore(ds, entity);

    HashSet<Long> keys = new HashSet<>();
    keys.add(new Long(entityKeyId));
    Assert.assertEquals(election.getContests(), keys);
  }

  // TODO(caseyprice,gianelgado): Finish populating other Entity objects and write further tests.
}
