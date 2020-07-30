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

import com.google.auto.value.AutoValue;
import java.util.HashSet;

/** A state or national election that will appear on voter ballots */
@AutoValue
public abstract class Election {
  public abstract long getId();

  public abstract String getName();

  public abstract String getScope();

  public abstract HashSet<Long> getPositions();

  public abstract String getDate();

  public abstract HashSet<Long> getPropositions();

  public static Builder builder() {
    return new AutoValue_Election.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(long id);

    public abstract Builder setName(String name);

    public abstract Builder setScope(String scope);

    public abstract Builder setPositions(HashSet<Long> positions);

    public abstract Builder setDate(String date);

    public abstract Builder setPropositions(HashSet<Long> propositions);

    public abstract Election build();
  }
}
