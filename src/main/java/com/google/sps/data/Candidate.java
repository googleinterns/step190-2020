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

/** A candidate for a public office position that will appear on voter ballots */
@AutoValue
public abstract class Candidate {
  public abstract String getName();

  public abstract String getPartyAffiliation();

  public abstract String getCampaignSite();

  public abstract String getPlatformDescription();

  public static Builder builder() {
    return new AutoValue_Candidate.Builder();
  }

  // creates a new Candidate object by extracting the properties from "obj"
  public static Candidate fromJSONObject(JSONObject obj) throws JSONException {
    return Candidate.builder()
        .setName(obj.getString("name"))
        .setPartyAffiliation(obj.getString("party"))

        // TODO(caseyprice): get values for campaignSite and platformDescription
        .setCampaignSite("")
        .setPlatformDescription("")
        .build();
  }

  // creates a new Entity and sets the proper properties.
  public Entity toEntity() {
    Entity entity = new Entity("Candidate");
    entity.setProperty("name", this.getName());
    entity.setProperty("partyAffiliation", this.getPartyAffiliation());
    entity.setProperty("campaignSite", this.getCampaignSite());
    entity.setProperty("platformDescription", this.getPlatformDescription());

    return entity;
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setPartyAffiliation(String partyAffiliation);

    public abstract Builder setCampaignSite(String campaignSite);

    public abstract Builder setPlatformDescription(String platformDescription);

    public abstract Candidate build();
  }
}
