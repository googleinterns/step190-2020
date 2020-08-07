package com.google.sps.servlets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
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

public class ContestServletTest {
  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

  @Mock HttpServletRequest httpServletRequest;
  @Mock HttpServletResponse httpServletResponse;
  @Mock PrintWriter printWriter;

  private static final ImmutableSet<Long> CANDIDATE_ID_SET =
      new ImmutableSet.Builder<Long>().add(1L).add(2L).add(3L).build();

  private Entity electionEntityOne;
  private Entity contestEntityOne;
  private Entity contestEntityTwo;
  private Entity referendumEntityOne;
  private Entity referendumEntityTwo;

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
    electionEntityOne.setProperty("referendums", new HashSet<Long>());

    contestEntityOne = new Entity("Contest");
    contestEntityOne.setProperty("name", "myFirstContest");
    contestEntityOne.setProperty("candidates", CANDIDATE_ID_SET);
    contestEntityOne.setProperty("description", "This contest is important.");

    contestEntityTwo = new Entity("Contest");
    contestEntityTwo.setProperty("name", "mySecondContest");
    contestEntityTwo.setProperty("candidates", new HashSet<Long>());
    contestEntityTwo.setProperty("description", "This contest is also important.");

    referendumEntityOne = new Entity("Referendum");
    referendumEntityOne.setProperty("title", "myFirstReferendum");
    referendumEntityOne.setProperty("description", "This is a referendum.");

    referendumEntityTwo = new Entity("Referendum");
    referendumEntityTwo.setProperty("title", "mySecondReferendum");
    referendumEntityTwo.setProperty("description", "This is another referendum.");
  }

  @Test
  public void singleElection_singleContest_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity contestEntity = new Entity("Contest");

    electionEntity.setPropertiesFrom(electionEntityOne);
    contestEntity.setPropertiesFrom(contestEntityOne);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestId = ds.put(contestEntity).getId();

    Collection<Long> contestSet = (Collection<Long>) electionEntity.getProperty("contests");
    contestSet.add(contestId);
    ds.put(electionEntity);

    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\": [{\"name\":\"myFirstContest\",\"candidates\":[1,2,3],\"description\":\"This contest is important.\"}],"
                + "\"referendums\": []}");
  }

  @Test
  public void singleElection_twoContest_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity firstContestEntity = new Entity("Contest");
    Entity secondContestEntity = new Entity("Contest");

    electionEntity.setPropertiesFrom(electionEntityOne);
    firstContestEntity.setPropertiesFrom(contestEntityOne);
    secondContestEntity.setPropertiesFrom(contestEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestOneId = ds.put(firstContestEntity).getId();
    long contestTwoId = ds.put(secondContestEntity).getId();

    HashSet<Long> contestSet = (HashSet<Long>) electionEntityOne.getProperty("contests");
    contestSet.add(contestOneId);
    contestSet.add(contestTwoId);
    ds.put(electionEntityOne);

    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\": [{\"name\":\"myFirstContest\",\"candidates\":[1,2,3],\"description\":\"This contest is important.\"},"
                + "{\"name\":\"mySecondContest\",\"candidates\":[],\"description\":\"This contest is also important.\"}],"
                + "\"referendums\": []}");
  }

  @Test
  public void singleElection_singleReferendum_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity referendumEntity = new Entity("Referendum");

    electionEntity.setPropertiesFrom(electionEntityOne);
    referendumEntity.setPropertiesFrom(referendumEntityOne);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long referendumId = ds.put(referendumEntity).getId();

    Collection<Long> referendumSet = (Collection<Long>) electionEntity.getProperty("referendums");
    referendumSet.add(referendumId);
    ds.put(electionEntity);

    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\": [],"
                + "\"referendums\": [{\"title\":\"myFirstReferendum\",\"description\":\"This is a referendum.\"}]}");
  }

  @Test
  public void singleElection_twoContestsTwoReferendums_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity firstContestEntity = new Entity("Contest");
    Entity secondContestEntity = new Entity("Contest");
    Entity firstReferendumEntity = new Entity("Referendum");
    Entity secondReferendumEntity = new Entity("Referendum");

    electionEntity.setPropertiesFrom(electionEntityOne);
    firstContestEntity.setPropertiesFrom(contestEntityOne);
    secondContestEntity.setPropertiesFrom(contestEntityTwo);
    firstReferendumEntity.setPropertiesFrom(referendumEntityOne);
    secondReferendumEntity.setPropertiesFrom(referendumEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestOneId = ds.put(firstContestEntity).getId();
    long contestTwoId = ds.put(secondContestEntity).getId();
    long referendumOneId = ds.put(firstReferendumEntity).getId();
    long referendumTwoId = ds.put(secondReferendumEntity).getId();

    HashSet<Long> contestSet = (HashSet<Long>) electionEntityOne.getProperty("contests");
    contestSet.add(contestOneId);
    contestSet.add(contestTwoId);
    HashSet<Long> referendumSet = (HashSet<Long>) electionEntityOne.getProperty("referendums");
    referendumSet.add(referendumOneId);
    referendumSet.add(referendumTwoId);
    ds.put(electionEntityOne);

    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\": [{\"name\":\"myFirstContest\",\"candidates\":[1,2,3],\"description\":\"This contest is important.\"},"
                + "{\"name\":\"mySecondContest\",\"candidates\":[],\"description\":\"This contest is also important.\"}],"
                + "\"referendums\": [{\"title\":\"myFirstReferendum\",\"description\":\"This is a referendum.\"},"
                + "{\"title\":\"mySecondReferendum\",\"description\":\"This is another referendum.\"}]}");
  }

  @Test
  public void noElectionEntityExists_testDoGet() throws IOException {
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("Election with id 9999 was not found.");
  }

  @Test
  public void queryParameterMissing_testDoGet() throws IOException {
    when(httpServletRequest.getParameter("electionId")).thenReturn(null);
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("No electionId in the query URL.");
  }

  @Test
  public void oneElection_contestMissingInDatastore_testDoGet() throws IOException {
    Entity electionEntity = new Entity("Election");
    Entity firstContestEntity = new Entity("Contest");
    Entity secondContestEntity = new Entity("Contest", 2);

    electionEntity.setPropertiesFrom(electionEntityOne);
    firstContestEntity.setPropertiesFrom(contestEntityOne);
    secondContestEntity.setPropertiesFrom(contestEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestOneId = ds.put(firstContestEntity).getId();
    long contestTwoId = secondContestEntity.getKey().getId();

    HashSet<Long> contestSet = (HashSet<Long>) electionEntityOne.getProperty("contests");
    contestSet.add(contestOneId);
    contestSet.add(contestTwoId);
    ds.put(electionEntityOne);

    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("Contest with Id " + contestTwoId + " was not found.");
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}
