package heykatu.broom_closet;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = BroomCloset.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- SoulHome addon fixes ------------------------------------------------------------------
    // Both are no-ops in a pack without SoulHome installed; see soulhome/SoulHomeCompat.java.
    private static final ModConfigSpec.BooleanValue SOULHOME_WEATHER = BUILDER.comment(
            "Whether weather (heehee) applies inside the soul home dimensions.",
            "Requires SoulHome to be installed. Takes effect on the next /reload."
    ).define("soulhomeWeather", true);

    private static final ModConfigSpec.BooleanValue SOULHOME_SLEEP = BUILDER.comment(
            "Whether sleeping in a soul home passes the night. SoulHome's dimensions delegate their",
            "When on, sleepers in the overworld and in soul homes are counted together against the,",
            "playersSleepingPercentage gamerule, and the overworld's clock is advanced instead.",
            "Requires SoulHome to be installed."
    ).define("soulhomeSleep", true);

    private static final ModConfigSpec.BooleanValue SOULHOME_RESET_PHANTOM_TIMER = BUILDER.comment(
            "When a soul home sleeper passes the night, reset the phantom timer for everyone, not just",
            "the sleeper. Cottage Witch needs this config in order to to mirror Midnight Thoughts'",
            "'resetPhantomTimerForNonSleepers' option, which will otherwise not work when someone",
            "in the Soul Home dimension is sleeping.",
            "Doesn't require SoulHome or Midnight Thoughts.s"
    ).define("soulhomeResetPhantomTimer", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean soulhomeWeather;
    public static boolean soulhomeSleep;
    public static boolean soulhomeResetPhantomTimer;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // The mod now registers two ModConfigSpecs (this and cClientConfig), and both
        // classes' onLoad listen for any ModConfigEvent on the mod bus. Need this guard
        // so that each only reads its own spec's values once that spec is done loading.
        if (event.getConfig().getSpec() != SPEC) return;

        soulhomeWeather = SOULHOME_WEATHER.get();
        soulhomeSleep = SOULHOME_SLEEP.get();
        soulhomeResetPhantomTimer = SOULHOME_RESET_PHANTOM_TIMER.get();
    }
}
