package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
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
  public void testFromJSONObject() {

    Candidate newCandidate =
        Candidate.builder()
            .setName("Jane Doe")
            .setPartyAffiliation("Green")
            .setPlatformDescription("")
            .setCampaignSite("www.janedoe.org")
            .build();

    JSONObject contestJSON =
        new JSONObject(
            "{\""
                + Contest.TYPE_JSON_KEYWORD
                + "\":\"General\",\""
                + Contest.NAME_JSON_KEYWORD
                + "\":\"Governer\",\""
                + Contest.CANDIDATES_JSON_KEYWORD
                + "\":["
                + "{\"name\": \"name1\",\"party\": \"party1\",\"candidateUrl\": \"www.siteOne.com\"}"
                + "]}");

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Contest newContest = Contest.fromJSONObject(ds, contestJSON);
    Set<Long> candidateIds = newContest.getCandidates();
    Assert.assertEquals(candidateIds.size(), 1);
    Assert.assertEquals(newContest.getName(), "Governer");
  }
}
