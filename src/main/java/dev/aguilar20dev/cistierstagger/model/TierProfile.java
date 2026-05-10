package dev.aguilar20dev.cistierstagger.model;

import java.util.List;

public record TierProfile(String nickname, boolean restricted, int totalPoints, int rankPosition,
                          List<TierEntry> tiers) {
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

    public record TierEntry(String kit, String tier, int points) {
    }
}
