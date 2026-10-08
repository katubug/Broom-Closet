package heykatu.broom_closet.mixin.soulhome;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import heykatu.broom_closet.soulhome.SoulHomeArrival;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// SoulHome always drops you in at 0.5/72/0.5. This moves arrivals to the island's spawn or the ground.
@Mixin(targets = "leaf.soulhome.utils.DimensionHelper")
public abstract class DimensionHelperMixin {
    @WrapOperation(
            method = "FlipDimension",
            at = @At(value = "INVOKE", target = "Lleaf/soulhome/utils/TeleportHelper;teleportEntity(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;DDDFF)V"))
    private static void broom_closet$arrival(Entity entity, ServerLevel level, double x, double y, double z, float yRot, float xRot,
                                             Operation<Void> op) {
        Vec3 to = SoulHomeArrival.adjust(level, x, y, z);
        op.call(entity, level, to.x, to.y, to.z, yRot, xRot);
    }
}
