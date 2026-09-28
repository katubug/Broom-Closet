package heykatu.broom_closet;

import com.mojang.logging.LogUtils;
import heykatu.broom_closet.ftbchunks.FtbChunksSidebarOverrides;
import heykatu.broom_closet.ftbquests.FtbTextColorOverrides;
import heykatu.broom_closet.soulhome.SoulHomeBiomeModifiers;
import heykatu.broom_closet.wearables.Wearables;
import heykatu.broom_closet.wishlist.Wishlist;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BroomCloset.MODID)
public class BroomCloset {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "broom_closet";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "broom_closet" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "broom_closet" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "broom_closet" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BroomCloset(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Registered first: StartupConfig holds every setting (across all features) that decides
        // what gets registered at all.
        modContainer.registerConfig(ModConfig.Type.STARTUP, StartupConfig.SPEC, MODID + "/startup.toml");

        // Cosmetic hats - when disabled, the items are never registered
        if (StartupConfig.WEARABLES_ENABLED.get()) {
            Wearables.register(modEventBus);
        }

        // Wishlist: purely client-side visual preference
        Wishlist.register(modEventBus);

        // SoulHome addon fixes. runtime behavior is gated in Config
        SoulHomeBiomeModifiers.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, MODID + "/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, MODID + "/client.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("Hello from inside the broom closet o-o");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            // Guarded so FtbTextColorOverrides is never loaded on a pack without FTB Quests installed.
            if (ClientConfig.ftbStylingActive()) {
                FtbTextColorOverrides.apply();
            }

            // Guarded so FtbChunksSidebarOverrides is never loaded on a pack without FTB Chunks installed.
            if (ModList.get().isLoaded("ftbchunks")) {
                FtbChunksSidebarOverrides.register();
            }
        }
    }
}
