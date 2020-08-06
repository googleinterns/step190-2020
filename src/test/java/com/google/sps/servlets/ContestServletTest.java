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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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

  private static final Set<Long> candidateIdSet =
      new ImmutableSet.Builder<Long>()
          .add(Long.valueOf(1))
          .add(Long.valueOf(2))
          .add(Long.valueOf(3))
          .build();
  private static Entity electionEntityOne;
  private static Entity contestEntityOne;
  private static Entity contestEntityTwo;

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

    contestEntityOne = new Entity("Contest");
    contestEntityOne.setProperty("name", "myFirstContest");
    contestEntityOne.setProperty("candidates", candidateIdSet);
    contestEntityOne.setProperty("description", "This contest is important.");

    contestEntityTwo = new Entity("Contest");
    contestEntityTwo.setProperty("name", "mySecondContest");
    contestEntityTwo.setProperty("candidates", new HashSet<Long>());
    contestEntityTwo.setProperty("description", "This contest is also important.");
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
            "[{\"name\":\"myFirstContest\",\"candidates\":[1,2,3],\"description\":\"This contest is important.\"}]");
  }

  @Test
  public void singleElection_twoContest_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity firstContestEntity = new Entity("Contest", 1);
    Entity secondContestEntity = new Entity("Contest", 2);

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
            "[{\"name\":\"myFirstContest\",\"candidates\":[1,2,3],\"description\":\"This contest is important.\"},"
                + "{\"name\":\"mySecondContest\",\"candidates\":[],\"description\":\"This contest is also important.\"}]");
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}
