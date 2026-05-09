package dev.aguilar20dev.cistierstagger.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.aguilar20dev.cistierstagger.model.TierProfile;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CisTiersApiClient {
  private static final String BASE_URL = "https://cistiers.com/api/profile/";
  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .build();

  public CompletableFuture<TierProfile> fetchProfile(String nickname) {
    String encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8);
    HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + encodedNickname))
        .timeout(Duration.ofSeconds(10))
        .header("Accept", "application/json")
        .GET()
        .build();

    return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(response -> parseResponse(response, nickname));
  }

  private TierProfile parseResponse(HttpResponse<String> response, String requestedNickname) {
    if (response.statusCode() != 200) {
      throw new IllegalStateException("CISTiers returned HTTP " + response.statusCode());
    }

    JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
    if (root.has("error")) {
      throw new PlayerNotFoundException(requestedNickname);
    }

    String nickname = getString(root, "nickname", requestedNickname);
    boolean restricted = getBoolean(root, "is_restricted", false);
    int rankPosition = getInt(root, "rank_position", 0);
    int totalPoints = 0;
    List<TierProfile.TierEntry> tiers = new ArrayList<>();

    JsonObject tierStats = getObject(root, "tier_stats");
    if (tierStats != null) {
      totalPoints = getInt(tierStats, "total_points", 0);
      JsonArray currentTiers = getArray(tierStats, "current_tiers");

      if (currentTiers != null) {
        for (JsonElement element : currentTiers) {
          if (!element.isJsonObject()) {
            continue;
          }

          JsonObject tier = element.getAsJsonObject();
          tiers.add(new TierProfile.TierEntry(
              getString(tier, "kit", "unknown"),
              getString(tier, "tier", "unknown"),
              getInt(tier, "points", 0)));
        }
      }
    }

    return new TierProfile(nickname, restricted, totalPoints, rankPosition, tiers);
  }

  private static JsonObject getObject(JsonObject object, String key) {
    JsonElement element = object.get(key);
    return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
  }

  private static JsonArray getArray(JsonObject object, String key) {
    JsonElement element = object.get(key);
    return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
  }

  private static String getString(JsonObject object, String key, String fallback) {
    JsonElement element = object.get(key);
    return element != null && !element.isJsonNull() ? element.getAsString() : fallback;
  }

  private static boolean getBoolean(JsonObject object, String key, boolean fallback) {
    JsonElement element = object.get(key);
    return element != null && !element.isJsonNull() ? element.getAsBoolean() : fallback;
  }

  private static int getInt(JsonObject object, String key, int fallback) {
    JsonElement element = object.get(key);
    return element != null && !element.isJsonNull() ? element.getAsInt() : fallback;
  }

  public static class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String nickname) {
      super("Player not found: " + nickname);
    }
  }
}
