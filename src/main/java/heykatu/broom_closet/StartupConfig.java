package heykatu.broom_closet;

import net.neoforged.neoforge.common.ModConfigSpec;

// Note: NeoForge loads STARTUP configs synchronously the instant they're registered (in the mod constructor),
// before RegisterEvent ever fires. So COMMON/CLIENT configs load later, after registries are already frozen,
// so only startup can gate item registry.

public class StartupConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue WEARABLES_ENABLED = BUILDER.comment(
            "Whether the cosmetic hat items (merged in from Witchy Wearables) are registered at all.",
            "When false, the items are never added to any registry -- they won't exist in creative,",
            "JEI, recipes, or anywhere else, and cost no memory. Takes effect on next launch.",
            "NOT synced between client and server -- keep this the same on both, or a client may",
            "fail to connect to a server running a different value."
    ).define("wearablesEnabled", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
