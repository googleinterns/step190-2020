package com.google.sps.data;

public class CandidateBuilder {
  private long id;
  private String name;
  private String partyAffiliation;
  private String campaignSite;
  private String platformDescription;

  public CandidateBuilder() {}

  public Candidate build() {
    return new Candidate(this);
  }

  public CandidateBuilder setID(long id) {
    this.id = id;
    return this;
  }

  public CandidateBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public CandidateBuilder setPartyAffiliation(String partyAffiliation) {
    this.partyAffiliation = partyAffiliation;
    return this;
  }

  public CandidateBuilder setCampaignSite(String campaignSite) {
    this.campaignSite = campaignSite;
    return this;
  }

  public CandidateBuilder setPlatformDescription(String platformDescription) {
    this.platformDescription = platformDescription;
    return this;
  }

  public long getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getPartyAffiliation() {
    return partyAffiliation;
  }

  public String getCampaignSite() {
    return campaignSite;
  }

  public String getPlatformDescription() {
    return platformDescription;
  }
}
