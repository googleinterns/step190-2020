package com.google.sps.servlets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import java.io.PrintWriter;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    electionEntity.setProperty("contests", new HashSet<Long>());
    electionEntity.setProperty("propositions", new HashSet<Long>());
    ds.put(electionEntity);
    when(httpServletResponse.getWriter()).thenReturn(printWriter);
    ElectionServlet electionServlet = new ElectionServlet();
    electionServlet.doGet(httpServletRequest, httpServletResponse);
    verify(printWriter)
        .println(
            "[{\"id\":\"9999\",\"name\":\"myElection\",\"date\":\"myDate\",\"scope\":\"myScope\",\"contests\":[],\"propositions\":[]}]");
  }
}
