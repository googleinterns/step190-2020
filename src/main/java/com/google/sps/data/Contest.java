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
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national public office position. */
@AutoValue
public abstract class Contest {
  // TODO(caseyprice): Refactor Contest to a name that better fits public office position and 
  // doesn't get confusing with the API.
  public static final String ENTITY_KIND = "Contest";
  public static final String TYPE_JSON_KEYWORD = "type";
  public static final String NAME_JSON_KEYWORD = "office";
  public static final String CANDIDATES_JSON_KEYWORD = "candidates";
  public static final String NAME_ENTITY_KEYWORD = "name";
  public static final String CANDIDATES_ENTITY_KEYWORD = "candidates";
  public static final String DESCRIPTION_ENTITY_KEYWORD = "description";

  public abstract String getName();

  // This Contest references a collection of Candidate entities in Datastore. This HashSet
  // represents their Key names.
  public abstract ImmutableSet<Long> getCandidates();

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
  public static Contest fromJSONObject(DatastoreService datastore, JSONObject contestData)
      throws JSONException {
    Set<Long> candidateKeyIds = new HashSet<>();

    if (contestData.has(CANDIDATES_JSON_KEYWORD)) {
      for (Object candidateObject : contestData.getJSONArray(CANDIDATES_JSON_KEYWORD)) {
        JSONObject candidate = (JSONObject) candidateObject;
        long candidateEntityKeyId = Candidate.fromJSONObject(candidate).addToDatastore(datastore);
        candidateKeyIds.add(candidateEntityKeyId);
      }
    }

    return Contest.builder()
        .setName(contestData.getString(NAME_JSON_KEYWORD))
        .setCandidates(candidateKeyIds)
        // TODO(gianelgado): get value for description
        .setDescription("")
        .build();
  }

  // Creates a new Contest object by using the propperties of the provided Contest entity
  public static Contest fromEntity(Entity entity) {
    ImmutableSet<Long> candidates = ImmutableSet.of();
    if (entity.getProperty(CANDIDATES_ENTITY_KEYWORD) != null) {
      candidates =
          ImmutableSet.copyOf((Collection<Long>) entity.getProperty(CANDIDATES_ENTITY_KEYWORD));
    }

    return Contest.builder()
        .setName((String) entity.getProperty(NAME_ENTITY_KEYWORD))
        .setDescription((String) entity.getProperty(DESCRIPTION_ENTITY_KEYWORD))
        .setCandidates(candidates)
        .build();
  }

  // Converts the Contest into a Datastore Entity and puts the Entity into the given Datastore
  // instance.
  public long addToDatastore(DatastoreService datastore) {
    Entity entity = new Entity(ENTITY_KIND);
    entity.setProperty(NAME_ENTITY_KEYWORD, this.getName());
    entity.setProperty(CANDIDATES_ENTITY_KEYWORD, this.getCandidates());
    entity.setProperty(DESCRIPTION_ENTITY_KEYWORD, this.getDescription());
    datastore.put(entity);
    return entity.getKey().getId();
  }
}
