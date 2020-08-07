package com.google.sps.servlets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
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
    electionEntityOne = new Entity("Election");

    electionEntityOne.setProperty("id", "9999");
    electionEntityOne.setProperty("name", "myElection");
    electionEntityOne.setProperty("scope", "myScope");
    electionEntityOne.setProperty("date", "myDate");
    electionEntityOne.setProperty("contests", new HashSet<Long>());
    electionEntityOne.setProperty("propositions", new HashSet<Long>());
    electionEntityOne.setProperty("pollingStations", new ArrayList<EmbeddedEntity>());

    pollingStationOne = new Entity("PollingStation");
    pollingStationOne.setProperty("name", "pollingStationOne");
    pollingStationOne.setProperty("address", "addressOne");
    pollingStationOne.setProperty("pollingHours", "-");
    pollingStationOne.setProperty("startDate", "today");
    pollingStationOne.setProperty("endDate", "never");
    pollingStationOne.setProperty("locationType", "pollingLocation");
  }

  @Test
  public void singleElection_singlePollingStation_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity pollingStationEntity = new Entity("PollingStation");

    electionEntity.setPropertiesFrom(electionEntityOne);
    pollingStationEntity.setPropertiesFrom(pollingStationOne);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    ArrayList<EmbeddedEntity> pollingStations =
        (ArrayList<EmbeddedEntity>) electionEntity.getProperty("pollingStations");
    EmbeddedEntity embeddedPollingStationEntity = new EmbeddedEntity();
    embeddedPollingStationEntity.setPropertiesFrom(pollingStationEntity);
    pollingStations.add(embeddedPollingStationEntity);

    ds.put(electionEntity);

    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    PollingStationServlet pollingStationServlet = new PollingStationServlet();
    pollingStationServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "[{\"name\":\"pollingStationOne\",\"address\":\"addressOne\","
                + "\"pollingHours\":\"-\",\"startDate\":\"today\",\"endDate\":\"never\","
                + "\"locationType\":\"pollingLocation\"}]");
  }

  @Test
  public void noElectionEntityExists_testDoGet() throws IOException {
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    PollingStationServlet pollingStationServlet = new PollingStationServlet();
    pollingStationServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("Election with id 9999 was not found.");
  }

  @Test
  public void queryParameterMissing_testDoGet() throws IOException {
    when(httpServletRequest.getParameter("electionId")).thenReturn(null);
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
