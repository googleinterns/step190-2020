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
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.*;
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

  private static Set<Long> CANDIDATE_ID_SET;
  private static Entity electionEntityOne;
  private static Entity contestEntityOne;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @BeforeClass
  public static void classSetUp() {
    helper.setUp();

    CANDIDATE_ID_SET = new HashSet<Long>();
    CANDIDATE_ID_SET.add(Long.valueOf(1));
    CANDIDATE_ID_SET.add(Long.valueOf(2));
    CANDIDATE_ID_SET.add(Long.valueOf(3));

    electionEntityOne = new Entity("Election");
    electionEntityOne.setProperty("id", "9999");
    electionEntityOne.setProperty("name", "myElection");
    electionEntityOne.setProperty("scope", "myScope");
    electionEntityOne.setProperty("date", "myDate");
    electionEntityOne.setProperty("contests", new HashSet<Long>());
    electionEntityOne.setProperty("propositions", new HashSet<Long>());

    contestEntityOne = new Entity("Contest");
    contestEntityOne.setProperty("name", "myContest");
    contestEntityOne.setProperty("candidates", CANDIDATE_ID_SET);
    contestEntityOne.setProperty("description", "This contest is important.");

    helper.tearDown();
  }

  @Before
  public void setUp() {
    helper.setUp();
  }

  @Test
  public void singleElection_singleContest_testReturn() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestId = ds.put(contestEntityOne).getId();

    HashSet<Long> contestSet = (HashSet<Long>) electionEntityOne.getProperty("contests");
    System.out.println(contestId);
    contestSet.add(contestId);
    ds.put(electionEntityOne);

    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "[{\"name\":\"myContest\",\"candidates\":[1,2,3],\"description\":\"This contest is important.\"}]");
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}
