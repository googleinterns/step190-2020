// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.sps.servlets.ServletUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A candidate for a public office position that will appear on voter ballots */
@AutoValue
public abstract class Candidate {

  public static final String ENTITY_KIND = "Candidate";
  public static final String NAME_JSON_KEYWORD = "name";
  public static final String PARTY_JSON_KEYWORD = "party";
  public static final String CHANNELS_JSON_KEYWORD = "channels";
  public static final String CAMPAIGN_URL_JSON_KEYWORD = "candidateUrl";
  public static final String NAME_ENTITY_KEYWORD = "name";
  public static final String PARTY_ENTITY_KEYWORD = "partyAffiliation";
  public static final String CAMPAIGN_URL_ENTITY_KEYWORD = "campaignSite";
  public static final String CHANNELS_ENTITY_KEYWORD = "channels";
  public static final String PLATFORM_ENTITY_KEYWORD = "platformDescription";

  private static final String WE_VOTE_API_BASE_URL =
      "https://api.wevoteusa.org/apis/v1/%s/?csrfmiddlewaretoken=%s";
  private static final String WE_VOTE_SEARCH_ALL_METHOD = "searchAll";
  private static final String WE_VOTE_BALLOT_ITEM_METHOD = "ballotItemRetrieve";
  private static final String WE_VOTE_TOKEN =
      "SvVojWyYxJk3vSPQqXOzVg8q9M9PpDBtF4qo8wcAVn0yUm18g97vi4RZXwgyshNi";
  private static final String WE_VOTE_VOTER_ID =
      "CmrnE4BCbd7E6vUMxCod49oSwY1AK1z7xxSybTtMBPdgA23aj2PO2pVLxPEJulNiyWfjQsUFpM3776tF68lTUlCS";
  private static final String WE_VOTE_ITEM_KIND = "CANDIDATE";

  private static final Logger logger = Logger.getLogger(Candidate.class.getName());

  public abstract String getName();

  public abstract String getPartyAffiliation();

  public abstract String getCampaignSite();

  public abstract String getPlatformDescription();

  public abstract ImmutableMap<String, String> getChannels();

  public static Builder builder() {
    return new AutoValue_Candidate.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setPartyAffiliation(String partyAffiliation);

    public abstract Builder setCampaignSite(String campaignSite);

    public abstract Builder setPlatformDescription(String platformDescription);

    public abstract Builder setChannels(Map<String, String> channels);

    public abstract Candidate build();
  }

  // Creates a new Candidate object by extracting the properties from "candidateData"
  public static Candidate fromJSONObject(JSONObject candidateData) {
    String candidateName;
    String candidateParty;
    String candidateUrl;
    Map<String, String> channelsMap = new HashMap<>();

    try {
      candidateName = candidateData.getString(NAME_JSON_KEYWORD);
    } catch (JSONException e) {
      candidateName = "";
    }

    try {
      candidateParty = candidateData.getString(PARTY_JSON_KEYWORD);
    } catch (JSONException e) {
      candidateParty = "";
    }

    try {
      candidateUrl = candidateData.getString(CAMPAIGN_URL_JSON_KEYWORD);
    } catch (JSONException e) {
      candidateUrl = "";
    }

    if (candidateData.has(CHANNELS_JSON_KEYWORD)) {
      for (Object channel : candidateData.getJSONArray(CHANNELS_JSON_KEYWORD)) {
        JSONObject currentChannel = (JSONObject) channel;
        channelsMap.put(currentChannel.getString("type"), currentChannel.getString("id"));
      }
    }

    return Candidate.builder()
        .setName(candidateName)
        .setPartyAffiliation(candidateParty)
        .setCampaignSite(candidateUrl)
        .setPlatformDescription(getPlatformDescriptionFromWeVoteApi(candidateName))
        .setChannels(channelsMap)
        .build();
  }

  /**
   * Use the WeVote API to search for the name of a candidate and get its we_vote_id, then use the
   * we_vote_id with the WeVote API to get the candidate description from Ballotpedia.
   */
  public static String getPlatformDescriptionFromWeVoteApi(String candidateName) {
    if (candidateName.equals("")) {
      return "";
    }

    String candidateDescription;
    try {
      // We need to search for the candidate's name to get their we_vote_id first.
      JSONObject searchObject =
          ServletUtils.readFromApiUrl(
                  String.format(
                      WE_VOTE_API_BASE_URL + "&text_from_search_field=%s&voter_device_id=%s",
                      WE_VOTE_SEARCH_ALL_METHOD,
                      WE_VOTE_TOKEN,
                      candidateName.replaceAll(" ", "+"),
                      WE_VOTE_VOTER_ID),
                  false)
              .get();

      JSONArray searchObjectResult = searchObject.getJSONArray("search_results");

      if (searchObjectResult.length() == 0) {
        return "";
      }

      String candidateWeVoteId = ((JSONObject) searchObjectResult.get(0)).getString("we_vote_id");

      JSONObject candidateObject =
          ServletUtils.readFromApiUrl(
                  String.format(
                      WE_VOTE_API_BASE_URL
                          + "&kind_of_ballot_item=%s&ballot_item_id=&ballot_item_we_vote_id=%s",
                      WE_VOTE_BALLOT_ITEM_METHOD,
                      WE_VOTE_TOKEN,
                      WE_VOTE_ITEM_KIND,
                      candidateWeVoteId),
                  /* isXml= */ false)
              .get();
      candidateDescription = candidateObject.getString("ballotpedia_candidate_summary");
    } catch (IOException | JSONException | NoSuchElementException e) {
      logger.log(Level.WARNING, "Was unable to retrieve description from given URL");
      return "";
    }

    return candidateDescription;
  }

  // Converts this Candidate object to a JSON string.
  public String toJsonString() {
    return new Gson().toJson(this);
  }

  // Creates a new Candidate object by using the properties of the provided Candidate entity
  public static Candidate fromEntity(Entity entity) {
    Map<String, String> channelsMap = new HashMap<>();
    EmbeddedEntity channelsEntity = (EmbeddedEntity) entity.getProperty(CHANNELS_ENTITY_KEYWORD);

    if (channelsEntity != null) {
      for (String channelType : channelsEntity.getProperties().keySet()) {
        channelsMap.put(channelType, (String) channelsEntity.getProperty(channelType));
      }
    }

    return Candidate.builder()
        .setName((String) entity.getProperty(NAME_ENTITY_KEYWORD))
        .setPartyAffiliation((String) entity.getProperty(PARTY_ENTITY_KEYWORD))
        .setCampaignSite((String) entity.getProperty(CAMPAIGN_URL_ENTITY_KEYWORD))
        .setPlatformDescription((String) entity.getProperty(PLATFORM_ENTITY_KEYWORD))
        .setChannels(channelsMap)
        .build();
  }

  // Converts the Candidate into a Datastore Entity and puts the Entity into the given Datastore
  // instance.
  public long addToDatastore(DatastoreService datastore) {
    Entity entity = new Entity(ENTITY_KIND);
    entity.setProperty(NAME_ENTITY_KEYWORD, this.getName());
    entity.setProperty(PARTY_ENTITY_KEYWORD, this.getPartyAffiliation());
    entity.setProperty(CAMPAIGN_URL_ENTITY_KEYWORD, this.getCampaignSite());
    entity.setProperty(PLATFORM_ENTITY_KEYWORD, this.getPlatformDescription());

    EmbeddedEntity channelsEntity = new EmbeddedEntity();
    for (String channelType : this.getChannels().keySet()) {
      channelsEntity.setProperty(channelType, this.getChannels().get(channelType));
    }
    entity.setProperty(CHANNELS_ENTITY_KEYWORD, channelsEntity);

    datastore.put(entity);
    return entity.getKey().getId();
  }
}
