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
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national election that will appear on voter ballots */
@AutoValue
public abstract class Election {
  public abstract String getId();

  public abstract String getName();

  public abstract String getDate();

  public abstract String getScope();

  // This Election references a collection of Contest entities in Datastore. This HashSet represents
  // their Key names.
  public abstract HashSet<Long> getContests();

  // This Election references a collection of Proposition entities in Datastore. This HashSet
  // represents their Key names.
  public abstract HashSet<Long> getPropositions();

  public static Builder builder() {
    return new AutoValue_Election.Builder();
  }

  public abstract Builder toBuilder();

  public Election withContests(HashSet<Long> contests) {
    return toBuilder().setContests(contests).build();
  }

  public Election withPropositions(HashSet<Long> propositions) {
    return toBuilder().setPropositions(propositions).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setScope(String scope);

    public abstract Builder setDate(String date);

    public abstract Builder setContests(HashSet<Long> contests);

    public abstract Builder setPropositions(HashSet<Long> propositions);

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
        .setId(electionQueryData.getString("id"))
        .setName(electionQueryData.getString("name"))
        .setDate(electionQueryData.getString("electionDay"))
        .setScope(electionQueryData.getString("ocdDivisionId"))
        .setContests(new HashSet<Long>())
        .setPropositions(new HashSet<Long>())
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
  public Election fromVoterInfoQuery(DatastoreService datastore, JSONObject voterInfoQueryData)
      throws JSONException {
    HashSet<Long> contestKeyList = this.getContests();
    HashSet<Long> propositionKeyList = new HashSet<>();
    if (voterInfoQueryData.has("contests")) {
      JSONArray contestListData = voterInfoQueryData.getJSONArray("contests");
      for (Object contestObject : contestListData) {
        JSONObject contest = (JSONObject) contestObject;

        Long contestEntityKeyId =
            Contest.fromVoterInfoQuery(datastore, contest).addToDatastore(datastore);
        contestKeyList.add(contestEntityKeyId);
      }
    }

    // TODO(caseyprice): get values for propositions

    return this.withContests(contestKeyList);
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
    HashSet<Long> contests = new HashSet<>();
    HashSet<Long> propositions = new HashSet<>();
    if (entity.getProperty("contests") != null) {
      contests = (HashSet<Long>) entity.getProperty("contests");
    }

    if (entity.getProperty("propositions") != null) {
      propositions = (HashSet<Long>) entity.getProperty("propositions");
    }

    return Election.builder()
        .setId((String) entity.getProperty("id"))
        .setName((String) entity.getProperty("name"))
        .setDate((String) entity.getProperty("date"))
        .setScope((String) entity.getProperty("scope"))
        .setContests(contests)
        .setPropositions(propositions)
        .build();
  }

  /**
   * Creates a new Entity with properties based on this object's members and stores in the
   * Datastore.
   *
   * @param datastore the DatastoreService to store the new Entity
   */
  public long addToDatastore(DatastoreService datastore) {
    return putInDatastore(datastore, new Entity("Election"));
  }

  /**
   * Assigns the given Entity the properties of this Election object and puts it into the given
   * Datastore instance.
   *
   * @param datastore the DatastoreService to store the new Entity
   */
  public long putInDatastore(DatastoreService datastore, Entity entity) {
    /* The "id" of an Election Entity is stored as a property instead of replacing the
     * Datastore-generated ID because Datastore may accidentally reassign IDs to other
     * entities. To avoid this problem, I would have to obtain a block of IDs with
     * allocateIds(), but this is also difficult because election IDs are not always
     * consecutive numbers and other entities we plan to store in Datastore will not
     * have IDs from the Civic Information API (ex. policies) */
    entity.setProperty("id", this.getId());
    entity.setProperty("name", this.getName());
    entity.setProperty("scope", this.getScope());
    entity.setProperty("date", this.getDate());
    entity.setProperty("contests", this.getContests());
    entity.setProperty("propositions", this.getPropositions());
    datastore.put(entity);
    return entity.getKey().getId();
  }
}
