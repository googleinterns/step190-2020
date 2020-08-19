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
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/** A polling station open for voters to vote or drop ballots off at */
@AutoValue
public abstract class PollingStation {
  public static final String ENTITY_KIND = "PollingStation";
  public static final String NAME_JSON_KEYWORD = "name";
  public static final String ADDRESS_JSON_KEYWORD = "address";
  public static final String START_DATE_JSON_KEYWORD = "startDate";
  public static final String END_DATE_JSON_KEYWORD = "endDate";
  public static final String LOCATION_TYPE_JSON_KEYWORD = "locationType";
  public static final String POLLING_HOURS_JSON_KEYWORD = "pollingHours";
  public static final String SOURCES_JSON_KEYWORD = "sources";

  public abstract String getName();

  public abstract String getAddress();

  public abstract String getPollingHours();

  public abstract String getStartDate();

  public abstract String getEndDate();

  public abstract String getLocationType();

  public abstract ImmutableList<String> getSources();

  public static Builder builder() {
    return new AutoValue_PollingStation.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setAddress(String scope);

    public abstract Builder setPollingHours(String hours);

    public abstract Builder setStartDate(String start);

    public abstract Builder setEndDate(String end);

    public abstract Builder setLocationType(String type);

    public abstract Builder setSources(ImmutableList<String> sources);

    public abstract PollingStation build();
  }

  /**
   * Creates a new PollingStation object using the information stored in a JSON object as well as
   * additional information on the election(s) it is valid for.
   *
   * @param obj the JSON oject we are reading the PollingStation object from, retrieved from a call
   *     to the Google Civic Information API
   * @param electionId the election ID of an election the polling station can be used for
   * @param locationType the type of station this is for the election signified by electionId.
   */
  public static PollingStation fromJSONObject(JSONObject obj, String locationType)
      throws JSONException {

    String line1;
    String line2;
    String line3;
    String streetName = "";
    String zipCode;
    List<String> sources = new ArrayList<String>();

    String name;
    String pollingHours;
    String startDate;
    String endDate;

    try {
      line1 = obj.getJSONObject(ADDRESS_JSON_KEYWORD).getString("line1");
      streetName += line1;
    } catch (JSONException e) {
      line1 = "";
    }

    try {
      line2 = obj.getJSONObject(ADDRESS_JSON_KEYWORD).getString("line2");
      if (!streetName.isEmpty()) {
        streetName += " ";
      }
      streetName += line2;
    } catch (JSONException e) {
      line2 = "";
    }

    try {
      line3 = obj.getJSONObject(ADDRESS_JSON_KEYWORD).getString("line3");
      if (!streetName.isEmpty()) {
        streetName += " ";
      }
      streetName += line3;
    } catch (JSONException e) {
      line3 = "";
    }

    try {
      zipCode = obj.getJSONObject(ADDRESS_JSON_KEYWORD).getString("zip");
    } catch (JSONException e) {
      zipCode = "";
    }

    try {
      name = obj.getString(NAME_JSON_KEYWORD);
    } catch (JSONException e) {
      name = "Polling Station";
    }

    try {
      pollingHours = obj.getString(POLLING_HOURS_JSON_KEYWORD);
    } catch (JSONException e) {
      pollingHours = "daily";
    }

    try {
      startDate = obj.getString(START_DATE_JSON_KEYWORD);
    } catch (JSONException e) {
      startDate = "on an unknown start date";
    }

    try {
      endDate = obj.getString(END_DATE_JSON_KEYWORD);
    } catch (JSONException e) {
      endDate = "an unknown end date";
    }

    if (obj.has(SOURCES_JSON_KEYWORD)) {
      for (Object sourceObject : obj.getJSONArray(SOURCES_JSON_KEYWORD)) {
        JSONObject source = (JSONObject) sourceObject;
        if (source.getBoolean("official")) {
          sources.add(source.getString("name"));
        }
      }
    }

    String address =
        streetName
            + ", "
            + obj.getJSONObject(ADDRESS_JSON_KEYWORD).getString("city")
            + ", "
            + obj.getJSONObject(ADDRESS_JSON_KEYWORD).getString("state")
            + " "
            + zipCode;

    ImmutableList<String> sourcesList = ImmutableList.copyOf(sources);

    return PollingStation.builder()
        .setName(name)
        .setAddress(address)
        .setPollingHours(pollingHours)
        .setStartDate(startDate)
        .setEndDate(endDate)
        .setLocationType(locationType)
        .setSources(sourcesList)
        .build();
  }

  /**
   * Creates a PollingStation object from an Entity in Datastore
   *
   * @param entity the Entity in Datastore that represents a PollingStation
   */
  public static PollingStation fromEntity(Entity entity) {
    List<String> sources =
        entity.getProperty(SOURCES_JSON_KEYWORD) == null
            ? new ArrayList<String>()
            : (ArrayList<String>) entity.getProperty(SOURCES_JSON_KEYWORD);

    ImmutableList<String> sourcesList = ImmutableList.copyOf(sources);

    return PollingStation.builder()
        .setName((String) entity.getProperty(NAME_JSON_KEYWORD))
        .setAddress((String) entity.getProperty(ADDRESS_JSON_KEYWORD))
        .setPollingHours((String) entity.getProperty(POLLING_HOURS_JSON_KEYWORD))
        .setStartDate((String) entity.getProperty(START_DATE_JSON_KEYWORD))
        .setEndDate((String) entity.getProperty(END_DATE_JSON_KEYWORD))
        .setLocationType((String) entity.getProperty(LOCATION_TYPE_JSON_KEYWORD))
        .setSources(sourcesList)
        .build();
  }

  /**
   * Creates a new Entity and sets the proper properties.
   *
   * @return an Entity that can be stored in Datastore
   */
  public Entity toEntity() {
    Entity entity = new Entity(ENTITY_KIND);
    entity.setProperty(NAME_JSON_KEYWORD, this.getName());
    entity.setProperty(ADDRESS_JSON_KEYWORD, this.getAddress());
    entity.setProperty(POLLING_HOURS_JSON_KEYWORD, this.getPollingHours());
    entity.setProperty(START_DATE_JSON_KEYWORD, this.getStartDate());
    entity.setProperty(END_DATE_JSON_KEYWORD, this.getEndDate());
    entity.setProperty(LOCATION_TYPE_JSON_KEYWORD, this.getLocationType());
    entity.setProperty(SOURCES_JSON_KEYWORD, new ArrayList<String>(this.getSources()));
    return entity;
  }
}
