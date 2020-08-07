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
import org.json.JSONException;
import org.json.JSONObject;

/** A polling station open for voters to vote or drop ballots off at */
@AutoValue
public abstract class PollingStation {
  public abstract String getName();

  public abstract String getAddress();

  public abstract String getPollingHours();

  public abstract String getStartDate();

  public abstract String getEndDate();

  public abstract String getLocationType();

  public static Builder builder() {
    return new AutoValue_PollingStation.Builder();
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
    String address =
        obj.getJSONObject("address").getString("line1")
            + " "
            + obj.getJSONObject("address").getString("line2")
            + " "
            + obj.getJSONObject("address").getString("line3")
            + " "
            + obj.getJSONObject("address").getString("city")
            + " "
            + obj.getJSONObject("address").getString("state")
            + " "
            + obj.getJSONObject("address").getString("zip");

    return PollingStation.builder()
        .setName(obj.getString("name"))
        .setAddress(address)
        .setPollingHours(obj.getString("pollingHours"))
        .setStartDate(obj.getString("startDate"))
        .setEndDate(obj.getString("endDate"))
        .setLocationType(locationType)
        .build();
  }

  /**
   * Creates a PollingStation object from an Entity in Datastore
   *
   * @param entity the Entity in Datastore that represents a PollingStation
   */
  public static PollingStation fromEntity(Entity entity) {
    return PollingStation.builder()
        .setName((String) entity.getProperty("name"))
        .setAddress((String) entity.getProperty("address"))
        .setPollingHours((String) entity.getProperty("pollingHours"))
        .setStartDate((String) entity.getProperty("startDate"))
        .setEndDate((String) entity.getProperty("endDate"))
        .setLocationType((String) entity.getProperty("locationType"))
        .build();
  }

  /**
   * Creates a new Entity and sets the proper properties.
   *
   * @return an Entity that can be stored in Datastore
   */
  public Entity toEntity() {
    Entity entity = new Entity("PollingStation");
    entity.setProperty("name", this.getName());
    entity.setProperty("address", this.getAddress());
    entity.setProperty("pollingHours", this.getPollingHours());
    entity.setProperty("startDate", this.getStartDate());
    entity.setProperty("endDate", this.getEndDate());
    entity.setProperty("locationType", this.getLocationType());
    return entity;
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setAddress(String scope);

    public abstract Builder setPollingHours(String hours);

    public abstract Builder setStartDate(String start);

    public abstract Builder setEndDate(String end);

    public abstract Builder setLocationType(String type);

    public abstract PollingStation build();
  }
}
