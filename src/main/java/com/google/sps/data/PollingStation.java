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

/** A polling station open for voters to vote or drop ballots off at */
@AutoValue
public abstract class PollingStation {
  public abstract String getName();

  public abstract String getAddress();

  public abstract HashMap<String, HashMap<String, String>> getElections();

  public static Builder builder() {
    return new AutoValue_PollingStation.Builder();
  }

  // creates a new PollingStation object by extracting the properties from "obj"
  public static PollingStation fromJSONObject(JSONObject obj) throws JSONException {
    return PollingStation.builder()
        .setName(obj.getJSONObject("address").getString("locationName"))
        .setAddress("")
        .setElections(new HashMap<String, HashMap<String, String>>())
        .build();
  }

  // TODO(anooshree): add a fromEntity constructor.

  // creates a new Entity and sets the proper properties.
  public Entity toEntity() {
    Entity entity = new Entity("PollingStation");
    entity.setProperty("name", this.getName());
    entity.setProperty("address", this.getAddress());
    entity.setProperty("elections", new HashMap<String, HashMap<String, String>>());
    return entity;
  }

  // TODO(anooshree): write method that goes through polling stations for a 
  // given election and updates polling stations, return a list
  //
  // Should update polling stations that already exist in Datastore and create
  // new ones for those that do not.
  //
  // public static HashSet<PollingStation> getPollingStationsForElection(String electionID)

  // TODO(anooshree): add a method that either adds a set or a single PollingStation to Datastore.


  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setAddress(String scope);

    public abstract Builder setElections(HashMap<String, HashMap<String, String>> elections);

    public abstract PollingStation build();
  }
}
