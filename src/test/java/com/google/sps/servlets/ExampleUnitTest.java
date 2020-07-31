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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/** An example unit test. */
public class ExampleUnitTest {
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

  // Example for how to use DatastoreService for servlet-only tests.
  @Test
  public void testElectionGet() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity electionEntity = new Entity("Election");
    electionEntity.setProperty("id", 9999);
    electionEntity.setProperty("name", "myElection");
    electionEntity.setProperty("scope", "myScope");
    electionEntity.setProperty("date", "myDate");
    ds.put(electionEntity);
    when(httpServletResponse.getWriter()).thenReturn(printWriter);
    ElectionServlet electionServlet = new ElectionServlet();
    electionServlet.doGet(httpServletRequest, httpServletResponse);
    verify(printWriter)
        .println(
            "[{\"ID\":9999,\"name\":\"myElection\",\"scope\":\"myScope\",\"positions\":[],\"date\":\"myDate\",\"propositions\":[]}]");
  }

  @Test
  public void testAThing() throws Exception {
    // Just a silly example.
    int a = 1;
    int b = 2;
    Assert.assertFalse(a == b);
  }
}
