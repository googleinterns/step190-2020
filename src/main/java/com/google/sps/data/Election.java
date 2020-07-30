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

import com.google.appengine.api.datastore.Entity;
import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national election that will appear on voter ballots */
@AutoValue
public abstract class Election {
  public abstract long getId();

  public abstract String getName();

  public abstract String getDate();

  public abstract String getScope();

  public abstract HashSet<Long> getContests();

  public abstract HashSet<Long> getPropositions();

  public static Builder builder() {
    return new AutoValue_Election.Builder();
  }

  // creates a new Election object by extracting the properties from "obj"
  // TODO(caseyprice): Update setContests and setPropositions parameters
  public static Election fromJSONObject(JSONObject obj) throws JSONException {
    return Election.builder()
        .setId(obj.getLong("id"))
        .setName(obj.getString("name"))
        .setDate(obj.getString("electionDay"))
        .setScope(obj.getString("ocdDivisionId"))
        // TODO(caseyprice): get values for Contests and Propositions
        .setContests(new HashSet<Long>())
        .setPropositions(new HashSet<Long>())
        .build();
  }

  // creates a new Entity and sets the proper properties.
  public Entity toEntity() {
    /* The "id" of an Election Entity is stored as a property instead of replacing the
     * Datastore-generated ID because Datastore may accidentally reassign IDs to other
     * entities. To avoid this problem, I would have to obtain a block of IDs with
     * allocateIds(), but this is also difficult because election IDs are not always
     * consecutive numbers and other entities we plan to store in Datastore will not
     * have IDs from the Civic Information API (ex. policies) */
    Entity entity = new Entity("Election");
    entity.setProperty("electionId", this.getId());
    entity.setProperty("name", this.getName());
    entity.setProperty("scope", this.getScope());
    entity.setProperty("date", this.getDate());
    // TODO(caseyprice): fill contests and propositions with list of key names
    // and check if HashSet can replace ArrayList
    entity.setProperty("contests", new ArrayList<String>());
    entity.setProperty("propositions", new ArrayList<String>());

    return entity;
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(long id);

    public abstract Builder setName(String name);

    public abstract Builder setScope(String scope);

    public abstract Builder setDate(String date);

    public abstract Builder setContests(HashSet<Long> contests);

    public abstract Builder setPropositions(HashSet<Long> propositions);

    public abstract Election build();
  }
}
