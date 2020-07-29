package com.google.sps.data;

public final class Candidate {
  private final long id;
  private final String name;
  private final String partyAffiliation;
  private final String campaignSite;
  private final String platformDescription;

  public Candidate(CandidateBuilder builder) {
    this.id = builder.getID();
    this.name = builder.getName();
    this.partyAffiliation = builder.getPartyAffiliation();
    this.campaignSite = builder.getCampaignSite();
    this.platformDescription = builder.getPlatformDescription();
  }
}
