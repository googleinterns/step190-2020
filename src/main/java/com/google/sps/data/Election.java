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
import com.google.appengine.api.datastore.Entity;
import com.google.auto.value.AutoValue;
import com.google.sps.servlets.ServletUtils;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national election that will appear on voter ballots */
@AutoValue
public abstract class Election {
  public abstract String getId();

  public abstract String getElectionId();

  public abstract String getName();

  public abstract String getDate();

  public abstract String getScope();

  public abstract HashSet<String> getContests();

  public abstract HashSet<String> getPropositions();

  public static Builder builder() {
    return new AutoValue_Election.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(String id);

    public abstract Builder setElectionId(String electionId);

    public abstract Builder setName(String name);

    public abstract Builder setScope(String scope);

    public abstract Builder setDate(String date);

    public abstract Builder setContests(HashSet<String> contests);

    public abstract Builder setPropositions(HashSet<String> propositions);

    public abstract Election build();
  }

  /**
   * Creates a new Election object by extracting the properties from "electionQueryData".
   *
   * @param electionQueryData the JSON output of an electionQuery to the Google Civic Information
   *     API
   * @return the new Election object
   */
  public static Election fromElectionQuery(JSONObject electionQueryData) throws JSONException {
    return Election.builder()
        .setElectionId(electionQueryData.getString("id"))
        .setName(electionQueryData.getString("name"))
        .setDate(electionQueryData.getString("electionDay"))
        .setScope(electionQueryData.getString("ocdDivisionId"))
        .setContests(new HashSet<String>())
        .setPropositions(new HashSet<String>())
        .build();
  }

  /**
   * Creates a new Election object by extracting the properties from an Election object and from the
   * properties of "voterInfoQueryData". Delegates creating Contest Entities from this election's
   * list of contests.
   *
   * @param election the Election object already populated by fromElectionQuery
   * @param datastore the Datastore to store this election's list of contests
   * @param voterInfoQueryData the JSON output of a voterInfoQuery to the Google Civic Information
   *     API
   * @return the new Election object
   */
  public static Election fromVoterInfoQuery(
      Election election, DatastoreService datastore, JSONObject voterInfoQueryData)
      throws JSONException {
    HashSet<String> contestKeyList = new HashSet<String>();
    HashSet<String> propositionKeyList = new HashSet<String>();

    if (voterInfoQueryData.has("contests")) {
      JSONArray contestListData = voterInfoQueryData.getJSONArray("contests");
      for (Object contestObject : contestListData) {
        JSONObject contest = (JSONObject) contestObject;

        Contest.fromVoterInfoQuery(datastore, contest).putInDatastore(datastore);
      }

      contestKeyList = ServletUtils.getEntityKeyNameList(datastore, "Contest");
    }

    // TODO(caseyprice): get values for propositions

    return Election.builder()
        .setElectionId(election.getElectionId())
        .setName(election.getName())
        .setDate(election.getDate())
        .setScope(election.getScope())
        .setContests(contestKeyList)
        .setPropositions(propositionKeyList)
        .build();
  }

  /**
   * Checks if an Election object has been populated by the output of a voterInfoQuery call from the
   * Google Civic Information API.
   *
   * @return true if contests and propositions contain elements, false otherwise
   */
  public boolean isPopulatedByVoterInfoQuery() {
    return !getContests().isEmpty() && !getPropositions().isEmpty();
  }

  /**
   * Creates a new Election object based on the properties of an Election Entity.
   *
   * @param entity the Election Entity to deep copy
   * @return the new Election object
   */
  public static Election fromEntity(Entity entity) {
    return Election.builder()
        .setId((String) entity.getProperty("electionId"))
        .setName((String) entity.getProperty("name"))
        .setDate((String) entity.getProperty("date"))
        .setScope((String) entity.getProperty("scope"))
        .setContests((HashSet<String>) entity.getProperty("contests"))
        .setPropositions((HashSet<String>) entity.getProperty("propositions"))
        .build();
  }

  /**
   * Creates a new Entity with properties based on this object's members and stores in the
   * Datastore.
   *
   * @param datastore the DatastoreService to store the new Entity
   */
  public void putInDatastore(DatastoreService datastore) {
    Entity entity = new Entity("Election");
    /* The "id" of an Election Entity is stored as a property instead of replacing the
     * Datastore-generated ID because Datastore may accidentally reassign IDs to other
     * entities. To avoid this problem, I would have to obtain a block of IDs with
     * allocateIds(), but this is also difficult because election IDs are not always
     * consecutive numbers and other entities we plan to store in Datastore will not
     * have IDs from the Civic Information API (ex. policies) */
    entity.setProperty("electionId", this.getElectionId());
    entity.setProperty("name", this.getName());
    entity.setProperty("scope", this.getScope());
    entity.setProperty("date", this.getDate());
    entity.setProperty("contests", this.getContests());
    entity.setProperty("propositions", this.getPropositions());
    datastore.put(entity);
  }
}
