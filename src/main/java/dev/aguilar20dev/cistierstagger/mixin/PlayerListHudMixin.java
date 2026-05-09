package dev.aguilar20dev.cistierstagger.mixin;

import dev.aguilar20dev.cistierstagger.CisTiersTagger;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
  @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
  private void cistiers$prependTier(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
    String username = entry.getProfile().name();
    cir.setReturnValue(CisTiersTagger.tags().appendTag(username, cir.getReturnValue()));
  }
}
