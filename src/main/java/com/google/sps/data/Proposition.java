package com.google.sps.data;

/** A proposition open for election on voter ballots */
public final class Proposition {
  private final long id;
  private final String name;
  private final String description;

  public Proposition(PropositionBuilder builder) {
    this.id = builder.getID();
    this.name = builder.getName();
    this.description = builder.getDescription();
  }
}
