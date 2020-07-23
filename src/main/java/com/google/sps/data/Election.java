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

/** An item on a todo list. */
public final class Election {

  private final String name;
  private final String scope;
  private final HashSet<long> positions;
  private final String date;
  private final HashSet<long> propositions;

  public Election(String name, String scope, String date) {
    this.name = name;
    this.scope = scope;
    this.date = date;
    this.positions = new HashSet<long> ();
    this.propositions = new HashSet<long> ();
  }

  public void addPosition(int positionID) {
      this.positions.add(positionID);
  }

  public void addProposition(int propID) {
      this.propositions.add(propID);
  }
}