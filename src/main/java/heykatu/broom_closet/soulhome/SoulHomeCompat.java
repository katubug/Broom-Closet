package heykatu.broom_closet.soulhome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.fml.ModList;

// Addon support for SoulHome.
//
// HEADS UP -- this feature also ships a file in SoulHome's OWN namespace:
//   src/main/resources/data/soulhome/dimension_type/soulhome.json
// It is a full replacement for SoulHome's dimension type, lowering ambient_light from 0.8 to 0.2
// and dropping fixed_time so the dimension follows the overworld's clock and can actually get dark.
public final class SoulHomeCompat {
    public static final String MODID = "soulhome";

    // SoulHome registers BOTH its dimension type AND its biome under the same "soulhome:soulhome" id. great.
    private static final ResourceLocation SOULHOME_ID = ResourceLocation.fromNamespaceAndPath(MODID, MODID);

    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, SOULHOME_ID);
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, SOULHOME_ID);

    // The DimensionSpecialEffects key SoulHome's dimension_type JSON names in its "effects" field.
    public static final ResourceLocation SKY_EFFECTS = ResourceLocation.fromNamespaceAndPath(MODID, "soul_sky_property");

    private SoulHomeCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    public static boolean isSoulHome(Level level) {
        DimensionType type = level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).get(DIMENSION_TYPE);
        return type != null && type.equals(level.dimensionType());
    }
}
