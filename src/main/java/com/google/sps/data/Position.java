package com.google.sps.data;

import java.util.HashMap;

/** A public office position that may be open for election on voter ballots */
public final class Position {
  private final long id;
  private final String name;
  private final HashMap<Long, Candidate> candidates;
  private final String description;

  public Position(PositionBuilder builder) {
    this.id = builder.getID();
    this.name = builder.getName();
    this.candidates = builder.getCandidates();
    this.description = builder.getDescription();
  }
}
