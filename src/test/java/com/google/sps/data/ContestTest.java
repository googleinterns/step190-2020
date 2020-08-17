package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.Collection;
import java.util.Set;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ContestTest {
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

  @Test
  public void oneContest_twoCandidates_fromJSONObject() {
    JSONObject contestJSON =
        new JSONObject(
            "{\""
                + Contest.TYPE_JSON_KEYWORD
                + "\":\"General\",\""
                + Contest.NAME_JSON_KEYWORD
                + "\":\"Governer\",\""
                + Contest.CANDIDATES_JSON_KEYWORD
                + "\":["
                + "{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"},"
                + "{\"name\": \"name2\",\"party\": \"party2\",\"candidateUrl\": \"www.siteTwo.com\"}"
                + "],\""
                + Contest.SOURCE_JSON_KEYWORD
                + "\":[{\"name\":\"Voter Information Project\"}]}");

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Contest newContest = Contest.fromJSONObject(ds, contestJSON);
    Set<Long> candidateIds = newContest.getCandidates();
    Assert.assertEquals(candidateIds.size(), 2);
    Assert.assertEquals(newContest.getName(), "Governer");
    Assert.assertEquals(newContest.getDescription(), "");
    Assert.assertEquals(newContest.getSource(), "Voter Information Project");
  }

  @Test
  public void oneContest_twoCandidates_toJsonString() {
    Candidate candidateOne =
        Candidate.builder()
            .setName("Jane Doe")
            .setPartyAffiliation("Green")
            .setPlatformDescription("")
            .setCampaignSite("www.janedoe.org")
            .build();

    Candidate candidateTwo =
        Candidate.builder()
            .setName("James Doe")
            .setPartyAffiliation("Tea")
            .setPlatformDescription("")
            .setCampaignSite("www.jamesdoe.org")
            .build();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long candidateOneId = candidateOne.addToDatastore(ds);
    long candidateTwoId = candidateTwo.addToDatastore(ds);

    ImmutableSet<Long> candidateIdSet =
        ImmutableSet.<Long>builder().add(candidateOneId).add(candidateTwoId).build();

    Contest newContest =
        Contest.builder()
            .setName("Governer")
            .setCandidates(candidateIdSet)
            .setDescription("Race for California Governer")
            .setSource("Voter Information Project")
            .build();

    JsonElement returnedContestJSON = JsonParser.parseString(newContest.toJsonString(ds));

    JsonElement expectedContestJSON =
        JsonParser.parseString(
            "{\""
                + Contest.NAME_ENTITY_KEYWORD
                + "\":\"Governer\", \""
                + Contest.CANDIDATES_ENTITY_KEYWORD
                + "\":["
                + candidateOne.toJsonString()
                + ","
                + candidateTwo.toJsonString()
                + "],\""
                + Contest.DESCRIPTION_ENTITY_KEYWORD
                + "\":\"Race for California Governer\""
                + ",\""
                + Contest.SOURCE_ENTITY_KEYWORD
                + "\":\"Voter Information Project\"}");

    Assert.assertEquals(expectedContestJSON, returnedContestJSON);
  }

  @Test
  public void testFromEntity() {
    ImmutableSet<Long> idSet = ImmutableSet.of(1L, 2L);
    Entity newContestEntity = new Entity(Contest.ENTITY_KIND);
    newContestEntity.setProperty(Contest.NAME_ENTITY_KEYWORD, "Governer");
    newContestEntity.setProperty(Contest.CANDIDATES_ENTITY_KEYWORD, idSet);
    newContestEntity.setProperty(
        Contest.DESCRIPTION_ENTITY_KEYWORD, "Race for California Governer");
    newContestEntity.setProperty(Contest.SOURCE_ENTITY_KEYWORD, "Voter Information Project");

    Contest newContest = Contest.fromEntity(newContestEntity);

    Assert.assertEquals("Governer", newContest.getName());
    Assert.assertEquals(idSet, newContest.getCandidates());
    Assert.assertEquals("Race for California Governer", newContest.getDescription());
    Assert.assertEquals("Voter Information Project", newContest.getSource());
  }

  @Test
  public void testAddToDatastore() throws Exception {
    ImmutableSet<Long> idSet = ImmutableSet.of(1L, 2L);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    Contest newContest =
        Contest.builder()
            .setName("Governer")
            .setCandidates(idSet)
            .setDescription("Race for California Governer")
            .setSource("Voter Information Project")
            .build();

    long contestId = newContest.addToDatastore(ds);
    Key contestKey = KeyFactory.createKey(Contest.ENTITY_KIND, contestId);
    Entity contestEntity = ds.get(contestKey);

    Assert.assertEquals(
        newContest.getName(), contestEntity.getProperty(Contest.NAME_ENTITY_KEYWORD));
    Assert.assertEquals(
        newContest.getCandidates(),
        ImmutableSet.copyOf(
            (Collection<Long>) contestEntity.getProperty(Contest.CANDIDATES_ENTITY_KEYWORD)));
    Assert.assertEquals(
        newContest.getDescription(), contestEntity.getProperty(Contest.DESCRIPTION_ENTITY_KEYWORD));
  }
}
