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
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national public office position. */
@AutoValue
public abstract class Contest {
  public abstract String getName();

  // This Contest references a collection of Candidate entities in Datastore. This HashSet
  // represents their Key names.
  public abstract HashSet<String> getCandidateKeyNames();

  public abstract String getDescription();

  public static Builder builder() {
    return new AutoValue_Contest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setCandidateKeyNames(HashSet<String> candidateKeyNames);

    public abstract Builder setDescription(String description);

    public abstract Contest build();
  }

  // creates a new Contest object by extracting the properties from "contestData"
  public static Contest fromVoterInfoQuery(DatastoreService datastore, JSONObject contestData)
      throws JSONException {
    HashSet<String> candidateKeyNames = new HashSet<String>();

    if (contestData.has("candidates")) {
      for (Object candidateObject : contestData.getJSONArray("candidates")) {
        JSONObject candidate = (JSONObject) candidateObject;
        String candidateEntityKeyName =
            Candidate.fromVoterInfoQuery(candidate).putInDatastore(datastore);
        candidateKeyNames.add(candidateEntityKeyName);
      }
    }

    return Contest.builder()
        .setName(contestData.getString("office"))
        .setCandidateKeyNames(candidateKeyNames)
        // TODO(gianelgado): get value for description
        .setDescription("")
        .build();
  }

  // creates a new Entity and sets the proper properties.
  public String putInDatastore(DatastoreService datastore) {
    Entity entity = new Entity("Contest");
    entity.setProperty("name", this.getName());
    entity.setProperty("candidates", this.getCandidateKeyNames());
    entity.setProperty("description", this.getDescription());
    datastore.put(entity);
    return entity.getKey().getName();
  }
}
