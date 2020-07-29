package com.google.sps.data;

import java.util.HashMap;

/** A polling station open for voters to vote or drop ballots off at */
public final class PollingStation {
  private final long id;
  private final String name;
  private final String address;
  private final HashMap<Long, Election> elections;

  public PollingStation(PollingStationBuilder builder) {
    this.id = builder.getID();
    this.name = builder.getName();
    this.address = builder.getAddress();
    this.elections = builder.getElections();
  }
}
