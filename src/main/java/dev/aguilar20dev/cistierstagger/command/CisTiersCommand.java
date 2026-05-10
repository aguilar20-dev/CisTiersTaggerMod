package dev.aguilar20dev.cistierstagger.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.aguilar20dev.cistierstagger.api.CisTiersApiClient;
import dev.aguilar20dev.cistierstagger.model.TierProfile;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public class CisTiersCommand {
  private static final CisTiersApiClient API_CLIENT = new CisTiersApiClient();

  public static void register() {
    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
      dispatcher.register(ClientCommands.literal("cistiers")
          .executes(CisTiersCommand::showHelp)
          .then(ClientCommands.argument("username", StringArgumentType.word())
              .suggests(CisTiersCommand::suggestOnlinePlayers)
              .executes(CisTiersCommand::showProfile)));
    });
  }

  private static int showHelp(CommandContext<FabricClientCommandSource> context) {
    context.getSource().sendFeedback(prefix().append(Component.literal(" Доступные команды:")
        .withStyle(ChatFormatting.GRAY)));
    context.getSource().sendFeedback(Component.literal("/cistiers <nickname>")
        .withStyle(ChatFormatting.YELLOW)
        .append(Component.literal(" - показать тиры игрока").withStyle(ChatFormatting.GRAY)));
    return 1;
  }

  private static CompletableFuture<Suggestions> suggestOnlinePlayers(
      CommandContext<FabricClientCommandSource> context,
      SuggestionsBuilder builder) {
      Minecraft client = Minecraft.getInstance();

      if (client.getConnection() == null) {
          return builder.buildFuture();
      }

      for (PlayerInfo player : client.getConnection().getOnlinePlayers()) {
          builder.suggest(player.getProfile().name());
      }

      return builder.buildFuture();
  }

  private static int showProfile(CommandContext<FabricClientCommandSource> context) {
    String username = StringArgumentType.getString(context, "username");
    FabricClientCommandSource source = context.getSource();
    source.sendFeedback(prefix().append(Component.literal(" Загружаю профиль игрока ")
        .withStyle(ChatFormatting.GRAY))
        .append(Component.literal(username).withStyle(ChatFormatting.AQUA)));

    API_CLIENT.fetchProfile(username)
        .thenAccept(profile -> Minecraft.getInstance().execute(() -> sendProfile(source, profile)))
        .exceptionally(error -> {
            Minecraft.getInstance().execute(() -> sendError(source, username, error));
          return null;
        });

    return 1;
  }

  private static void sendProfile(FabricClientCommandSource source, TierProfile profile) {
    source.sendFeedback(Component.literal(""));
    source.sendFeedback(prefix()
        .append(Component.literal(" " + profile.nickname()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
        .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
        .append(Component.literal(profile.restricted() ? "RESTRICTED" : "NOT RESTRICTED")
            .withStyle(profile.restricted() ? ChatFormatting.RED : ChatFormatting.GREEN)));

      MutableComponent stats = Component.literal("  Место: ").withStyle(ChatFormatting.GRAY)
        .append(Component.literal(profile.rankPosition() > 0 ? "#" + profile.rankPosition() : "N/A")
            .withStyle(ChatFormatting.GOLD))
        .append(Component.literal("  Очки: ").withStyle(ChatFormatting.GRAY))
        .append(Component.literal(String.valueOf(profile.totalPoints())).withStyle(ChatFormatting.YELLOW));
    source.sendFeedback(stats);

    if (profile.tiers().isEmpty()) {
      source.sendFeedback(Component.literal("  У игрока нет тиров.").withStyle(ChatFormatting.DARK_GRAY));
      return;
    }

    source.sendFeedback(Component.literal("  Текущие тиры:").withStyle(ChatFormatting.GRAY));
    for (TierProfile.TierEntry tier : profile.tiers()) {
      source.sendFeedback(Component.literal("  " + kitIcon(tier.kit()) + " ")
          .withStyle(ChatFormatting.DARK_GRAY)
          .append(Component.literal(formatKit(tier.kit())).withStyle(ChatFormatting.WHITE))
          .append(Component.literal("  "))
          .append(Component.literal(tier.tier().toUpperCase()).withStyle(tierColor(tier.tier()), ChatFormatting.BOLD))
          .append(Component.literal("  "))
          .append(Component.literal(tier.points() + " очк.").withStyle(ChatFormatting.GRAY)));
    }
  }

  private static void sendError(FabricClientCommandSource source, String username, Throwable error) {
    Throwable cause = unwrap(error);
    if (cause instanceof CisTiersApiClient.PlayerNotFoundException) {
      source.sendFeedback(prefix().append(Component.literal(" У игрока ")
          .withStyle(ChatFormatting.GRAY))
          .append(Component.literal(username).withStyle(ChatFormatting.YELLOW))
          .append(Component.literal(" нет тиров.").withStyle(ChatFormatting.GRAY)));
      return;
    }

    source.sendFeedback(prefix().append(Component.literal(" Не удалось загрузить профиль: ")
        .withStyle(ChatFormatting.RED))
        .append(Component.literal(cause.getMessage()).withStyle(ChatFormatting.GRAY)));
  }

  private static Throwable unwrap(Throwable error) {
    if (error.getCause() != null) {
      return error.getCause();
    }

    return error;
  }

  private static MutableComponent prefix() {
    return Component.literal("[CisTiers]").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
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

  private static ChatFormatting tierColor(String tier) {
    return switch (tier.toLowerCase()) {
      case "ht1", "lt1" -> ChatFormatting.GOLD;
      case "ht2", "lt2" -> ChatFormatting.LIGHT_PURPLE;
      case "ht3", "lt3" -> ChatFormatting.AQUA;
      case "ht4", "lt4" -> ChatFormatting.GREEN;
      case "ht5", "lt5" -> ChatFormatting.GRAY;
      default -> ChatFormatting.WHITE;
    };
  }
}
