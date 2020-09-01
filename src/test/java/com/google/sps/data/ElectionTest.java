package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ElectionTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

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
            "{\"elections\": [{\"id\": \"9999\",\"name\": \"myElection\","
                + "\"electionDay\": \"myDate\",\"ocdDivisionId\": \"myScope\"}]}");
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
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
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
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
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
    HashSet<Long> someIds = new HashSet<>();
    someIds.add(Long.parseLong("1"));
    someIds.add(Long.parseLong("2"));
    Entity entity = new Entity("Election");
    entity.setProperty("id", "9999");
    entity.setProperty("name", "myElection");
    entity.setProperty("date", "myDate");
    entity.setProperty("scope", "myScope");
    entity.setProperty("contests", someIds);
    entity.setProperty("referendums", someIds);
    entity.setProperty("divisions", ImmutableSet.of("firstDistrict", "secondDistrict"));

    Election election = Election.fromEntity(entity);

    Assert.assertEquals(election.getId(), "9999");
    Assert.assertEquals(election.getName(), "myElection");
    Assert.assertEquals(election.getDate(), "myDate");
    Assert.assertEquals(election.getScope(), "myScope");
    Assert.assertEquals(election.getContests(), someIds);
    Assert.assertEquals(election.getReferendums(), someIds);
    Assert.assertEquals(
        election.getDivisions(), ImmutableSet.of("firstDistrict", "secondDistrict"));
  }

  // Test putting voterInfoQuery JSON response for one election in an Election object and reading
  // from it.
  @Test
  public void testAddVoterInfoQueryData_addTwoDistricts_AddBothContests_omitNothing()
      throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"type1\",\"office\": \"officeName\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteOne.com\"}]},"
                + "{\"type\": \"type2\",\"office\": \"officeName\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\":\"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\":\"www.siteTwo.com\"}]}]}");

    Election updatedElection =
        election.fromVoterInfoQuery(
            ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict", "mySecondDistrict"));

    Assert.assertEquals(updatedElection.getContests().size(), 2);
    Assert.assertEquals(
        updatedElection.getDivisions(), ImmutableSet.of("myFirstDistrict", "mySecondDistrict"));
  }

  @Test
  public void testAddVoterInfoQueryData_noInitialDistrict_addOneDistrict_omitOneContest()
      throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"type1\",\"office\": \"officeName\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteOne.com\"}]},"
                + "{\"type\": \"type2\",\"office\": \"officeName\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\":\"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\":\"www.siteTwo.com\"}]}]}");

    Election updatedElection =
        election.fromVoterInfoQuery(ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict"));

    Assert.assertEquals(updatedElection.getContests().size(), 1);
    Assert.assertEquals(updatedElection.getDivisions(), ImmutableSet.of("myFirstDistrict"));
  }

  @Test
  public void testAddVoterInfoQueryData_oneInitialdistrict_addOneDistrict_omitOneContest()
      throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    HashSet<String> initialDistricts = new HashSet<>();
    initialDistricts.add("mySecondDistrict");
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setReferendums(new HashSet<Long>())
            .setDivisions(initialDistricts)
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"type1\",\"office\": \"officeName\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteOne.com\"}]},"
                + "{\"type\": \"type2\",\"office\": \"officeName\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\":\"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\":\"www.siteTwo.com\"}]}]}");

    Election updatedElection =
        election.fromVoterInfoQuery(ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict"));

    Assert.assertEquals(updatedElection.getContests().size(), 1);
    Assert.assertEquals(
        updatedElection.getDivisions(), ImmutableSet.of("myFirstDistrict", "mySecondDistrict"));
  }

  // Test putting voterInfoQuery JSON response for one election in an Election object and reading
  // from it.
  @Test
  public void testAddReferendumsToElection_addTwoDistricts_addTwoReferendums_omitNothing()
      throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"Referendum\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"referendumTitle\": \"Proposition 1\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 1.\"},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"referendumTitle\": \"Proposition 2\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 2.\"}]}]}");

    Election updatedElection =
        election.fromVoterInfoQuery(
            ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict", "mySecondDistrict"));

    Assert.assertEquals(updatedElection.getReferendums().size(), 2);
    Assert.assertEquals(
        updatedElection.getDivisions(), ImmutableSet.of("myFirstDistrict", "mySecondDistrict"));
  }

  @Test
  public void testAddReferendumsToElection_addOneDistricts_addOneReferendum_omitOneReferendum()
      throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"Referendum\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"referendumTitle\": \"Proposition 1\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 1.\"},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"referendumTitle\": \"Proposition 2\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 2.\"}]}]}");

    Election updatedElection =
        election.fromVoterInfoQuery(ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict"));

    Assert.assertEquals(updatedElection.getReferendums().size(), 1);
    Assert.assertEquals(updatedElection.getDivisions(), ImmutableSet.of("myFirstDistrict"));
  }

  // Test putting voterInfoQuery JSON response for one election in an Election object and reading
  // from it.
  @Test
  public void testAddAllFieldsToElection_addTwoDistricts_omitNothing() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"type1\",\"office\": \"officeName\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteTwo.com\"}]},"
                + "{\"type\": \"type2\",\"office\": \"officeName\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteOne.com\"}]},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"referendumTitle\": \"Proposition 1\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 1.\"},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"referendumTitle\": \"Proposition 2\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 2.\"}]}]}");

    Election updatedElection =
        election.fromVoterInfoQuery(
            ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict", "mySecondDistrict"));

    Assert.assertEquals(updatedElection.getContests().size(), 2);
    Assert.assertEquals(updatedElection.getReferendums().size(), 2);
    Assert.assertEquals(
        updatedElection.getDivisions(), ImmutableSet.of("myFirstDistrict", "mySecondDistrict"));
    Assert.assertTrue(updatedElection.isPopulatedByVoterInfoQuery());
  }

  @Test
  public void testAddAllFieldsToElection_addOneDistrict_omitOneContest_omitOneReferendum()
      throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Election election =
        Election.builder()
            .setId("9999")
            .setName("myElection")
            .setDate("myDate")
            .setScope("myScope")
            .setContests(new HashSet<Long>())
            .setReferendums(new HashSet<Long>())
            .setDivisions(new HashSet<String>())
            .build();
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"type1\",\"office\": \"officeName\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteTwo.com\"}]},"
                + "{\"type\": \"type2\",\"office\": \"officeName\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteOne.com\"}]},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"referendumTitle\": \"Proposition 1\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 1.\"},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"mySecondDistrict\"},"
                + "\"referendumTitle\": \"Proposition 2\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 2.\"}]}]}");

    Election updatedElection =
        election.fromVoterInfoQuery(ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict"));

    Assert.assertEquals(updatedElection.getContests().size(), 1);
    Assert.assertEquals(updatedElection.getReferendums().size(), 1);
    Assert.assertEquals(updatedElection.getDivisions(), ImmutableSet.of("myFirstDistrict"));
    Assert.assertTrue(updatedElection.isPopulatedByVoterInfoQuery());
  }

  // Test putting API JSON response for one election in an Election object and reading from it.
  @Test
  public void testPopulatingAllFieldsOfElectionFromJson() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    JSONObject electionQueryJsonObject =
        new JSONObject(
            "{\"elections\": [{\"id\": \"9999\",\"name\": \"myElection\","
                + "\"electionDay\": \"myDate\",\"ocdDivisionId\": \"myScope\"}]}");
    JSONArray electionQueryArray = electionQueryJsonObject.getJSONArray("elections");
    JSONObject electionJson = electionQueryArray.getJSONObject(0);
    JSONObject voterInfoQueryJson =
        new JSONObject(
            "{\"election\": {\"id\": \"9999\"},"
                + "\"contests\": [{\"type\": \"type1\",\"office\": \"officeName\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteTwo.com\"}]},"
                + "{\"type\": \"type2\",\"office\": \"officeName\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"candidates\": [{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteTwo.com\"}]},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"referendumTitle\": \"Proposition 1\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 1.\"},"
                + "{\"type\": \"Referendum\", \"district\":{\"id\": \"myFirstDistrict\"},"
                + "\"referendumTitle\": \"Proposition 2\","
                + "\"referendumSubtitle\": \"Subtitle text for Prop 2.\"}]}]}");

    Election election =
        Election.fromElectionQuery(electionJson)
            .fromVoterInfoQuery(ds, voterInfoQueryJson, ImmutableSet.of("myFirstDistrict"));

    Assert.assertEquals(election.getId(), "9999");
    Assert.assertEquals(election.getName(), "myElection");
    Assert.assertEquals(election.getDate(), "myDate");
    Assert.assertEquals(election.getScope(), "myScope");
    Assert.assertEquals(election.getContests().size(), 2);
    Assert.assertEquals(election.getReferendums().size(), 2);
    Assert.assertEquals(election.getDivisions().size(), 1);
  }
}
