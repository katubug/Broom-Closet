package heykatu.broom_closet.mixin.ftbchunks;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import heykatu.broom_closet.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LargeMapScreen.class)
public abstract class LargeMapScreenMixin {
    // kill ftb chunks map at openMap()
    @Inject(method = "openMap", at = @At("HEAD"), cancellable = true)
    private static void broomcloset$preventOpen(CallbackInfoReturnable<Boolean> cir) {
        if (ClientConfig.ftbChunksMapDisabled()) {
            cir.setReturnValue(false);
        }
    }
}
