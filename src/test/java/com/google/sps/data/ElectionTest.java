package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.sps.data.Election;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ElectionTest {
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

  // Test putting API JSON response for one election in an Election object and reading from it.
  @Test
  public void testAddElectionQueryData() throws Exception {
    JSONObject electionQueryJsonObject =
        new JSONObject(
            "{\"elections\": [{\"id\": \"9999\",\"name\": \"myElection\",\"electionDay\": \"myDate\",\"ocdDivisionId\": \"myScope\"}]}");
    JSONArray electionQueryArray = electionQueryJsonObject.getJSONArray("elections");
    JSONObject electionJson = electionQueryArray.getJSONObject(0);

    Election election = Election.fromElectionQuery(electionJson);

    Assert.assertEquals(election.getId(), "9999");
    Assert.assertEquals(election.getName(), "myElection");
    Assert.assertEquals(election.getDate(), "myDate");
    Assert.assertEquals(election.getScope(), "myScope");
  }

  // Test updating an existing Entity in the Datastore.
  @Test
  public void testPutInDatastore() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity("Election");
    entity.setProperty("id", "9999");
    entity.setProperty("name", "myElection");
    entity.setProperty("date", "myDate");
    entity.setProperty("scope", "myScope");
    ds.put(entity);
    long entityKeyId = entity.getKey().getId();

    Election updatedElection =
        Election.builder()
            .setId("0001")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setPropositions(new HashSet<Long>())
            .setPollingStations(new ArrayList<EmbeddedEntity>())
            .build();

    long updatedEntityKeyId = updatedElection.putInDatastore(ds, entity);

    Assert.assertEquals(entityKeyId, updatedEntityKeyId);

    Entity updatedEntity = ds.get(KeyFactory.createKey("Election", updatedEntityKeyId));

    Assert.assertEquals(updatedEntity.getProperty("id"), "0001");
  }

  // Test putting API JSON response for one election in the Datastore and reading from it.
  @Test
  public void testAddToDatastore() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setPropositions(new HashSet<Long>())
            .setPollingStations(new ArrayList<EmbeddedEntity>())
            .build();

    long entityKeyId = election.addToDatastore(ds);
    Entity entity = ds.get(KeyFactory.createKey("Election", entityKeyId));

    Assert.assertEquals(entity.getProperty("id"), "9999");
    Assert.assertEquals(entity.getProperty("name"), "myElection");
    Assert.assertEquals(entity.getProperty("date"), "myDate");
    Assert.assertEquals(entity.getProperty("scope"), "myScope");
  }

  // Test converting an Election Entity into an Election object.
  @Test
  public void testFromEntityToElectionObject() throws Exception {
    Entity entity = new Entity("Election");
    entity.setProperty("id", "9999");
    entity.setProperty("name", "myElection");
    entity.setProperty("date", "myDate");
    entity.setProperty("scope", "myScope");

    Election election = Election.fromEntity(entity);

    Assert.assertEquals(election.getId(), "9999");
    Assert.assertEquals(election.getName(), "myElection");
    Assert.assertEquals(election.getDate(), "myDate");
    Assert.assertEquals(election.getScope(), "myScope");
    Assert.assertEquals(election.getContests(), new HashSet<Long>());
    Assert.assertEquals(election.getPropositions(), new HashSet<Long>());
    Assert.assertEquals(election.getPollingStations(), new ArrayList<EmbeddedEntity>());
  }

  // Test putting voterInfoQuery JSON response for one election in an Election object and reading
  // from it.
  @Test
  public void testAddVoterInfoQueryData() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setPropositions(new HashSet<Long>())
            .setPollingStations(new ArrayList<EmbeddedEntity>())
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"typeName\",\"office\": \"officeName\","
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\"}]}],"
                + "\"earlyVoteSites\": [{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\",\"line1\": \"1\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}]}");

    Election updatedElection = election.fromVoterInfoQuery(ds, voterInfoQueryJson);

    Assert.assertEquals(updatedElection.getContests().size(), 1);
    Assert.assertEquals(updatedElection.getPollingStations().size(), 1);
  }
}
