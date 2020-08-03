package com.google.sps.servlets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.sps.data.Election;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/** An example unit test. */
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

  // Test putting API JSON response in the Datastore and reading from it.
  @Test
  public void testElectionPutAndGet() throws Exception {
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
}
