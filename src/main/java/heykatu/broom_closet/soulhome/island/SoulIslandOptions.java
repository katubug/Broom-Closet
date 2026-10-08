package heykatu.broom_closet.soulhome.island;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import heykatu.broom_closet.BroomCloset;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Loads the picker's island list from data/<ns>/soul_island/*.json. Reloads with /reload.
@EventBusSubscriber(modid = BroomCloset.MODID)
public final class SoulIslandOptions extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();

    // Kept in display order (order field, then id).
    private static volatile Map<ResourceLocation, SoulIslandOption> options = Map.of();

    private final HolderLookup.Provider registries;

    private SoulIslandOptions(HolderLookup.Provider registries) {
        super(GSON, "soul_island");
        this.registries = registries;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SoulIslandOptions(event.getRegistryAccess()));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        Map<ResourceLocation, SoulIslandOption> loaded = new LinkedHashMap<>();

        jsons.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> SoulIslandOption.CODEC.parse(ops, e.getValue())
                        .resultOrPartial(err -> LOGGER.error("Bad soul island {}: {}", e.getKey(), err))
                        .ifPresent(option -> loaded.put(e.getKey(), option)));

        Map<ResourceLocation, SoulIslandOption> sorted = new LinkedHashMap<>();
        loaded.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().order()))
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));

        options = sorted;
        LOGGER.info("Loaded {} soul islands", sorted.size());
    }

    public static SoulIslandOption get(ResourceLocation id) {
        return options.get(id);
    }

    public static boolean isEmpty() {
        return options.isEmpty();
    }

    public static List<SoulIslandOption.Entry> entries() {
        return options.entrySet().stream()
                .map(e -> new SoulIslandOption.Entry(e.getKey(), e.getValue().screenshot(), e.getValue().name(), e.getValue().description()))
                .toList();
    }
}
