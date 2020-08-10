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
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

/** A referendum open for election on voter ballots */
@AutoValue
public abstract class Referendum {
  public static final String ENTITY_KIND = "Referendum";
  public static final String TITLE_JSON_KEYWORD = "referendumTitle";
  public static final String DESCRIPTION_JSON_KEYWORD = "referendumSubtitle";
  public static final String TITLE_ENTITY_KEYWORD = "title";
  public static final String DESCRIPTION_ENTITY_KEYWORD = "description";

  public abstract String getTitle();

  public abstract String getDescription();

  public static Builder builder() {
    return new AutoValue_Referendum.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setTitle(String title);

    public abstract Builder setDescription(String description);

    public abstract Referendum build();
  }

  // creates a new Referendum object by extracting the properties from "obj"
  public static Referendum fromJSONObject(JSONObject obj) throws JSONException {
    return Referendum.builder()
        .setTitle(obj.getString(TITLE_JSON_KEYWORD))
        .setDescription(obj.getString(DESCRIPTION_JSON_KEYWORD))
        .build();
  }

  // Converts this Referendum object to a JSON string.
  public String toJsonString() {
    return new Gson().toJson(this);
  }

  // Creates a new Referendum object by using the propperties of the provided Referendum entity
  public static Referendum fromEntity(Entity entity) {
    return Referendum.builder()
        .setTitle((String) entity.getProperty(TITLE_ENTITY_KEYWORD))
        .setDescription((String) entity.getProperty(DESCRIPTION_ENTITY_KEYWORD))
        .build();
  }

  // Converts the Referendum into a Datastore Entity and puts the Entity into the given Datastore
  // instance.
  public long addToDatastore(DatastoreService datastore) {
    Entity entity = new Entity(ENTITY_KIND);
    entity.setProperty(TITLE_ENTITY_KEYWORD, this.getTitle());
    entity.setProperty(DESCRIPTION_ENTITY_KEYWORD, this.getDescription());
    datastore.put(entity);
    return entity.getKey().getId();
  }
}
