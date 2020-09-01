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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A referendum open for election on voter ballots */
@AutoValue
public abstract class Referendum {
  public static final String ENTITY_KIND = "Referendum";
  public static final String TITLE_JSON_KEYWORD = "referendumTitle";
  public static final String DESCRIPTION_JSON_KEYWORD = "referendumSubtitle";
  public static final String URL_JSON_KEYWORD = "referendumUrl";
  public static final String SOURCE_JSON_KEYWORD = "sources";
  public static final String SOURCE_NAME_JSON_KEYWORD = "name";
  public static final String DIVISION_JSON_KEYWORD = "district";
  public static final String TITLE_ENTITY_KEYWORD = "title";
  public static final String DESCRIPTION_ENTITY_KEYWORD = "description";
  public static final String SOURCE_ENTITY_KEYWORD = "source";
  public static final String URL_ENTITY_KEYWORD = "url";
  public static final String DIVISION_ENTITY_KEYWORD = "division";
  public static final String SOURCE_CLASS = Referendum.class.getName();
  public static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

  public abstract String getTitle();

  public abstract String getDescription();

  public abstract String getSource();

  public abstract String getUrl();

  public abstract String getDivision();

  public static Builder builder() {
    return new AutoValue_Referendum.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setTitle(String title);

    public abstract Builder setDescription(String description);

    public abstract Builder setSource(String source);

    public abstract Builder setUrl(String url);

    public abstract Builder setDivision(String division);

    public abstract Referendum build();
  }

  // creates a new Referendum object by extracting the properties from "obj"
  public static Referendum fromJSONObject(JSONObject obj) throws JSONException {
    String description;
    String title;
    String source = "";
    String division = "";
    String url;

    try {
      title = obj.getString(TITLE_JSON_KEYWORD);
    } catch (JSONException e) {
      LOGGER.logp(Level.WARNING, SOURCE_CLASS, "fromJSONObject", "referendumTitle does not exist");
      throw new JSONException("Malformed referendum JSONObject: referendumTitle does not exist.");
    }

    try {
      url = obj.getString(URL_JSON_KEYWORD);
    } catch (JSONException e) {
      url = "";
    }

    try {
      description = obj.getString(DESCRIPTION_JSON_KEYWORD);
    } catch (JSONException e) {
      description = "";
    }

    if (obj.has(SOURCE_JSON_KEYWORD)) {
      // "source" field is given as a list of Strings, so put them into a list as one String
      JSONArray sourceList = obj.getJSONArray(SOURCE_JSON_KEYWORD);
      source =
          StreamSupport.stream(sourceList.spliterator(), false)
              .map(sourceObject -> ((JSONObject) sourceObject).getString(SOURCE_NAME_JSON_KEYWORD))
              .collect(Collectors.joining(", "));
    }

    if (obj.has(DIVISION_JSON_KEYWORD)) {
      division = obj.getJSONObject(DIVISION_JSON_KEYWORD).getString("id");
    }

    return Referendum.builder()
        .setTitle(title)
        .setDescription(description)
        .setSource(source)
        .setUrl(url)
        .setDivision(division)
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
        .setSource((String) entity.getProperty(SOURCE_ENTITY_KEYWORD))
        .setUrl((String) entity.getProperty(URL_ENTITY_KEYWORD))
        .setDivision((String) entity.getProperty(DIVISION_ENTITY_KEYWORD))
        .build();
  }

  // Converts the Referendum into a Datastore Entity and puts the Entity into the given Datastore
  // instance.
  public long addToDatastore(DatastoreService datastore) {
    Entity entity = new Entity(ENTITY_KIND);
    entity.setProperty(TITLE_ENTITY_KEYWORD, this.getTitle());
    entity.setProperty(DESCRIPTION_ENTITY_KEYWORD, this.getDescription());
    entity.setProperty(SOURCE_ENTITY_KEYWORD, this.getSource());
    entity.setProperty(URL_ENTITY_KEYWORD, this.getUrl());
    entity.setProperty(DIVISION_ENTITY_KEYWORD, this.getDivision());
    datastore.put(entity);
    return entity.getKey().getId();
  }
}
