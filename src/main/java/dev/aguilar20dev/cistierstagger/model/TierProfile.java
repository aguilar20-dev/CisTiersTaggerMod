package dev.aguilar20dev.cistierstagger.model;

import java.util.List;

public class TierProfile {
  private final String nickname;
  private final boolean restricted;
  private final int totalPoints;
  private final int rankPosition;
  private final List<TierEntry> tiers;

  public TierProfile(
      String nickname,
      boolean restricted,
      int totalPoints,
      int rankPosition,
      List<TierEntry> tiers) {
    this.nickname = nickname;
    this.restricted = restricted;
    this.totalPoints = totalPoints;
    this.rankPosition = rankPosition;
    this.tiers = List.copyOf(tiers);
  }

  public String nickname() {
    return nickname;
  }

  public boolean restricted() {
    return restricted;
  }

  public int totalPoints() {
    return totalPoints;
  }

  public int rankPosition() {
    return rankPosition;
  }

  public List<TierEntry> tiers() {
    return tiers;
  }

  public static class TierEntry {
    private final String kit;
    private final String tier;
    private final int points;

    public TierEntry(String kit, String tier, int points) {
      this.kit = kit;
      this.tier = tier;
      this.points = points;
    }

    public String kit() {
      return kit;
    }

    public String tier() {
      return tier;
    }

    public int points() {
      return points;
    }
  }
}
