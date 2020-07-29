package com.google.sps.data;

public class PropositionBuilder {
  private long id;
  private String name;
  private String description;

  public PropositionBuilder() {}

  public Proposition build() {
    return new Proposition(this);
  }

  public PropositionBuilder setID(long id) {
    this.id = id;
    return this;
  }

  public PropositionBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public PropositionBuilder setDescription(String Description) {
    this.description = description;
    return this;
  }

  public long getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
