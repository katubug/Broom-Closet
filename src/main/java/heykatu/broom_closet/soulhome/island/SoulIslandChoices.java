package heykatu.broom_closet.soulhome.island;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Which structure each player picked for their soul home, keyed by the home owner's UUID.
// Stored on the overworld rather than on the player because the island is placed during dimension
// creation, which can be triggered by someone else's personal key while the owner is offline.
public final class SoulIslandChoices extends SavedData {
    private static final String NAME = "broom_closet_soul_islands";

    private static final Factory<SoulIslandChoices> FACTORY = new Factory<>(SoulIslandChoices::new, SoulIslandChoices::load, null);

    private final Map<UUID, ResourceLocation> choices = new HashMap<>();
    // the option's "spawn", structure-relative. copied at pick time so a /reload can't change it
    private final Map<UUID, BlockPos> localSpawns = new HashMap<>();
    // world pos, filled in once the island actually gets placed
    private final Map<UUID, BlockPos> spawns = new HashMap<>();

    public static SoulIslandChoices get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public ResourceLocation get(UUID owner) {
        return choices.get(owner);
    }

    public boolean has(UUID owner) {
        return choices.containsKey(owner);
    }

    // First pick wins; the island can't be swapped out from under an existing home.
    public boolean set(UUID owner, ResourceLocation structure, @Nullable BlockPos localSpawn) {
        if (choices.putIfAbsent(owner, structure) != null) return false;
        if (localSpawn != null) localSpawns.put(owner, localSpawn);
        setDirty();
        return true;
    }

    @Nullable
    public BlockPos localSpawn(UUID owner) {
        return localSpawns.get(owner);
    }

    @Nullable
    public BlockPos spawn(UUID owner) {
        return spawns.get(owner);
    }

    public void setSpawn(UUID owner, BlockPos pos) {
        spawns.put(owner, pos.immutable());
        setDirty();
    }

    private static SoulIslandChoices load(CompoundTag tag, HolderLookup.Provider registries) {
        SoulIslandChoices data = new SoulIslandChoices();
        for (String key : tag.getAllKeys()) {
            UUID owner;
            try {
                owner = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }

            // old saves are just uuid -> "structure"
            if (tag.contains(key, Tag.TAG_STRING)) {
                ResourceLocation structure = ResourceLocation.tryParse(tag.getString(key));
                if (structure != null) data.choices.put(owner, structure);
                continue;
            }

            CompoundTag entry = tag.getCompound(key);
            ResourceLocation structure = ResourceLocation.tryParse(entry.getString("structure"));
            if (structure == null) continue;
            data.choices.put(owner, structure);
            NbtUtils.readBlockPos(entry, "local_spawn").ifPresent(p -> data.localSpawns.put(owner, p));
            NbtUtils.readBlockPos(entry, "spawn").ifPresent(p -> data.spawns.put(owner, p));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        choices.forEach((owner, structure) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("structure", structure.toString());
            if (localSpawns.containsKey(owner)) entry.put("local_spawn", NbtUtils.writeBlockPos(localSpawns.get(owner)));
            if (spawns.containsKey(owner)) entry.put("spawn", NbtUtils.writeBlockPos(spawns.get(owner)));
            tag.put(owner.toString(), entry);
        });
        return tag;
    }
}
