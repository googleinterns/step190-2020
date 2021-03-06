package com.google.sps.servlets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import javax.servlet.http.Cookie;
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

  private Entity electionEntityOne;
  private Entity contestEntityOne;
  private Entity contestEntityTwo;
  private Entity candidateEntityOne;
  private Entity candidateEntityTwo;
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
    electionEntityOne.setProperty("divisions", new HashSet<String>());

    contestEntityOne = new Entity("Contest");
    contestEntityOne.setProperty("name", "myFirstContest");
    contestEntityOne.setProperty("candidates", new HashSet<Long>());
    contestEntityOne.setProperty("description", "This contest is important.");
    contestEntityOne.setProperty("source", "Voter Information Project");
    contestEntityOne.setProperty("division", "firstDistrict");

    contestEntityTwo = new Entity("Contest");
    contestEntityTwo.setProperty("name", "mySecondContest");
    contestEntityTwo.setProperty("candidates", new HashSet<Long>());
    contestEntityTwo.setProperty("description", "This contest is also important.");
    contestEntityTwo.setProperty("source", "Voter Information Project");
    contestEntityTwo.setProperty("division", "secondDistrict");

    candidateEntityOne = new Entity("Candidate");
    candidateEntityOne.setProperty("name", "myFirstCandidate");
    candidateEntityOne.setProperty("partyAffiliation", "myParty");
    candidateEntityOne.setProperty("campaignSite", "myWebsite");
    candidateEntityOne.setProperty("platformDescription", "This is a cool candidate.");

    candidateEntityTwo = new Entity("Candidate");
    candidateEntityTwo.setProperty("name", "mySecondCandidate");
    candidateEntityTwo.setProperty("partyAffiliation", "myOtherParty");
    candidateEntityTwo.setProperty("campaignSite", "myOtherWebsite");
    candidateEntityTwo.setProperty("platformDescription", "But this is an even cooler candidate.");

    referendumEntityOne = new Entity("Referendum");
    referendumEntityOne.setProperty("title", "myFirstReferendum");
    referendumEntityOne.setProperty("description", "This is a referendum.");
    referendumEntityOne.setProperty("source", "Voter Information Project");
    referendumEntityOne.setProperty("url", "testUrl");
    referendumEntityOne.setProperty("division", "firstDistrict");

    referendumEntityTwo = new Entity("Referendum");
    referendumEntityTwo.setProperty("title", "mySecondReferendum");
    referendumEntityTwo.setProperty("description", "This is another referendum.");
    referendumEntityTwo.setProperty("source", "Voter Information Project");
    referendumEntityTwo.setProperty("url", "testUrl");
    referendumEntityTwo.setProperty("division", "secondDistrict");
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

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[{\"name\":\"myFirstContest\",\"candidates\":[],\"description\":\"This contest is important.\","
                + "\"source\":\"Voter Information Project\",\"division\":\"firstDistrict\"}],\"referendums\":[]}");
  }

  @Test
  public void singleElection_singleContest_singleCandidate_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity contestEntity = new Entity("Contest");
    Entity candidateEntity = new Entity("Candidate");

    electionEntity.setPropertiesFrom(electionEntityOne);
    contestEntity.setPropertiesFrom(contestEntityOne);
    candidateEntity.setPropertiesFrom(candidateEntityOne);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestId = ds.put(contestEntity).getId();
    long candidateId = ds.put(candidateEntity).getId();

    Collection<Long> candidateSet = (Collection<Long>) contestEntity.getProperty("candidates");
    candidateSet.add(candidateId);
    ds.put(contestEntity);
    Collection<Long> contestSet = (Collection<Long>) electionEntity.getProperty("contests");
    contestSet.add(contestId);
    ds.put(electionEntity);

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[{\"name\":\"myFirstContest\",\"candidates\":[{\"name\":\"myFirstCandidate\","
                + "\"partyAffiliation\":\"myParty\",\"campaignSite\":\"myWebsite\",\"platformDescription\":"
                + "\"This is a cool candidate.\",\"channels\":{}}],\"description\":\"This contest is important.\","
                + "\"source\":\"Voter Information Project\",\"division\":\"firstDistrict\"}],\"referendums\":[]}");
  }

  @Test
  public void singleElection_singleContest_twoCandidate_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity contestEntity = new Entity("Contest");
    Entity firstCandidateEntity = new Entity("Candidate");
    Entity secondCandidateEntity = new Entity("Candidate");

    electionEntity.setPropertiesFrom(electionEntityOne);
    contestEntity.setPropertiesFrom(contestEntityOne);
    firstCandidateEntity.setPropertiesFrom(candidateEntityOne);
    secondCandidateEntity.setPropertiesFrom(candidateEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestId = ds.put(contestEntity).getId();
    long firstCandidateId = ds.put(firstCandidateEntity).getId();
    long secondCandidateId = ds.put(secondCandidateEntity).getId();

    Collection<Long> candidateSet = (Collection<Long>) contestEntity.getProperty("candidates");
    candidateSet.add(firstCandidateId);
    candidateSet.add(secondCandidateId);
    ds.put(contestEntity);
    Collection<Long> contestSet = (Collection<Long>) electionEntity.getProperty("contests");
    contestSet.add(contestId);
    ds.put(electionEntity);

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[{\"name\":\"myFirstContest\",\"candidates\":[{\"name\":\"myFirstCandidate\","
                + "\"partyAffiliation\":\"myParty\",\"campaignSite\":\"myWebsite\",\"platformDescription\":"
                + "\"This is a cool candidate.\",\"channels\":{}},{\"name\":\"mySecondCandidate\",\"partyAffiliation\":\"myOtherParty\","
                + "\"campaignSite\":\"myOtherWebsite\",\"platformDescription\":\"But this is an even cooler candidate.\",\"channels\":{}}],"
                + "\"description\":\"This contest is important.\",\"source\":\"Voter Information Project\",\"division\":\"firstDistrict\"}],"
                + "\"referendums\":[]}");
  }

  @Test
  public void singleElection_twoContest_twoDistricts_testDoGet() throws Exception {
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

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[{\"name\":\"myFirstContest\",\"candidates\":[],\"description\":\"This contest is important.\","
                + "\"source\":\"Voter Information Project\",\"division\":\"firstDistrict\"},"
                + "{\"name\":\"mySecondContest\",\"candidates\":[],\"description\":\"This contest is also important.\","
                + "\"source\":\"Voter Information Project\",\"division\":\"secondDistrict\"}],\"referendums\":[]}");
  }

  @Test
  public void singleElection_twoContest_oneDistrict_testDoGet() throws Exception {
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

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[{\"name\":\"myFirstContest\",\"candidates\":[],\"description\":\"This contest is important.\","
                + "\"source\":\"Voter Information Project\",\"division\":\"firstDistrict\"}],\"referendums\":[]}");
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

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[],"
                + "\"referendums\":[{\"title\":\"myFirstReferendum\",\"description\":\"This is a referendum.\","
                + "\"source\":\"Voter Information Project\",\"url\":\"testUrl\",\"division\":\"firstDistrict\"}]}");
  }

  @Test
  public void singleElection_twoReferendum_twoDistricts_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity firstReferendumEntity = new Entity("Referendum");
    Entity secondReferendumEntity = new Entity("Referendum");

    electionEntity.setPropertiesFrom(electionEntityOne);
    firstReferendumEntity.setPropertiesFrom(referendumEntityOne);
    secondReferendumEntity.setPropertiesFrom(referendumEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long referendumOneId = ds.put(firstReferendumEntity).getId();
    long referendumTwoId = ds.put(secondReferendumEntity).getId();

    HashSet<Long> referendumSet = (HashSet<Long>) electionEntityOne.getProperty("referendums");
    referendumSet.add(referendumOneId);
    referendumSet.add(referendumTwoId);
    ds.put(electionEntityOne);

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[],"
                + "\"referendums\":[{\"title\":\"myFirstReferendum\",\"description\":\"This is a referendum.\","
                + "\"source\":\"Voter Information Project\",\"url\":\"testUrl\",\"division\":\"firstDistrict\"},"
                + "{\"title\":\"mySecondReferendum\",\"description\":\"This is another referendum.\","
                + "\"source\":\"Voter Information Project\",\"url\":\"testUrl\",\"division\":\"secondDistrict\"}]}");
  }

  @Test
  public void singleElection_twoReferendum_oneDistrict_testDoGet() throws Exception {
    Entity electionEntity = new Entity("Election");
    Entity firstReferendumEntity = new Entity("Referendum");
    Entity secondReferendumEntity = new Entity("Referendum");

    electionEntity.setPropertiesFrom(electionEntityOne);
    firstReferendumEntity.setPropertiesFrom(referendumEntityOne);
    secondReferendumEntity.setPropertiesFrom(referendumEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long referendumOneId = ds.put(firstReferendumEntity).getId();
    long referendumTwoId = ds.put(secondReferendumEntity).getId();

    HashSet<Long> referendumSet = (HashSet<Long>) electionEntityOne.getProperty("referendums");
    referendumSet.add(referendumOneId);
    referendumSet.add(referendumTwoId);
    ds.put(electionEntityOne);

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[],"
                + "\"referendums\":[{\"title\":\"myFirstReferendum\",\"description\":\"This is a referendum.\","
                + "\"source\":\"Voter Information Project\",\"url\":\"testUrl\",\"division\":\"firstDistrict\"}]}");
  }

  @Test
  public void noElectionEntityExists_testDoGet() throws IOException {
    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("Election with id 9999 was not found.");
  }

  @Test
  public void queryParameterMissing_testDoGet() throws IOException {
    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn(null);
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("No electionId in the query URL.");
  }

  @Test
  public void divisionCookieMissing_testDoGet() throws IOException {
    Entity electionEntity = new Entity("Election");
    electionEntity.setPropertiesFrom(electionEntityOne);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    ds.put(electionEntityOne);

    Cookie[] returnedCookies = {};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter).println("Divisions information for address not found");
  }

  @Test
  public void oneElection_omitsContestMissingInDatastore_testDoGet() throws IOException {
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

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[{\"name\":\"myFirstContest\",\"candidates\":[],\"description\":\"This contest is important.\","
                + "\"source\":\"Voter Information Project\",\"division\":\"firstDistrict\"}],\"referendums\":[]}");
  }

  @Test
  public void oneElection_omitsCandidateMissingInDatastore_testDoGet() throws IOException {
    Entity electionEntity = new Entity("Election");
    Entity contestEntity = new Entity("Contest");
    electionEntity.setPropertiesFrom(electionEntityOne);
    contestEntity.setPropertiesFrom(contestEntityOne);

    Entity firstCandidateEntity = new Entity("Candidate");
    Entity secondCandidateEntity = new Entity("Candidate", 3);
    firstCandidateEntity.setPropertiesFrom(candidateEntityOne);
    secondCandidateEntity.setPropertiesFrom(candidateEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long contestId = ds.put(contestEntity).getId();
    long firstCandidateId = ds.put(firstCandidateEntity).getId();
    long secondCandidateId = secondCandidateEntity.getKey().getId();

    Collection<Long> candidateSet = (Collection<Long>) contestEntity.getProperty("candidates");
    candidateSet.add(firstCandidateId);
    candidateSet.add(secondCandidateId);
    ds.put(contestEntity);

    Collection<Long> contestSet = (Collection<Long>) electionEntity.getProperty("contests");
    contestSet.add(contestId);
    ds.put(electionEntity);

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[{\"name\":\"myFirstContest\",\"candidates\":[{\"name\":\"myFirstCandidate\","
                + "\"partyAffiliation\":\"myParty\",\"campaignSite\":\"myWebsite\",\"platformDescription\":"
                + "\"This is a cool candidate.\",\"channels\":{}}],"
                + "\"description\":\"This contest is important.\",\"source\":\"Voter Information Project\",\"division\":\"firstDistrict\"}],"
                + "\"referendums\":[]}");
  }

  @Test
  public void oneElection_omitsReferendumMissingInDatastore_testDoGet() throws IOException {
    Entity electionEntity = new Entity("Election");
    Entity firstReferendumEntity = new Entity("Referendum");
    Entity secondReferendumEntity = new Entity("Referendum", 4);

    electionEntity.setPropertiesFrom(electionEntityOne);
    firstReferendumEntity.setPropertiesFrom(referendumEntityOne);
    secondReferendumEntity.setPropertiesFrom(referendumEntityTwo);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long referendumOneId = ds.put(firstReferendumEntity).getId();
    long referendumTwoId = secondReferendumEntity.getKey().getId();

    HashSet<Long> referendumSet = (HashSet<Long>) electionEntityOne.getProperty("referendums");
    referendumSet.add(referendumOneId);
    referendumSet.add(referendumTwoId);
    ds.put(electionEntityOne);

    Cookie[] returnedCookies = {new Cookie("addressDivisions", "firstDistrict|secondDistrict")};
    when(httpServletRequest.getCookies()).thenReturn(returnedCookies);
    when(httpServletRequest.getParameter("electionId")).thenReturn("9999");
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    ContestsServlet contestServlet = new ContestsServlet();
    contestServlet.doGet(httpServletRequest, httpServletResponse);

    verify(printWriter)
        .println(
            "{\"contests\":[],"
                + "\"referendums\":[{\"title\":\"myFirstReferendum\",\"description\":\"This is a referendum.\","
                + "\"source\":\"Voter Information Project\",\"url\":\"testUrl\",\"division\":\"firstDistrict\"}]}");
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}
