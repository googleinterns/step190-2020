package com.google.sps.data;

import java.util.HashMap;

public class PositionBuilder {
  private long id;
  private String name;
  private HashMap<Long, Candidate> candidates;
  private String description;

  public PositionBuilder() {}

  public Position build() {
    return new Position(this);
  }

  public PositionBuilder setID(long id) {
    this.id = id;
    return this;
  }

  public PositionBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public PositionBuilder setCandidates(HashMap<Long, Candidate> candidates) {
    if (candidates == null) {
      candidates = new HashMap<Long, Candidate>();
    }

    this.candidates = candidates;
    return this;
  }

  public PositionBuilder setDescription(String Description) {
    this.description = description;
    return this;
  }

  public long getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  public HashMap<Long, Candidate> getCandidates() {
    return candidates;
  }

  public String getDescription() {
    return description;
  }
}
