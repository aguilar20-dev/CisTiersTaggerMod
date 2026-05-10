package dev.aguilar20dev.cistierstagger;

import dev.aguilar20dev.cistierstagger.api.CisTiersApiClient;
import dev.aguilar20dev.cistierstagger.model.TierProfile;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class CisTiersTagService {
  private static final CisTiersApiClient API_CLIENT = new CisTiersApiClient();
  private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

  public Optional<Component> getTag(String username) {
    if (username == null || username.isBlank()) {
      return Optional.empty();
    }

    String key = username.toLowerCase(Locale.ROOT);
    CacheEntry entry = CACHE.computeIfAbsent(key, ignored -> {
      CacheEntry created = new CacheEntry();
      load(username, created);
      return created;
    });

    return entry.tag;
  }

  public Component appendTag(String username, Component original) {
    Optional<Component> tag = getTag(username);
    if (tag.isEmpty()) {
      return original;
    }

    return tag.get().copy()
        .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
        .append(original);
  }

  private void load(String username, CacheEntry entry) {
    API_CLIENT.fetchProfile(username)
        .thenApply(CisTiersTagService::createBestTierTag)
        .thenAccept(tag -> entry.tag = tag)
        .exceptionally(error -> {
          entry.tag = Optional.empty();
          return null;
        });
  }

  private static Optional<Component> createBestTierTag(TierProfile profile) {
    return profile.tiers().stream()
        .min(Comparator.comparingInt(CisTiersTagService::tierPriority))
        .map(tier -> Component.literal(kitIcon(tier.kit()) + " ")
            .withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal(tier.tier().toUpperCase(Locale.ROOT))
                .withStyle(tierColor(tier.tier()), ChatFormatting.BOLD)));
  }

  private static int tierPriority(TierProfile.TierEntry tier) {
    String value = tier.tier().toLowerCase(Locale.ROOT);
    int retiredPenalty = value.startsWith("r") ? 100 : 0;
    value = value.replaceFirst("^r", "");

    if (value.length() < 3) {
      return Integer.MAX_VALUE;
    }

    int position = value.startsWith("ht") ? 0 : 1;
    int tierNumber;

    try {
      tierNumber = Integer.parseInt(value.substring(2));
    } catch (NumberFormatException ignored) {
      return Integer.MAX_VALUE;
    }

    return retiredPenalty + (tierNumber - 1) * 2 + position;
  }

  private static String kitIcon(String kit) {
    return switch (kit.toLowerCase(Locale.ROOT)) {
      case "sword" -> "🗡";
      case "netherite" -> "♦";
      case "dpot" -> "🧪";
      case "op" -> "★";
      case "mace" -> "🪓";
      case "smp" -> "🛡";
      case "uhc" -> "❤";
      case "vanilla" -> "●";
      default -> "•";
    };
  }

  private static ChatFormatting tierColor(String tier) {
    return switch (tier.toLowerCase(Locale.ROOT).replaceFirst("^r", "")) {
      case "ht1", "lt1" -> ChatFormatting.GOLD;
      case "ht2", "lt2" -> ChatFormatting.LIGHT_PURPLE;
      case "ht3", "lt3" -> ChatFormatting.AQUA;
      case "ht4", "lt4" -> ChatFormatting.GREEN;
      case "ht5", "lt5" -> ChatFormatting.GRAY;
      default -> ChatFormatting.WHITE;
    };
  }

  private static class CacheEntry {
    private volatile Optional<Component> tag = Optional.empty();
  }
}
