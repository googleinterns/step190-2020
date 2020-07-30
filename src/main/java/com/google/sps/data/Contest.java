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
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

/** A state or national public office position. */
@AutoValue
public abstract class Contest {
  public abstract String getName();

  public abstract HashMap<Long, Candidate> getCandidates();

  public abstract String getDescription();

  public static Builder builder() {
    return new AutoValue_Contest.Builder();
  }

  // creates a new Contest object by extracting the properties from "obj"
  public static Contest fromJSONObject(JSONObject obj) throws JSONException {
    return Contest.builder()
        .setName(obj.getString("office"))
        // TODO(caseyprice): get values for candidates and description
        .setCandidates(new HashMap<Long, Candidate>())
        .setDescription("")
        .build();
  }

  // creates a new Entity and sets the proper properties.
  public Entity toEntity() {
    Entity entity = new Entity("Contest");
    entity.setProperty("name", this.getName());
    entity.setProperty("candidates", new ArrayList<String>());
    entity.setProperty("description", this.getDescription());
    return entity;
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setCandidates(HashMap<Long, Candidate> candidates);

    public abstract Builder setDescription(String description);

    public abstract Contest build();
  }
}
