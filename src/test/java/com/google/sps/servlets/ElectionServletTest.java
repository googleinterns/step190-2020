package com.google.sps.servlets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
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

public class ElectionServletTest {
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

  // Test getting election data from Datastore
  @Test
  public void testElectionGet() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity electionEntity = new Entity("Election");
    electionEntity.setProperty("id", "9999");
    electionEntity.setProperty("name", "myElection");
    electionEntity.setProperty("scope", "myScope");
    electionEntity.setProperty("date", "myDate");
    electionEntity.setProperty("contests", new HashSet<String>());
    electionEntity.setProperty("propositions", new HashSet<String>());
    ds.put(electionEntity);
    when(httpServletResponse.getWriter()).thenReturn(printWriter);
    ElectionServlet electionServlet = new ElectionServlet();
    electionServlet.doGet(httpServletRequest, httpServletResponse);
    verify(printWriter)
        .println(
            "[{\"id\":\"9999\",\"name\":\"myElection\",\"date\":\"myDate\",\"scope\":\"myScope\",\"contests\":[],\"propositions\":[]}]");
  }

  // Test putting API JSON response for one election in the Datastore and reading from it.
  @Test
  public void testElectionPut() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    JSONObject json =
        new JSONObject(
            "{\"elections\": [{\"id\": \"9999\",\"name\": \"myElection\",\"electionDay\": \"myDate\",\"ocdDivisionId\": \"myScope\"}]}");
    JSONArray electionQueryArray = json.getJSONArray("elections");

    for (Object o : electionQueryArray) {
      JSONObject election = (JSONObject) o;
      Election.fromElectionQuery(election).putInDatastore(ds);
    }

    when(httpServletResponse.getWriter()).thenReturn(printWriter);
    ElectionServlet electionServlet = new ElectionServlet();
    electionServlet.doGet(httpServletRequest, httpServletResponse);
    verify(printWriter)
        .println(
            "[{\"id\":\"9999\",\"name\":\"myElection\",\"date\":\"myDate\",\"scope\":\"myScope\",\"contests\":[],\"propositions\":[]}]");
  }

  // Test putting API JSON response for multiple elections in the Datastore and reading from it.
  @Test
  public void testMultipleElectionsPutAndGet() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    JSONObject json =
        new JSONObject(
            "{\"elections\": [{\"id\": \"0001\",\"name\": \"election1\",\"electionDay\": \"date1\",\"ocdDivisionId\": \"scope1\"},"
                + "{\"id\": \"0002\",\"name\": \"election2\",\"electionDay\": \"date2\",\"ocdDivisionId\": \"scope2\"}]}");
    JSONArray electionQueryArray = json.getJSONArray("elections");

    for (Object o : electionQueryArray) {
      JSONObject election = (JSONObject) o;
      Election.fromElectionQuery(election).putInDatastore(ds);
    }

    when(httpServletResponse.getWriter()).thenReturn(printWriter);
    ElectionServlet electionServlet = new ElectionServlet();
    electionServlet.doGet(httpServletRequest, httpServletResponse);
    verify(printWriter)
        .println(
            "[{\"id\":\"0001\",\"name\":\"election1\",\"date\":\"date1\",\"scope\":\"scope1\",\"contests\":[],\"propositions\":[]},"
                + "{\"id\":\"0002\",\"name\":\"election2\",\"date\":\"date2\",\"scope\":\"scope2\",\"contests\":[],\"propositions\":[]}]");
  }
}
