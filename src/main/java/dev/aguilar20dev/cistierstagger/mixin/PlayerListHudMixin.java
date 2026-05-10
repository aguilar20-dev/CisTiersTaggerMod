package dev.aguilar20dev.cistierstagger.mixin;

import dev.aguilar20dev.cistierstagger.CisTiersTagger;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin {
  @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
  private void cistiers$prependTier(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
    String username = info.getProfile().name();
    cir.setReturnValue(CisTiersTagger.tags().appendTag(username, cir.getReturnValue()));
  }
}
