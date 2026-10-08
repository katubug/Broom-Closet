package heykatu.broom_closet.mixin.soulhome;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import heykatu.broom_closet.soulhome.island.SoulIslandChoices;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.UUID;

// Swaps SoulHome's UUID-rolled island for the one the owner picked (see SoulKeyInterceptor).
// Targeted by name since Broom Closet doesn't compile against SoulHome. Owners with no saved pick
// (homes that existed before the picker) keep SoulHome's original island.
@Mixin(targets = "leaf.soulhome.registry.DimensionRegistry")
public abstract class DimensionRegistryMixin {
    @WrapOperation(
            method = "createSoulDimension(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/resources/ResourceKey;Ljava/lang/String;)Lnet/minecraft/server/level/ServerLevel;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;get(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"))
    private static Optional<StructureTemplate> broom_closet$pickedIsland(StructureTemplateManager manager, ResourceLocation original,
                                                                        Operation<Optional<StructureTemplate>> op,
                                                                        @Local(argsOnly = true) MinecraftServer server,
                                                                        @Local(argsOnly = true) String userUUID) {
        ResourceLocation picked = picked(server, userUUID);
        if (picked != null) {
            Optional<StructureTemplate> template = op.call(manager, picked);
            if (template.isPresent()) return template;
        }
        return op.call(manager, original);
    }

    // Turns the island's structure-relative spawn into a world pos once we know where it landed.
    @WrapOperation(
            method = "createSoulDimension(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/resources/ResourceKey;Ljava/lang/String;)Lnet/minecraft/server/level/ServerLevel;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z"))
    private static boolean broom_closet$saveSpawn(StructureTemplate template, ServerLevelAccessor level, BlockPos pos, BlockPos pivot,
                                                  StructurePlaceSettings settings, RandomSource random, int flags,
                                                  Operation<Boolean> op,
                                                  @Local(argsOnly = true) MinecraftServer server,
                                                  @Local(argsOnly = true) String userUUID) {
        boolean placed = op.call(template, level, pos, pivot, settings, random, flags);

        ResourceLocation picked = picked(server, userUUID);
        if (picked == null) return placed;
        UUID owner = UUID.fromString(userUUID);
        SoulIslandChoices choices = SoulIslandChoices.get(server);
        BlockPos local = choices.localSpawn(owner);
        // only if the picked island actually got placed, not SoulHome's fallback
        if (local != null && server.getStructureManager().get(picked).orElse(null) == template) {
            choices.setSpawn(owner, StructureTemplate.calculateRelativePosition(settings, local).offset(pos));
        }
        return placed;
    }

    private static ResourceLocation picked(MinecraftServer server, String userUUID) {
        try {
            return SoulIslandChoices.get(server).get(UUID.fromString(userUUID));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
