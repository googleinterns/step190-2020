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

import java.util.HashSet;

/** A state or national election that will appear on voter ballots */
public final class Election {

  private final long id;
  private final String name;
  private final String scope;
  private final HashSet<Long> positions;
  private final String date;
  private final HashSet<Long> propositions;

  public Election(long id, String name, String scope, String date) {
    this.id = id;
    this.name = name;
    this.scope = scope;
    this.date = date;
    this.positions = new HashSet<Long>();
    this.propositions = new HashSet<Long>();
  }

  public void addPosition(Long positionId) {
    this.positions.add(positionId);
  }

  public void addProposition(Long propositionId) {
    this.propositions.add(propositionId);
  }
}
