package dev.aguilar20dev.cistierstagger;

import dev.aguilar20dev.cistierstagger.api.CisTiersApiClient;
import dev.aguilar20dev.cistierstagger.model.TierProfile;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CisTiersTagService {
  private static final CisTiersApiClient API_CLIENT = new CisTiersApiClient();
  private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

  public Optional<Text> getTag(String username) {
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

  public Text appendTag(String username, Text original) {
    Optional<Text> tag = getTag(username);
    if (tag.isEmpty()) {
      return original;
    }

    return tag.get().copy()
        .append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
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

  private static Optional<Text> createBestTierTag(TierProfile profile) {
    return profile.tiers().stream()
        .min(Comparator.comparingInt(CisTiersTagService::tierPriority))
        .map(tier -> Text.literal(kitIcon(tier.kit()) + " ")
            .formatted(Formatting.DARK_GRAY)
            .append(Text.literal(tier.tier().toUpperCase(Locale.ROOT))
                .formatted(tierColor(tier.tier()), Formatting.BOLD)));
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

  private static Formatting tierColor(String tier) {
    return switch (tier.toLowerCase(Locale.ROOT).replaceFirst("^r", "")) {
      case "ht1", "lt1" -> Formatting.GOLD;
      case "ht2", "lt2" -> Formatting.LIGHT_PURPLE;
      case "ht3", "lt3" -> Formatting.AQUA;
      case "ht4", "lt4" -> Formatting.GREEN;
      case "ht5", "lt5" -> Formatting.GRAY;
      default -> Formatting.WHITE;
    };
  }

  private static class CacheEntry {
    private volatile Optional<Text> tag = Optional.empty();
  }
}
