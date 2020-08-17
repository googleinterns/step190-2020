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
import java.util.HashMap;
import java.util.Map;
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
        // TODO(gianelgado): get value for platformDescription
        .setPlatformDescription("")
        .setChannels(channelsMap)
        .build();
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
