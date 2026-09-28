package heykatu.broom_closet.soulhome.island;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

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
    public boolean set(UUID owner, ResourceLocation structure) {
        if (choices.putIfAbsent(owner, structure) != null) return false;
        setDirty();
        return true;
    }

    private static SoulIslandChoices load(CompoundTag tag, HolderLookup.Provider registries) {
        SoulIslandChoices data = new SoulIslandChoices();
        for (String key : tag.getAllKeys()) {
            ResourceLocation structure = ResourceLocation.tryParse(tag.getString(key));
            if (structure == null) continue;
            try {
                data.choices.put(UUID.fromString(key), structure);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        choices.forEach((owner, structure) -> tag.putString(owner.toString(), structure.toString()));
        return tag;
    }
}
