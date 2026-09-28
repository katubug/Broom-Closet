package heykatu.broom_closet.mixin.ftbchunks;

import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig;
import heykatu.broom_closet.ClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.GlobalPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FTBChunksClient.class)
public abstract class FTBChunksClientMixin {
    // renderHud() does two unrelated things in one method, stop them
    @Inject(method = "renderHud", at = @At("HEAD"))
    private void broomcloset$forceMinimapDisabled(GuiGraphics graphics, DeltaTracker tickDelta, CallbackInfo ci) {
        if (ClientConfig.ftbChunksMapDisabled()) {
            FTBChunksClientConfig.MINIMAP_ENABLED.set(false);
        }
    }

    // handlePlayerDeath is the single client-side entry point for all death waypoint creation. Cancelling it here bypasses FTB Chunks' own DEATH_WAYPOINTS becuase FTB IS A BUTTHEAD WHO WON'T RESPECT THEIR OWN CONFIGS
    @Inject(method = "handlePlayerDeath", at = @At("HEAD"), cancellable = true)
    private void broomcloset$suppressDeathWaypoints(GlobalPos pos, int number, CallbackInfo ci) {
        if (ClientConfig.deathWaypointsDisabled()) {
            ci.cancel();
        }
    }
}
