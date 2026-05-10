package dev.aguilar20dev.cistierstagger.mixin;

import dev.aguilar20dev.cistierstagger.CisTiersTagger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerEntityMixin {
  @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
  private void cistiers$prependTier(CallbackInfoReturnable<Component> cir) {
    Player player = (Player) (Object) this;
    String username = player.getGameProfile().name();
    cir.setReturnValue(CisTiersTagger.tags().appendTag(username, cir.getReturnValue()));
  }
}
