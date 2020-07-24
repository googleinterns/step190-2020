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

/** A user who has provided us with location information as they navigate the site */
public class User {

  private String state;
  private String address;
  private HashSet<Election> elections;

  public User(String state) {
    this.state = state;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress() {
    return this.address;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getState() {
    return this.state;
  }

  public void addElection(Election election) {
    this.elections.add(election);
  }

  public HashSet<Election> getElections() {
    return this.elections;
  }
}
