package com.google.sps.data;

import java.util.HashSet;

public class ElectionBuilder {
  private long id;
  private String name;
  private String scope;
  private HashSet<Long> positions;
  private String date;
  private HashSet<Long> propositions;

  public ElectionBuilder() {}

  public Election build() {
    return new Election(this);
  }

  public ElectionBuilder setID(long id) {
    this.id = id;
    return this;
  }

  public ElectionBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public ElectionBuilder setScope(String scope) {
    this.scope = scope;
    return this;
  }

  public ElectionBuilder setPositions(HashSet<Long> positions) {
    if (positions == null) {
      positions = new HashSet<Long>();
    }

    this.positions = positions;
    return this;
  }

  public ElectionBuilder setDate(String date) {
    this.date = date;
    return this;
  }

  public ElectionBuilder setPropositions(HashSet<Long> propositions) {
    if (propositions == null) {
      propositions = new HashSet<Long>();
    }

    this.propositions = propositions;
    return this;
  }

  public long getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getScope() {
    return scope;
  }

  public HashSet<Long> getPositions() {
    return positions;
  }

  public String getDate() {
    return date;
  }

  public HashSet<Long> getPropositions() {
    return propositions;
  }
}
