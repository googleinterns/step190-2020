package com.google.sps.data;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.sps.servlets.ServletUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServletUtils.class)
public class CandidateTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());
  private String searchUrl =
      "https://api.wevoteusa.org/apis/v1/searchAll/"
          + "?csrfmiddlewaretoken=SvVojWyYxJk3vSPQqXOzVg8q9M9PpDBtF4qo8wcAVn0yUm18g97vi4RZXwgyshNi"
          + "&text_from_search_field=Jane+Doe"
          + "&voter_device_id=CmrnE4BCbd7E6vUMxCod49oSwY1AK1z7xxSybTtMBPdgA23aj2PO2pVLxPEJulNiyWfjQsUFpM3776tF68lTUlCS";
  private String candidateUrl =
      "https://api.wevoteusa.org/apis/v1/ballotItemRetrieve/"
          + "?csrfmiddlewaretoken=SvVojWyYxJk3vSPQqXOzVg8q9M9PpDBtF4qo8wcAVn0yUm18g97vi4RZXwgyshNi"
          + "&kind_of_ballot_item=CANDIDATE&ballot_item_id=&ballot_item_we_vote_id=myId";

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
  public void allFieldsPresent_twoChannels_testFromJsonObject() throws IOException {
    ImmutableMap<String, String> expectedChannelsMap =
        ImmutableMap.<String, String>builder()
            .put("Twitter", "twitterHandle")
            .put("Facebook", "facebookPage")
            .build();
    JSONObject candidateJSON =
        new JSONObject(
            "{\""
                + Candidate.NAME_JSON_KEYWORD
                + "\":\"Jane Doe\",\""
                + Candidate.PARTY_JSON_KEYWORD
                + "\":\"Green Party\",\""
                + Candidate.CHANNELS_JSON_KEYWORD
                + "\":[{\"type\":\"Twitter\", \"id\":\"twitterHandle\"}, {\"type\":\"Facebook\", \"id\":\"facebookPage\"}],\""
                + Candidate.CAMPAIGN_URL_JSON_KEYWORD
                + "\":\"www.janedoe.org\"}");
    mockStatic(ServletUtils.class);
    when(ServletUtils.readFromApiUrl(searchUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"search_results\": [{\"we_vote_id\": \"myId\"}]}")));
    when(ServletUtils.readFromApiUrl(candidateUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"ballotpedia_candidate_summary\": \"mySummary\"}")));
    Candidate newCandidate = Candidate.fromJSONObject(candidateJSON);

    Assert.assertEquals(newCandidate.getName(), "Jane Doe");
    Assert.assertEquals(newCandidate.getPartyAffiliation(), "Green Party");
    Assert.assertEquals(newCandidate.getCampaignSite(), "www.janedoe.org");
    Assert.assertEquals(newCandidate.getChannels(), expectedChannelsMap);
    Assert.assertEquals(newCandidate.getPlatformDescription(), "mySummary");
  }

  @Test
  public void allFieldsPresent_oneChannel_testFromJsonObject() throws IOException {
    ImmutableMap<String, String> expectedChannelsMap =
        ImmutableMap.<String, String>builder().put("Twitter", "twitterHandle").build();
    JSONObject candidateJSON =
        new JSONObject(
            "{\""
                + Candidate.NAME_JSON_KEYWORD
                + "\":\"Jane Doe\",\""
                + Candidate.PARTY_JSON_KEYWORD
                + "\":\"Green Party\",\""
                + Candidate.CHANNELS_JSON_KEYWORD
                + "\":[{\"type\":\"Twitter\", \"id\":\"twitterHandle\"}],\""
                + Candidate.CAMPAIGN_URL_JSON_KEYWORD
                + "\":\"www.janedoe.org\"}");
    mockStatic(ServletUtils.class);
    when(ServletUtils.readFromApiUrl(searchUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"search_results\": [{\"we_vote_id\": \"myId\"}]}")));
    when(ServletUtils.readFromApiUrl(candidateUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"ballotpedia_candidate_summary\": \"mySummary\"}")));

    Candidate newCandidate = Candidate.fromJSONObject(candidateJSON);
    Assert.assertEquals(newCandidate.getName(), "Jane Doe");
    Assert.assertEquals(newCandidate.getPartyAffiliation(), "Green Party");
    Assert.assertEquals(newCandidate.getCampaignSite(), "www.janedoe.org");
    Assert.assertEquals(newCandidate.getChannels(), expectedChannelsMap);
    Assert.assertEquals(newCandidate.getPlatformDescription(), "mySummary");
  }

  @Test
  public void channelsFieldMissing_testFromJsonObject() throws IOException {
    ImmutableMap<String, String> expectedChannelsMap = ImmutableMap.of();
    JSONObject candidateJSON =
        new JSONObject(
            "{\""
                + Candidate.NAME_JSON_KEYWORD
                + "\":\"Jane Doe\",\""
                + Candidate.PARTY_JSON_KEYWORD
                + "\":\"Green Party\",\""
                + Candidate.CAMPAIGN_URL_JSON_KEYWORD
                + "\":\"www.janedoe.org\"}");
    mockStatic(ServletUtils.class);
    when(ServletUtils.readFromApiUrl(searchUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"search_results\": [{\"we_vote_id\": \"myId\"}]}")));
    when(ServletUtils.readFromApiUrl(candidateUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"ballotpedia_candidate_summary\": \"mySummary\"}")));

    Candidate newCandidate = Candidate.fromJSONObject(candidateJSON);
    Assert.assertEquals(newCandidate.getName(), "Jane Doe");
    Assert.assertEquals(newCandidate.getPartyAffiliation(), "Green Party");
    Assert.assertEquals(newCandidate.getCampaignSite(), "www.janedoe.org");
    Assert.assertEquals(newCandidate.getChannels(), expectedChannelsMap);
    Assert.assertEquals(newCandidate.getPlatformDescription(), "mySummary");
  }

  @Test
  public void urlFieldMissing_testFromJsonObject() throws IOException {
    JSONObject candidateJSON =
        new JSONObject(
            "{\""
                + Candidate.NAME_JSON_KEYWORD
                + "\":\"Jane Doe\",\""
                + Candidate.PARTY_JSON_KEYWORD
                + "\":\"Green Party\"}");
    mockStatic(ServletUtils.class);
    when(ServletUtils.readFromApiUrl(searchUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"search_results\": [{\"we_vote_id\": \"myId\"}]}")));
    when(ServletUtils.readFromApiUrl(candidateUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"ballotpedia_candidate_summary\": \"mySummary\"}")));

    Candidate newCandidate = Candidate.fromJSONObject(candidateJSON);
    Assert.assertEquals(newCandidate.getName(), "Jane Doe");
    Assert.assertEquals(newCandidate.getPartyAffiliation(), "Green Party");
    Assert.assertEquals(newCandidate.getCampaignSite(), "");
    Assert.assertEquals(newCandidate.getPlatformDescription(), "mySummary");
  }

  @Test
  public void partyFieldMissing_testFromJsonObject() throws IOException {
    JSONObject candidateJSON =
        new JSONObject(
            "{\""
                + Candidate.NAME_JSON_KEYWORD
                + "\":\"Jane Doe\",\""
                + Candidate.CAMPAIGN_URL_JSON_KEYWORD
                + "\":\"www.janedoe.org\"}");
    mockStatic(ServletUtils.class);
    when(ServletUtils.readFromApiUrl(searchUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"search_results\": [{\"we_vote_id\": \"myId\"}]}")));
    when(ServletUtils.readFromApiUrl(candidateUrl))
        .thenReturn(
            Optional.of(new JSONObject("{\"ballotpedia_candidate_summary\": \"mySummary\"}")));

    Candidate newCandidate = Candidate.fromJSONObject(candidateJSON);
    Assert.assertEquals(newCandidate.getName(), "Jane Doe");
    Assert.assertEquals(newCandidate.getPartyAffiliation(), "");
    Assert.assertEquals(newCandidate.getCampaignSite(), "www.janedoe.org");
    Assert.assertEquals(newCandidate.getPlatformDescription(), "mySummary");
  }

  @Test
  public void platformDescriptionFieldMissing_testFromJsonObject() throws IOException {
    JSONObject candidateJSON =
        new JSONObject(
            "{\""
                + Candidate.NAME_JSON_KEYWORD
                + "\":\"Jane Doe\",\""
                + Candidate.PARTY_JSON_KEYWORD
                + "\":\"Green Party\"}");
    mockStatic(ServletUtils.class);
    when(ServletUtils.readFromApiUrl(searchUrl)).thenReturn(Optional.of(new JSONObject("{}")));
    when(ServletUtils.readFromApiUrl(candidateUrl)).thenReturn(Optional.of(new JSONObject("{}")));

    Candidate newCandidate = Candidate.fromJSONObject(candidateJSON);
    Assert.assertEquals(newCandidate.getName(), "Jane Doe");
    Assert.assertEquals(newCandidate.getPartyAffiliation(), "Green Party");
    Assert.assertEquals(newCandidate.getCampaignSite(), "");
    Assert.assertEquals(newCandidate.getPlatformDescription(), "");
  }

  @Test
  public void noChannels_testAddToDatastore() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Candidate newCandidate =
        Candidate.builder()
            .setName("Jane Doe")
            .setPartyAffiliation("Green")
            .setPlatformDescription("")
            .setCampaignSite("www.janedoe.org")
            .setChannels(new HashMap<String, String>())
            .build();
    long candidateId = newCandidate.addToDatastore(ds);
    Key candidateKey = KeyFactory.createKey(Candidate.ENTITY_KIND, candidateId);
    Entity candidateEntity = ds.get(candidateKey);
    Assert.assertEquals(
        candidateEntity.getProperty(Candidate.NAME_ENTITY_KEYWORD), newCandidate.getName());
    Assert.assertEquals(
        candidateEntity.getProperty(Candidate.PARTY_ENTITY_KEYWORD),
        newCandidate.getPartyAffiliation());
    Assert.assertEquals(
        candidateEntity.getProperty(Candidate.CAMPAIGN_URL_ENTITY_KEYWORD),
        newCandidate.getCampaignSite());
    Assert.assertEquals(
        candidateEntity.getProperty(Candidate.PLATFORM_ENTITY_KEYWORD),
        newCandidate.getPlatformDescription());
  }

  @Test
  public void withChannels_testAddtoDatastore() throws Exception {
    ImmutableMap<String, String> expectedChannelsMap =
        ImmutableMap.<String, String>builder()
            .put("Twitter", "twitterHandle")
            .put("Facebook", "facebookPage")
            .build();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Candidate newCandidate =
        Candidate.builder()
            .setName("Jane Doe")
            .setPartyAffiliation("Green")
            .setPlatformDescription("")
            .setCampaignSite("www.janedoe.org")
            .setChannels(expectedChannelsMap)
            .build();
    long candidateId = newCandidate.addToDatastore(ds);
    Key candidateKey = KeyFactory.createKey(Candidate.ENTITY_KIND, candidateId);
    Entity candidateEntity = ds.get(candidateKey);

    EmbeddedEntity channelsEntity =
        (EmbeddedEntity) candidateEntity.getProperty(Candidate.CHANNELS_ENTITY_KEYWORD);

    Assert.assertEquals(newCandidate.getChannels(), channelsEntity.getProperties());
    Assert.assertEquals(
        newCandidate.getName(), candidateEntity.getProperty(Candidate.NAME_ENTITY_KEYWORD));
    Assert.assertEquals(
        newCandidate.getPartyAffiliation(),
        candidateEntity.getProperty(Candidate.PARTY_ENTITY_KEYWORD));
    Assert.assertEquals(
        newCandidate.getCampaignSite(),
        candidateEntity.getProperty(Candidate.CAMPAIGN_URL_ENTITY_KEYWORD));
    Assert.assertEquals(
        newCandidate.getPlatformDescription(),
        candidateEntity.getProperty(Candidate.PLATFORM_ENTITY_KEYWORD));
  }
}
