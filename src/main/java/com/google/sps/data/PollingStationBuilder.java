package com.google.sps.data;

import java.util.HashMap;

public class PollingStationBuilder {
  private long id;
  private String name;
  private String address;
  private HashMap<Long, Election> elections;

  public PollingStationBuilder() {}

  public PollingStation build() {
    return new PollingStation(this);
  }

  public PollingStationBuilder setID(long id) {
    this.id = id;
    return this;
  }

  public PollingStationBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public PollingStationBuilder setAddress(String address) {
    this.address = address;
    return this;
  }

  public PollingStationBuilder setElections(HashMap<Long, Election> elections) {
    if (elections == null) {
      elections = new HashMap<Long, Election>();
    }

    this.elections = elections;
    return this;
  }

  public long getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public HashMap<Long, Election> getElections() {
    return elections;
  }
}
