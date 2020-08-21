package com.google.sps.servlets;

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
import org.json.XML;
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
public class DeadlineServletTest {
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

  @Test
  public void queryParameterMissing_testDoGet() throws IOException {
    when(httpServletRequest.getParameter("state")).thenReturn(null);
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    DeadlinesServlet deadlinesServlet = new DeadlinesServlet();
    deadlinesServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("No state in the query URL.");
  }

  @Test
  public void californiaUrl_testDoGet() throws IOException {
    mockStatic(ServletUtils.class);
    when(ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "state"))
        .thenReturn(Optional.of("ca"));

    String xmlString =
        "<evag xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "schema-version=\"1.0\" xsi:schemaLocation=\"/xml-api/api-schema.xsd\">"
            + "<deadline-dates>"
            + "<election-date>"
            + "<date>2020-11-03T00:00:00</date>"
            + "<election-type>General Election</election-type> </election-date><deadline-date> "
            + "<rule>By Mail: Postmarked by</rule> <date>2020-10-19T00:00:00</date> <election-type>General Election</election-type>"
            + "<voting-request-type>Registration</voting-request-type> </deadline-date> <deadline-date> <rule>By Online or Fax: Received by</rule>"
            + "<date>2020-10-19T00:00:00</date> <election-type>General Election</election-type> <voting-request-type>Registration</voting-request-type>"
            + "</deadline-date> <deadline-date> <rule>Received by</rule> <date>2020-10-27T00:00:00</date>"
            + "<election-type>General Election</election-type> <voting-request-type>Ballot Request</voting-request-type> </deadline-date>"
            + "<deadline-date> <rule>Return by Mail: Postmarked by*</rule> <date>2020-11-03T00:00:00</date> <election-type>General Election</election-type>"
            + "<voting-request-type>Ballot Return</voting-request-type> </deadline-date> <deadline-date>"
            + "<rule>Return by Fax: Received by</rule> <date>2020-11-03T20:00:00</date>"
            + "<election-type>General Election</election-type> <voting-request-type>Ballot Return</voting-request-type>"
            + "</deadline-date> </deadline-dates> </evag>";

    when(ServletUtils.readFromApiUrl(anyString()))
        .thenReturn(Optional.of(new JSONObject(XML.toJSONObject(xmlString).toString())));

    when(httpServletRequest.getParameter("state")).thenReturn("ca");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    DeadlinesServlet deadlinesServlet = new DeadlinesServlet();
    deadlinesServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"myArrayList\":[{\"map\":{\"date\":\"2020-10-19T00:00:00\","
                + "\"election-type\":\"General Election\",\"rule\":\"By Mail: Postmarked by\","
                + "\"voting-request-type\":\"Registration\"}},{\"map\":{\"date\":"
                + "\"2020-10-19T00:00:00\",\"election-type\":\"General Election\",\"rule\":"
                + "\"By Online or Fax: Received by\",\"voting-request-type\":\"Registration\"}},"
                + "{\"map\":{\"date\":\"2020-10-27T00:00:00\",\"election-type\":\"General Election\","
                + "\"rule\":\"Received by\",\"voting-request-type\":\"Ballot Request\"}},{\"map\":"
                + "{\"date\":\"2020-11-03T00:00:00\",\"election-type\":\"General Election\",\"rule\":"
                + "\"Return by Mail: Postmarked by*\",\"voting-request-type\":\"Ballot Return\"}},"
                + "{\"map\":{\"date\":\"2020-11-03T20:00:00\",\"election-type\":\"General Election\","
                + "\"rule\":\"Return by Fax: Received by\",\"voting-request-type\":\"Ballot Return\"}}]}");
  }
}
