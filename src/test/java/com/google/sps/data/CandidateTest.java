package com.google.sps.data;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CandidateTest {
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
  public void testFromJsonObject() {
    JSONObject candidateJSON =
        new JSONObject(
            "{\""
                + Candidate.NAME_JSON_KEYWORD
                + "\":\"Jane Doe\",\""
                + Candidate.PARTY_JSON_KEYWORD
                + "\":\"Green Party\",\""
                + Candidate.CAMPAIGN_JSON_KEYWORD
                + "\":\"www.janedoe.org\"}");

    System.out.println(candidateJSON);
    Candidate newCandidate = Candidate.fromJSONObject(candidateJSON);
    Assert.assertEquals(newCandidate.getName(), "Jane Doe");
    Assert.assertEquals(newCandidate.getPartyAffiliation(), "Green Party");
    Assert.assertEquals(newCandidate.getCampaignSite(), "www.janedoe.org");
  }
}
