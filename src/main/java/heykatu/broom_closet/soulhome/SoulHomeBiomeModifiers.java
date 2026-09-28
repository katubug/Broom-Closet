package heykatu.broom_closet.soulhome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import heykatu.broom_closet.BroomCloset;
import heykatu.broom_closet.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

// Turns precipitation back on for SoulHome's biome
public final class SoulHomeBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, BroomCloset.MODID);

    public static final Supplier<MapCodec<Precipitation>> PRECIPITATION = BIOME_MODIFIER_SERIALIZERS.register(
            "soulhome_precipitation",
            () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(Precipitation::biomes)
            ).apply(instance, Precipitation::new)));

    private SoulHomeBiomeModifiers() {}

    // Registered unconditionally, even in packs without SoulHome. TODO: do i wanna change this?
    public static void register(IEventBus modEventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
    }

    public record Precipitation(HolderSet<Biome> biomes) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            // MODIFY is the phase NeoForge documents for changing single values like climate.
            if (phase != Phase.MODIFY || !this.biomes.contains(biome)) return;
            // Read here rather than gating registration, so toggling the config only needs a
            // datapack reload (/reload)
            if (!Config.soulhomeWeather) return;

            builder.getClimateSettings().setHasPrecipitation(true);
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return PRECIPITATION.get();
        }
    }
}
