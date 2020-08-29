package com.google.sps.servlets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PollingStationServletTest {
  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

  @Mock HttpServletRequest httpServletRequest;
  @Mock HttpServletResponse httpServletResponse;
  @Mock PrintWriter printWriter;

  private Entity electionEntityOne;
  private Entity pollingStationOne;
  private Entity pollingStationTwo;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void setUp() {
    helper.setUp();

    pollingStationOne = new Entity("PollingStation");
    pollingStationOne.setProperty("name", "pollingStationOne");
    pollingStationOne.setProperty("address", "addressOne");
    pollingStationOne.setProperty("pollingHours", "-");
    pollingStationOne.setProperty("startDate", "today");
    pollingStationOne.setProperty("endDate", "never");
    pollingStationOne.setProperty("locationType", "pollingLocation");
    pollingStationOne.setProperty("sources", new ArrayList<String>());
  }

  @Test
  public void noElectionEntityExists_testDoGet() throws IOException {
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletRequest.getParameter("address")).thenReturn("myAddress");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    PollingStationServlet pollingStationServlet = new PollingStationServlet();
    pollingStationServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("Polling locations for myAddress were not found.");
  }

  @Test
  public void queryParameterMissing_testDoGet() throws IOException {
    when(httpServletRequest.getParameter("electionId")).thenReturn(null);
    when(httpServletRequest.getParameter("address")).thenReturn("myAddress");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    PollingStationServlet pollingStationServlet = new PollingStationServlet();
    pollingStationServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("No electionId in the query URL.");
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}
