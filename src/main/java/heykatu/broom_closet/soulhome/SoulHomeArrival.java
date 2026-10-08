package heykatu.broom_closet.soulhome;

import heykatu.broom_closet.soulhome.island.SoulIslandChoices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

// Where people actually show up in a soul home. See DimensionHelperMixin.
public final class SoulHomeArrival {
    // SoulHome's hardcoded drop-in point
    private static final Vec3 SOULHOME_ARRIVAL = new Vec3(0.5, 72, 0.5);

    private SoulHomeArrival() {}

    // x/y/z already include each entity's offset from the player (pets etc), so keep that offset
    public static Vec3 adjust(ServerLevel level, double x, double y, double z) {
        if (!SoulHomeCompat.isSoulHome(level)) return new Vec3(x, y, z); // heading back out, leave it

        Vec3 offset = new Vec3(x, y, z).subtract(SOULHOME_ARRIVAL);
        BlockPos spawn = savedSpawn(level);
        if (spawn != null) {
            double sx = spawn.getX() + 0.5 + offset.x, sy = spawn.getY() + offset.y, sz = spawn.getZ() + 0.5 + offset.z;
            if (isFree(level, BlockPos.containing(sx, sy, sz))) return new Vec3(sx, sy, sz);
            // someone built over it, find the ground there instead
            return new Vec3(sx, land(level, sx, y, sz), sz);
        }
        return new Vec3(x, land(level, x, y, z), z);
    }

    private static BlockPos savedSpawn(ServerLevel level) {
        // soul home dims are soulhome:<owner uuid>
        try {
            UUID owner = UUID.fromString(level.dimension().location().getPath());
            return SoulIslandChoices.get(level.getServer()).spawn(owner);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean isFree(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
    }

    // Wherever you'd have landed falling from startY, minus the falling. Void below = no change.
    private static double land(ServerLevel level, double x, double startY, double z) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, startY, z).mutable();
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return startY;
        while (pos.getY() > level.getMinBuildHeight()) {
            pos.move(Direction.DOWN);
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty()) return pos.getY() + shape.max(Direction.Axis.Y);
            if (!state.getFluidState().isEmpty()) return pos.getY() + 1; // float on top
        }
        return startY;
    }
}
