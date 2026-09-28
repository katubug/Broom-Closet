package heykatu.broom_closet.wearables.client;

import heykatu.broom_closet.BroomCloset;
import heykatu.broom_closet.wearables.Wearables;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@EventBusSubscriber(modid = BroomCloset.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WearablesClient {

    private static final HatModelCache CACHE = new HatModelCache();

    @SubscribeEvent
    public static void addModels(ModelEvent.RegisterAdditional event) {
        event.register(Wearables.armorModelId("cat_ears"));
        event.register(Wearables.armorModelId("demon_horns"));
        event.register(Wearables.armorModelId("druid_horns"));
        event.register(Wearables.armorModelId("elf_ears"));
        event.register(Wearables.armorModelId("frog"));
        event.register(Wearables.armorModelId("frog_top_hat"));
        event.register(Wearables.armorModelId("goblin_ears"));
        event.register(Wearables.armorModelId("antlers"));
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(CACHE);
    }

    public static HatModel getHatModel(Item item) {
        return CACHE.getOrCompute(item);
    }

    static class HatModelCache implements PreparableReloadListener {

        private final Map<Item, HatModel> cache = new HashMap<>();

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                               ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                               Executor backgroundExecutor, Executor gameExecutor) {
            return barrier.wait(null).thenRunAsync(cache::clear, gameExecutor);
        }

        public HatModel getOrCompute(Item item) {
            return cache.computeIfAbsent(item, i -> {
                String path = BuiltInRegistries.ITEM.getKey(i).getPath();
                return new HatModel(Wearables.armorModelId(path));
            });
        }
    }
}
