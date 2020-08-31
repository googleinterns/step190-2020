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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national election that will appear on voter ballots */
@AutoValue
public abstract class Election {
  public static final String ENTITY_KIND = "Election";
  public static final String ELECTIONS_JSON_KEYWORD = "elections";
  public static final String ID_JSON_KEYWORD = "id";
  public static final String NAME_JSON_KEYWORD = "name";
  public static final String DATE_JSON_KEYWORD = "electionDay";
  public static final String SCOPE_JSON_KEYWORD = "ocdDivisionId";
  public static final String CONTESTS_JSON_KEYWORD = "contests";
  public static final String ID_ENTITY_KEYWORD = "id";
  public static final String NAME_ENTITY_KEYWORD = "name";
  public static final String DATE_ENTITY_KEYWORD = "date";
  public static final String SCOPE_ENTITY_KEYWORD = "scope";
  public static final String CONTESTS_ENTITY_KEYWORD = "contests";
  public static final String REFERENDUMS_ENTITY_KEYWORD = "referendums";
  public static final String POLLING_STATIONS_ENTITY_KEYWORD = "pollingStations";
  public static final String EARLY_VOTE_JSON_KEYWORD = "earlyVoteSites";
  public static final String EARLY_VOTE_LOCATION_TYPE = "earlyVoteSite";
  public static final String DROP_OFF_JSON_KEYWORD = "dropOffLocations";
  public static final String DROP_OFF_LOCATION_TYPE = "dropOffLocation";
  public static final String DIVISIONS_ENTITY_KEYWORD = "divisions";

  public abstract String getId();

  public abstract String getName();

  public abstract String getDate();

  public abstract String getScope();

  // This Election references a collection of Contest entities in Datastore. This HashSet represents
  // their Key names.
  public abstract Set<Long> getContests();

  // This Election references a collection of Referendum entities in Datastore. This HashSet
  // represents their Key names.
  public abstract Set<Long> getReferendums();

  // All the divisions that have been queried with this election.
  public abstract Set<String> getDivisions();

  public static Builder builder() {
    return new AutoValue_Election.Builder();
  }

  public abstract Builder toBuilder();

  public Election withContests(Set<Long> contests) {
    return toBuilder().setContests(contests).build();
  }

  public Election withReferendums(Set<Long> referendums) {
    return toBuilder().setReferendums(referendums).build();
  }

  public Election withDivisions(Set<String> divisions) {
    return toBuilder().setDivisions(divisions).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setScope(String scope);

    public abstract Builder setDate(String date);

    public abstract Builder setContests(Set<Long> contests);

    public abstract Builder setReferendums(Set<Long> Referendums);

    public abstract Builder setDivisions(Set<String> divisions);

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
        .setId(electionQueryData.getString(ID_JSON_KEYWORD))
        .setName(electionQueryData.getString(NAME_JSON_KEYWORD))
        .setDate(electionQueryData.getString(DATE_JSON_KEYWORD))
        .setScope(electionQueryData.getString(SCOPE_JSON_KEYWORD))
        .setContests(new HashSet<Long>())
        .setReferendums(new HashSet<Long>())
        .setDivisions(new HashSet<String>())
        .build();
  }

  /**
   * Creates an Election object with contests and referendums fields from the corresponding
   * properties of "voterInfoQueryData". Copies the remaining fields from this Election object.
   * Delegates creating Contest Entities in Datastore from this Election's list of contests.
   *
   * @param datastore the Datastore to store this election's list of contests
   * @param voterInfoQueryData the JSON output of a voterInfoQuery to the Google Civic Information
   *     API
   * @param divisions the set of divisions which limit the contests to be added
   * @return the new Election object
   */
  public Election fromVoterInfoQuery(
      DatastoreService datastore, JSONObject voterInfoQueryData, Set<String> divisions)
      throws JSONException {
    Set<Long> contestKeyList = this.getContests();
    Set<Long> referendumKeyList = this.getReferendums();
    Set<String> divisionsList = this.getDivisions();
    divisionsList.addAll(divisions);

    if (voterInfoQueryData.has(CONTESTS_JSON_KEYWORD)) {
      JSONArray contestListData = voterInfoQueryData.getJSONArray(CONTESTS_JSON_KEYWORD);
      for (Object contestObject : contestListData) {
        JSONObject contest = (JSONObject) contestObject;
        String currentDivision =
            contest.getJSONObject(Contest.DIVISION_JSON_KEYWORD).getString("id");

        // Referendums are a separate contest type, so separate them out from the office positions
        // and put them in their own object field.
        if (divisions.contains(currentDivision)) {
          if (contest.getString(Contest.TYPE_JSON_KEYWORD).equals(Referendum.ENTITY_KIND)) {
            long referendumEntityKeyId =
                Referendum.fromJSONObject(contest).addToDatastore(datastore);
            referendumKeyList.add(referendumEntityKeyId);
          } else {
            long contestEntityKeyId =
                Contest.fromJSONObject(datastore, contest).addToDatastore(datastore);
            contestKeyList.add(contestEntityKeyId);
          }
        }
      }
    }

    return this.withContests(contestKeyList)
        .withReferendums(referendumKeyList)
        .withDivisions(divisionsList);
  }

  /**
   * Checks if an Election object has been populated by the output of a voterInfoQuery call from the
   * Google Civic Information API.
   *
   * @return true if contests and Referendums contain elements, false otherwise
   */
  public boolean isPopulatedByVoterInfoQuery() {
    return !getContests().isEmpty() && !getReferendums().isEmpty() && !getDivisions().isEmpty();
  }

  /**
   * Creates a new Election object based on the properties of an Election Entity.
   *
   * @param entity the Election Entity to deep copy
   * @return the new Election object
   */
  public static Election fromEntity(Entity entity) {
    Set<Long> contests = new HashSet<>();
    Set<Long> referendums = new HashSet<>();
    Set<String> divisions = new HashSet<>();

    if (entity.getProperty(CONTESTS_ENTITY_KEYWORD) != null) {
      contests = new HashSet<>((Collection<Long>) entity.getProperty(CONTESTS_ENTITY_KEYWORD));
    }

    if (entity.getProperty(REFERENDUMS_ENTITY_KEYWORD) != null) {
      referendums =
          new HashSet<>((Collection<Long>) entity.getProperty(REFERENDUMS_ENTITY_KEYWORD));
    }

    if (entity.getProperty(DIVISIONS_ENTITY_KEYWORD) != null) {
      divisions = new HashSet<>((Collection<String>) entity.getProperty(DIVISIONS_ENTITY_KEYWORD));
    }

    return Election.builder()
        .setId((String) entity.getProperty(ID_ENTITY_KEYWORD))
        .setName((String) entity.getProperty(NAME_ENTITY_KEYWORD))
        .setDate((String) entity.getProperty(DATE_ENTITY_KEYWORD))
        .setScope((String) entity.getProperty(SCOPE_ENTITY_KEYWORD))
        .setContests(contests)
        .setReferendums(referendums)
        .setDivisions(divisions)
        .build();
  }

  /**
   * Creates a new Entity with properties based on this object's members and stores in the
   * Datastore.
   *
   * @param datastore the DatastoreService to store the new Entity
   */
  public long addToDatastore(DatastoreService datastore) {
    return putInDatastore(datastore, new Entity(ENTITY_KIND));
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
    entity.setProperty(ID_ENTITY_KEYWORD, this.getId());
    entity.setProperty(NAME_ENTITY_KEYWORD, this.getName());
    entity.setProperty(DATE_ENTITY_KEYWORD, this.getDate());
    entity.setProperty(SCOPE_ENTITY_KEYWORD, this.getScope());
    entity.setProperty(CONTESTS_ENTITY_KEYWORD, this.getContests());
    entity.setProperty(REFERENDUMS_ENTITY_KEYWORD, this.getReferendums());
    entity.setProperty(DIVISIONS_ENTITY_KEYWORD, this.getDivisions());
    datastore.put(entity);
    return entity.getKey().getId();
  }
}
