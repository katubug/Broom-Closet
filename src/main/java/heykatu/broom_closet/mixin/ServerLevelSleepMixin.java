package heykatu.broom_closet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import heykatu.broom_closet.Config;
import heykatu.broom_closet.soulhome.SoulHomeCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Switches off vanilla's own night-skip check inside soul home dimensions
@Mixin(ServerLevel.class)
public class ServerLevelSleepMixin {
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"))
    private boolean broom_closet$ignoreSoulHomeSleepStatus(SleepStatus sleepStatus, int percentage, Operation<Boolean> original) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (Config.soulhomeSleep && SoulHomeCompat.isSoulHome(level)) {
            return false;
        }
        return original.call(sleepStatus, percentage);
    }
}
