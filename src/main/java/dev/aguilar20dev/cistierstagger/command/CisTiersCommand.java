package dev.aguilar20dev.cistierstagger.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.aguilar20dev.cistierstagger.api.CisTiersApiClient;
import dev.aguilar20dev.cistierstagger.model.TierProfile;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CisTiersCommand {
  private static final CisTiersApiClient API_CLIENT = new CisTiersApiClient();

  public static void register() {
    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
      dispatcher.register(ClientCommandManager.literal("cistiers")
          .executes(CisTiersCommand::showHelp)
          .then(ClientCommandManager.argument("username", StringArgumentType.word())
              .suggests(CisTiersCommand::suggestOnlinePlayers)
              .executes(CisTiersCommand::showProfile)));
    });
  }

  private static int showHelp(CommandContext<FabricClientCommandSource> context) {
    context.getSource().sendFeedback(prefix().append(Text.literal(" Доступные команды:")
        .formatted(Formatting.GRAY)));
    context.getSource().sendFeedback(Text.literal("/cistiers <nickname>")
        .formatted(Formatting.YELLOW)
        .append(Text.literal(" - показать тиры игрока").formatted(Formatting.GRAY)));
    return 1;
  }

  private static CompletableFuture<Suggestions> suggestOnlinePlayers(
      CommandContext<FabricClientCommandSource> context,
      SuggestionsBuilder builder) {
    MinecraftClient client = MinecraftClient.getInstance();

    if (client.getNetworkHandler() == null) {
      return builder.buildFuture();
    }

    for (PlayerListEntry player : client.getNetworkHandler().getPlayerList()) {
      builder.suggest(player.getProfile().name());
    }

    return builder.buildFuture();
  }

  private static int showProfile(CommandContext<FabricClientCommandSource> context) {
    String username = StringArgumentType.getString(context, "username");
    FabricClientCommandSource source = context.getSource();
    source.sendFeedback(prefix().append(Text.literal(" Загружаю профиль игрока ")
        .formatted(Formatting.GRAY))
        .append(Text.literal(username).formatted(Formatting.AQUA)));

    API_CLIENT.fetchProfile(username)
        .thenAccept(profile -> MinecraftClient.getInstance().execute(() -> sendProfile(source, profile)))
        .exceptionally(error -> {
          MinecraftClient.getInstance().execute(() -> sendError(source, username, error));
          return null;
        });

    return 1;
  }

  private static void sendProfile(FabricClientCommandSource source, TierProfile profile) {
    source.sendFeedback(Text.literal(""));
    source.sendFeedback(prefix()
        .append(Text.literal(" " + profile.nickname()).formatted(Formatting.AQUA, Formatting.BOLD))
        .append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
        .append(Text.literal(profile.restricted() ? "RESTRICTED" : "NOT RESTRICTED")
            .formatted(profile.restricted() ? Formatting.RED : Formatting.GREEN)));

    MutableText stats = Text.literal("  Место: ").formatted(Formatting.GRAY)
        .append(Text.literal(profile.rankPosition() > 0 ? "#" + profile.rankPosition() : "N/A")
            .formatted(Formatting.GOLD))
        .append(Text.literal("  Очки: ").formatted(Formatting.GRAY))
        .append(Text.literal(String.valueOf(profile.totalPoints())).formatted(Formatting.YELLOW));
    source.sendFeedback(stats);

    if (profile.tiers().isEmpty()) {
      source.sendFeedback(Text.literal("  У игрока нет тиров.").formatted(Formatting.DARK_GRAY));
      return;
    }

    source.sendFeedback(Text.literal("  Текущие тиры:").formatted(Formatting.GRAY));
    for (TierProfile.TierEntry tier : profile.tiers()) {
      source.sendFeedback(Text.literal("  " + kitIcon(tier.kit()) + " ")
          .formatted(Formatting.DARK_GRAY)
          .append(Text.literal(formatKit(tier.kit())).formatted(Formatting.WHITE))
          .append(Text.literal("  "))
          .append(Text.literal(tier.tier().toUpperCase()).formatted(tierColor(tier.tier()), Formatting.BOLD))
          .append(Text.literal("  "))
          .append(Text.literal(tier.points() + " очк.").formatted(Formatting.GRAY)));
    }
  }

  private static void sendError(FabricClientCommandSource source, String username, Throwable error) {
    Throwable cause = unwrap(error);
    if (cause instanceof CisTiersApiClient.PlayerNotFoundException) {
      source.sendFeedback(prefix().append(Text.literal(" У игрока ")
          .formatted(Formatting.GRAY))
          .append(Text.literal(username).formatted(Formatting.YELLOW))
          .append(Text.literal(" нет тиров.").formatted(Formatting.GRAY)));
      return;
    }

    source.sendFeedback(prefix().append(Text.literal(" Не удалось загрузить профиль: ")
        .formatted(Formatting.RED))
        .append(Text.literal(cause.getMessage()).formatted(Formatting.GRAY)));
  }

  private static Throwable unwrap(Throwable error) {
    if (error.getCause() != null) {
      return error.getCause();
    }

    return error;
  }

  private static MutableText prefix() {
    return Text.literal("[CisTiers]").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD);
  }

  private static String formatKit(String kit) {
    if (kit == null || kit.isBlank()) {
      return "Unknown";
    }

    return kit.substring(0, 1).toUpperCase() + kit.substring(1);
  }

  private static String kitIcon(String kit) {
    return switch (kit.toLowerCase()) {
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
    return switch (tier.toLowerCase()) {
      case "ht1", "lt1" -> Formatting.GOLD;
      case "ht2", "lt2" -> Formatting.LIGHT_PURPLE;
      case "ht3", "lt3" -> Formatting.AQUA;
      case "ht4", "lt4" -> Formatting.GREEN;
      case "ht5", "lt5" -> Formatting.GRAY;
      default -> Formatting.WHITE;
    };
  }
}
