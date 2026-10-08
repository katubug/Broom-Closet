package heykatu.broom_closet;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.regex.Pattern;

// Client-only: home for settings client/cosmetic settings.
@EventBusSubscriber(modid = BroomCloset.MODID, value = Dist.CLIENT)
public class ClientConfig {
    private static final Pattern CODE_REMAP_PATTERN = Pattern.compile("^[A-Za-z0-9]:#[0-9A-Fa-f]{6}$");

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue FTB_STYLING = BUILDER.comment(
            "Whether to override FTB Quests' hardcoded icon tint, subtitle color, and formatting shortcode styling,",
            "Requires FTB Quests."
    ).define("ftbStyling", true);

    private static final ModConfigSpec.ConfigValue<String> QUEST_SHAPE_TINT_COLOR = BUILDER.comment(
            "Tint color for a quest icon's *shape* on the chapter map. FTB Quests hex format: #RRGGBB or #AARRGGBB",
            "Requires FTB Quests."
    ).define("questShapeTintColor", "#212121");

    private static final ModConfigSpec.ConfigValue<String> QUEST_BACKGROUND_TINT_COLOR = BUILDER.comment(
            "Tint color for a quest icon's *background* on the chapter map. FTB Quests hex format: #RRGGBB or #AARRGGBB",
            "Requires FTB Quests."
    ).define("questBackgroundTintColor", "#96FFFFFF");

    private static final ModConfigSpec.ConfigValue<String> QUEST_SUBTITLE_COLOR = BUILDER.comment(
            "Text color for a quest's subtitle. FTB Hardcodes this so we're going rogue. Hex format: #RRGGBB.",
            "Applies both in the opened-quest panel and in the quest's chapter-map tooltip.",
            "Requires FTB Quests."
    ).define("questSubtitleColor", "#AAAAAA");

    private static final ModConfigSpec.ConfigValue<String> CHAPTER_SUBTITLE_COLOR = BUILDER.comment(
            "Text color for a *chapter's* own subtitle, shown in its chapter-tab tooltip. Hex format: #RRGGBB",
            "Requires FTB Quests."
    ).define("chapterSubtitleColor", "#AAAAAA");

    private static final ModConfigSpec.ConfigValue<String> CHAPTER_SELECTED_BORDER_COLOR = BUILDER.comment(
            "Border color around the currently-selected chapter tab. FTB Quests hex format: #RRGGBB or #AARRGGBB",
            "Requires FTB Quests"
    ).define("chapterSelectedBorderColor", "#C0AAAAAA");

    private static final ModConfigSpec.ConfigValue<String> CHAPTER_HOVER_HIGHLIGHT_COLOR = BUILDER.comment(
            "Highlight color shown when hovering a *non-selected* chapter tab. FTB Quests hex format: #RRGGBB or #AARRGGBB",
            "Requires FTB Quests"
    ).define("chapterHoverHighlightColor", "#28FFFFFF");

    private static final ModConfigSpec.BooleanValue FTB_TEXT_SHADOW_ENABLED = BUILDER.comment(
            "Whether FTB Quests/Library text draws a drop shadow at all. Set to false to disable text shadows entirely.",
            "Requires FTB Quests and Library. NOTE: this changes across all FTB Library, not just Quests."
    ).define("ftbTextShadowEnabled", true);

    private static final ModConfigSpec.ConfigValue<String> FTB_TEXT_SHADOW_COLOR = BUILDER.comment(
            "Color of the FTB Quests/Library text drop shadow. FTB Quests hex format: #RRGGBB or #AARRGGBB.",
            "Leave empty (\"\") to use vanilla's own behavior: a shadow auto-derived from each text's own color at 25% brightness.",
            "Has no effect if ftbTextShadowEnabled is false.",
            "Requires FTB Quests/Library."
    ).define("ftbTextShadowColor", "");

    private static final ModConfigSpec.IntValue FTB_TEXT_SHADOW_OFFSET_X = BUILDER.comment(
            "Horizontal pixel offset of the text drop shadow from its text. Vanilla's default is 1 (shadow drawn 1px right).",
            "Requires FTB Quests"
    ).defineInRange("ftbTextShadowOffsetX", 1, -16, 16);

    private static final ModConfigSpec.IntValue FTB_TEXT_SHADOW_OFFSET_Y = BUILDER.comment(
            "Vertical pixel offset of the text drop shadow from its text. Vanilla's default is 1 (shadow drawn 1px down).",
            "Requires FTB Quests."
    ).defineInRange("ftbTextShadowOffsetY", 1, -16, 16);

    // Seeded with FTB's own default 16 legacy color codes (0-9, a-f).
    // Deliberately excludes k/l/m/n/o/r (obfuscated/bold/strikethrough/underline/italic/reset)
    // Also excludes 'z' (FTB's rainbow code), which has no fixed hex to seed with.
    private static final List<String> DEFAULT_CODE_REMAPS = List.of(
            "0:#000000", "1:#0000AA", "2:#00AA00", "3:#00AAAA",
            "4:#AA0000", "5:#AA00AA", "6:#FFAA00", "7:#AAAAAA",
            "8:#555555", "9:#5555FF", "a:#55FF55", "b:#55FFFF",
            "c:#FF5555", "d:#FF55FF", "e:#FFFF55", "f:#FFFFFF"
    );

    private static final ModConfigSpec.BooleanValue DISABLE_FTB_CHUNKS_MAP = BUILDER.comment(
            "Disables FTB Chunks' fullscreen world map (M key) and minimap entirely.",
            "BECAUSE FTB CHUNKS IS A PUNK. MORE LIKE FTB PUNKS.",
            "Does not affect chunk claiming (a separate screen/keybind) or FTB Chunks' claim protection.",
            "Requires FTB Chunks."
    ).define("disableFtbChunksMap", false);

    private static final ModConfigSpec.BooleanValue DISABLE_DEATH_WAYPOINTS = BUILDER.comment(
            "Disables FTB Chunks' automatic death waypoints (the red \"Death #N\" markers).",
            "This bypasses FTB's config entirely and cancels death waypoint creation at the source.",
            "I wish it hadn't come to this. Requires FTB Chunks."
    ).define("disableDeathWaypoints", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> CODE_REMAPS = BUILDER.comment(
            "Remap or add FTB Quests text formatting shortcodes (e.g. &c). Each entry is \"<letter>:#RRGGBB\", for example \"c:#FF0000\".",
            "Note that this applies to any FTB Library-based screen (not just FTB Quests)",
            "Defaults below are FTB's own colors for 0-9/a-f, either reassign the colors, or add unused letters as new codes.",
            "However, DO NOT use the following letters: k, l, m, n, o, r, or z here. Those are other formatting codes and remapping them",
            "will potentially cause issues!",
            "Changes to this list *only* take effect after a *full client restart* (quitting and relaunching).",
            "Requires FTB Quests."
    ).defineListAllowEmpty("codeRemaps", DEFAULT_CODE_REMAPS, () -> "g:#FFFFFF", ClientConfig::validateCodeRemap);

    private static final ModConfigSpec.BooleanValue SOULHOME_STORM_DARKENING = BUILDER.comment(
            "Whether thunderstorms visibly dim SoulHome's soul home dimensions. Requires a client restart.",
            "Works together with the dimension_type override of the ambient light. Without that config option,",
            "darkening will be less noticeable. Requires SoulHome."
    ).define("soulhomeStormDarkening", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean ftbStyling;
    public static String questShapeTintColor;
    public static String questBackgroundTintColor;
    public static String questSubtitleColor;
    public static String chapterSubtitleColor;
    public static String chapterSelectedBorderColor;
    public static String chapterHoverHighlightColor;
    public static boolean ftbTextShadowEnabled;
    public static String ftbTextShadowColor;
    public static int ftbTextShadowOffsetX;
    public static int ftbTextShadowOffsetY;
    public static List<? extends String> codeRemaps;
    public static boolean disableFtbChunksMap;
    public static boolean disableDeathWaypoints;
    public static boolean soulhomeStormDarkening;

    private static boolean validateCodeRemap(final Object obj) {
        return obj instanceof String s && CODE_REMAP_PATTERN.matcher(s).matches();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // The mod now registers two ModConfigSpecs (this and Config), and both
        // classes' onLoad listen for any ModConfigEvent on the mod bus. Need this guard
        // so that each only reads its own spec's values once that spec is done loading.
        if (event.getConfig().getSpec() != SPEC) return;

        ftbStyling = FTB_STYLING.get();
        questShapeTintColor = QUEST_SHAPE_TINT_COLOR.get();
        questBackgroundTintColor = QUEST_BACKGROUND_TINT_COLOR.get();
        questSubtitleColor = QUEST_SUBTITLE_COLOR.get();
        chapterSubtitleColor = CHAPTER_SUBTITLE_COLOR.get();
        chapterSelectedBorderColor = CHAPTER_SELECTED_BORDER_COLOR.get();
        chapterHoverHighlightColor = CHAPTER_HOVER_HIGHLIGHT_COLOR.get();
        ftbTextShadowEnabled = FTB_TEXT_SHADOW_ENABLED.get();
        ftbTextShadowColor = FTB_TEXT_SHADOW_COLOR.get();
        ftbTextShadowOffsetX = FTB_TEXT_SHADOW_OFFSET_X.get();
        ftbTextShadowOffsetY = FTB_TEXT_SHADOW_OFFSET_Y.get();
        codeRemaps = CODE_REMAPS.get();
        disableFtbChunksMap = DISABLE_FTB_CHUNKS_MAP.get();
        disableDeathWaypoints = DISABLE_DEATH_WAYPOINTS.get();
        soulhomeStormDarkening = SOULHOME_STORM_DARKENING.get();
    }

    // Off in packs without FTB Quests regardless of the config default.
    public static boolean ftbStylingActive() {
        return ModList.get().isLoaded("ftbquests") && ftbStyling;
    }

    // Off in packs without FTB Chunks regardless of the config default.
    public static boolean ftbChunksMapDisabled() {
        return ModList.get().isLoaded("ftbchunks") && disableFtbChunksMap;
    }

    // Off in packs without FTB Chunks regardless of the config default.
    public static boolean deathWaypointsDisabled() {
        return ModList.get().isLoaded("ftbchunks") && disableDeathWaypoints;
    }
}
