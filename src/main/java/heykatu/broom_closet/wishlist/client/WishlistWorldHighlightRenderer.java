package heykatu.broom_closet.wishlist.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashSet;
import java.util.Set;

// Highlights every loaded block matching a wishlisted BlockItem with a translucent tinted
// overlay, occluded normally by walls.
// Rescans on a low-frequency timer. Deliberately capped to a couple of chunks rather than full render distance.
public class WishlistWorldHighlightRenderer {

    private static final int RESCAN_INTERVAL_TICKS = 30;
    private static final int SCAN_RADIUS_CHUNKS = 2;
    private static final float COLOR_R = 1.0F;
    private static final float COLOR_G = 0.84F;
    private static final float COLOR_B = 0.0F;
    private static final float COLOR_A = 0.35F;

    private int ticksUntilRescan = 0;
    private int lastScannedVersion = -1;
    private Set<BlockPos> cachedPositions = Set.of();

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        boolean dirty = WishlistData.version() != lastScannedVersion;
        if (!dirty && --ticksUntilRescan > 0) {
            return;
        }

        ticksUntilRescan = RESCAN_INTERVAL_TICKS;
        lastScannedVersion = WishlistData.version();
        rescan();
    }

    private void rescan() {
        Set<Block> targets = WishlistData.wishlistedBlocks();
        if (targets.isEmpty()) {
            cachedPositions = Set.of();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null) {
            cachedPositions = Set.of();
            return;
        }

        int radius = SCAN_RADIUS_CHUNKS;
        int centerX = player.chunkPosition().x;
        int centerZ = player.chunkPosition().z;
        Set<BlockPos> found = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunk(centerX + dx, centerZ + dz, false);
                if (chunk == null) {
                    continue;
                }

                LevelChunkSection[] sections = chunk.getSections();
                int minSection = chunk.getMinSection();
                int chunkBlockX = chunk.getPos().x * 16;
                int chunkBlockZ = chunk.getPos().z * 16;

                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    if (section.hasOnlyAir() || !section.maybeHas(state -> targets.contains(state.getBlock()))) {
                        continue;
                    }

                    int sectionBlockY = (minSection + i) * 16;
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = section.getBlockState(x, y, z);
                                if (targets.contains(state.getBlock())) {
                                    found.add(new BlockPos(chunkBlockX + x, sectionBlockY + y, chunkBlockZ + z));
                                }
                            }
                        }
                    }
                }
            }
        }

        cachedPositions = found;
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || cachedPositions.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // Per-position push mirrors NeoForge's own BlockEntityRenderBoundsDebugRenderer -- rather than
        // translating the shared poseStack once for the whole batch. tried AFTER_SOLID_BLOCKS but that
        // made highlights track camera lol
        for (BlockPos pos : cachedPositions) {
            Vec3 offset = Vec3.atLowerCornerOf(pos).subtract(camera);
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);

            DebugRenderer.renderFilledBox(
                    poseStack, bufferSource,
                    0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
                    COLOR_R, COLOR_G, COLOR_B, COLOR_A
            );

            poseStack.popPose();
        }

        bufferSource.endBatch(RenderType.debugFilledBox());
    }
}
