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
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national public office position. */
@AutoValue
public abstract class Contest {
  public abstract String getName();

  // This Contest references a collection of Candidate entities in Datastore. This HashSet
  // represents their Key names.
  public abstract Set<Long> getCandidates();

  public abstract String getDescription();

  public static Builder builder() {
    return new AutoValue_Contest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setCandidates(Set<Long> candidates);

    public abstract Builder setDescription(String description);

    public abstract Contest build();
  }

  // Creates a new Contest object by extracting the properties from "contestData". For each of its
  // "candidate" properties, creates a new Candidate object and inserts it into the given Datastore
  // instance.
  public static Contest fromVoterInfoQuery(DatastoreService datastore, JSONObject contestData)
      throws JSONException {
    Set<Long> candidateKeyIds = new HashSet<>();

    if (contestData.has("candidates")) {
      for (Object candidateObject : contestData.getJSONArray("candidates")) {
        JSONObject candidate = (JSONObject) candidateObject;
        long candidateEntityKeyId =
            Candidate.fromVoterInfoQuery(candidate).addToDatastore(datastore);
        candidateKeyIds.add(candidateEntityKeyId);
      }
    }

    return Contest.builder()
        .setName(contestData.getString("office"))
        .setCandidates(candidateKeyIds)
        // TODO(gianelgado): get value for description
        .setDescription("")
        .build();
  }

  // Creates a new Contest object by using the propperties of the provided
  // contenst entity
  public static Contest fromEntity(Entity entity) {
    HashSet<Long> candidates = new HashSet<>();
    if (entity.getProperty("candidates") != null) {
      candidates = (HashSet<Long>) entity.getProperty("candidates");
    }

    return Contest.builder()
        .setName((String) entity.getProperty("name"))
        .setDescription((String) entity.getProperty("description"))
        .setCandidates(candidates)
        .build();
  }

  // Converts the Contest into a Datastore Entity and puts the Entity into the given Datastore
  // instance.
  public long addToDatastore(DatastoreService datastore) {
    Entity entity = new Entity("Contest");
    entity.setProperty("name", this.getName());
    entity.setProperty("candidates", this.getCandidates());
    entity.setProperty("description", this.getDescription());
    datastore.put(entity);
    return entity.getKey().getId();
  }
}
