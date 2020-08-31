package com.google.sps.servlets;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServletUtils.class)
public class PollingStationServletTest {
  private static final LocalServiceTestHelper helper =
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

  @Test
  public void singlePollingStationReturned_testDoGet() throws Exception {
    mockStatic(ServletUtils.class);
    when(ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "electionId"))
        .thenReturn(Optional.of("2000"));
    when(ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "address"))
        .thenReturn(Optional.of("myAddress"));
    String returnString =
        "{\"pollingLocations\": ["
            + "{"
            + "\"address\": {"
            + "\"line1\": \"555 Main St\","
            + "\"city\": \"Plainville\","
            + "\"state\": \"MN\","
            + "\"zip\": \"1111\""
            + "},"
            + "\"pollingHours\": \"myPollingHours\","
            + "\"name\": \"myPollingStation\","
            + "\"startDate\": \"myStartDate\","
            + "\"endDate\": \"myEndDate\","
            + "\"sources\": ["
            + "{"
            + "\"name\": \"sourceOne\","
            + "\"official\": \"true\""
            + "}"
            + "]"
            + "}"
            + "]}";

    when(ServletUtils.readFromApiUrl(anyString(), anyBoolean()))
        .thenReturn(Optional.of(new JSONObject(returnString)));

    when(httpServletRequest.getParameter("electionId")).thenReturn("2000");
    when(httpServletRequest.getParameter("address")).thenReturn("myAddress");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    PollingStationServlet pollingStationServlet = new PollingStationServlet();
    pollingStationServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "[{\"name\":\"myPollingStation\",\"address\":\"555 Main St, Plainville, MN 1111\","
                + "\"pollingHours\":\"myPollingHours\",\"startDate\":\"myStartDate\",\"endDate\":\"myEndDate\","
                + "\"locationType\":\"pollingLocations\",\"sources\":[\"sourceOne\"]}]");
  }

  @Test
  public void failedQuery_returnEmptyOptional_testDoGet() throws IOException {
    mockStatic(ServletUtils.class);
    when(ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "electionId"))
        .thenReturn(Optional.of("2000"));
    when(ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "address"))
        .thenReturn(Optional.of("myAddress"));
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletRequest.getParameter("address")).thenReturn("myAddress");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);
    when(ServletUtils.readFromApiUrl(anyString(), anyBoolean())).thenReturn(Optional.empty());

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
