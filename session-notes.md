# Session Notes

## Session 2026-07-01 — FTB Quests optional styling override (scaffolding + real mixins + config)

### What was accomplished

Built an optional, client-side FTB Quests (NeoForge build 2101.1.27) styling override feature,
fully end-user configurable via a NeoForge TOML config, in a mod (`broom_closet`) that must keep
working (and doing its own unrelated things) in modpacks where FTB Quests isn't installed.

**Scaffolding (optional-dependency infrastructure):**
- `FTBQuestsMixinPlugin` (`heykatu.broom_closet.mixin`) — `IMixinConfigPlugin` that checks
  `LoadingModList.get().getModFileById("ftbquests") != null` and gates whether the FTB mixin
  config's mixins get applied. Deliberately lives *outside* `heykatu.broom_closet.mixin.ftbquests`
  (Mixin loads its configured package specially; the plugin can't live inside the package it gates).
- `broom_closet.ftbquests.mixins.json` — second, sibling mixin config to the wizard's
  `broom_closet.mixins.json`, `package: heykatu.broom_closet.mixin.ftbquests`, gated by the plugin above.
- `build.gradle` — corrected FTB Maven coordinates (see Bugs below); FTB deps kept `implementation`,
  dev/compile-time only per FTB's ARR license (not bundled into release jars).
- `neoforge.mods.toml` template — added second `[[mixins]]` block + an `optional`/`AFTER`/`BOTH`
  dependency block for `ftbquests`.

**Real overrides (this session's main work), driven entirely by decompiling FTB Quests/Library
with Vineflower — see Lessons for tooling notes:**
- `QuestButtonMixin` (targets `dev.ftb.mods.ftbquests.client.gui.quests.QuestButton#draw`):
  overrides the quest icon's shape tint (was hardcoded `Color4I.DARK_GRAY`) and background tint
  (was hardcoded `Color4I.WHITE.withAlpha(150)`), both via MixinExtras `@ModifyExpressionValue`.
- `ViewQuestPanelMixin` (targets `...ViewQuestPanel#addWidgets`): overrides the quest subtitle
  text color (was hardcoded `ChatFormatting.GRAY`), re-applying just the color on top of the
  original styled component so italics are preserved.
- `FtbTextColorOverrides` (`heykatu.broom_closet.ftbquests`, **not** a mixin) — remaps/adds FTB's
  `&`-style legacy text shortcodes (e.g. `&c`) by directly mutating FTB Library's own
  `TextComponentParser.CODE_TO_FORMATTING` / `SPECIAL_COLOR_CODES` public static maps. No bytecode
  transform needed for this one at all.
- `FtbStylingConfig` (new, `ModConfig.Type.CLIENT`) — `ftbStyling` master toggle,
  `questShapeTintColor` (default `#212121`), `questBackgroundTintColor` (default `#96FFFFFF`),
  `questSubtitleColor` (default `#AAAAAA`), and `codeRemaps` (`List<String>` of `"<letter>:#RRGGBB"`
  entries, seeded by default with FTB's own 16 legacy color codes `0-9`/`a-f` at their real vanilla
  RGB values so the list ships fully populated rather than empty). Config comment explicitly warns
  against remapping `k/l/m/n/o/r`/`z` (formatting effects / rainbow, not real colors — doing so
  would replace the effect with a flat recolor, e.g. `l:#FF0000` would turn bold text into just red
  non-bold text).
- Color parsing reuses existing utilities throughout — no custom hex parser was written:
  `Color4I.fromString(String)` (FTB Library's own canonical parser, accepts `#RRGGBB`/`#AARRGGBB`)
  for the two `Color4I`-typed tints, and vanilla `TextColor.parseColor(String)` (what FTB's own
  parser uses internally) for the subtitle color and the shortcode remap list.

**Round 2 — decompiled the remaining chapter/theme classes for more hardcoded colors, after
playtesting confirmed round 1 worked.** Reviewed `ChapterPanel`, `QuestPanel`, `QuestScreen`,
`FTBQuestsTheme`, `TabButton`, `ChapterImageButton`, `ExpandChaptersButton`,
`OtherButtonsPanelBottom`, `TaskButton`, `RewardButton`. Most hits turned out to be edit-mode-only
(gated behind `file.canEdit()`), debug-tooltip-only (gated behind F3/Shift+Ctrl), translation-warning
highlights, or minor hover-text colors on tooltips — deliberately left alone. Three were judged
worth adding, plus a fourth that was added and then reverted:
- `ChapterPanelMixin` (targets the **nested** class `ChapterPanel.ChapterButton`) — three overrides:
  - `chapterSelectedBorderColor` (default `#C0AAAAAA`) — border around the selected chapter tab. **Confirmed working by playtest.**
  - `chapterHoverHighlightColor` (default `#28FFFFFF`) — hover highlight on a non-selected chapter tab. **Confirmed NOT working by playtest — see Known Issues.**
  - `chapterSubtitleColor` (default `#AAAAAA`, new config key, separate from `questSubtitleColor`) — the chapter's own subtitle in its tab tooltip. Targets a **synthetic lambda method** (`lambda$new$0`), not the constructor directly — a stream `.map(line -> ...)` lambda compiles to its own method in bytecode, confirmed via `javap -p`, not guessed.
- `QuestButtonMixin` — added a second injection for the quest subtitle's **other** appearance: the
  tooltip on the chapter map (`addMouseOverText`), reusing the existing `questSubtitleColor` (same
  visual concept as the in-panel one `ViewQuestPanelMixin` already covered, so one setting drives
  both consistently). This method has 7 identical-descriptor `withStyle(ChatFormatting)` calls —
  ordinal 1 is ours, verified via `javap -c` bytecode offsets, not source-line counting.
- `FTBQuestsThemeMixin` (`widgetUndercoatColor`, targeting `FTBQuestsTheme#drawWidget`'s
  `Color4I.BLACK.withAlpha(60)`) — **added, then removed at the user's request** ("overkill"). File
  deleted, config field/entry removed, mixin config's `client` list entry removed. If revisited,
  the injection target itself was sound (single, unambiguous `withAlpha` call in that method).

### Bugs found and root causes

1. **Client crash: `InvalidMixinException: mixin '...QuestShapeTintMixin' was not found`.**
   Root cause: an earlier placeholder mixin name (`QuestShapeTintMixin`) was listed in
   `broom_closet.ftbquests.mixins.json`'s `client` array before the real class existed. Mixin
   must locate and read a listed mixin class's bytecode to prepare the config *before*
   `IMixinConfigPlugin.shouldApplyMixin` is ever consulted — the plugin only controls whether an
   *existing* mixin gets applied, not whether the class needs to exist. A dedicated-server test
   run didn't catch this because `client`-only mixins are skipped entirely on that distribution
   regardless. **Fix:** don't list a mixin class name until the class actually exists; emptied the
   list until the real mixins were written.
2. **Wrong FTB Maven coordinates.** Guessed `ftb-library-neoforge:2101.1.13` /
   `ftb-teams-neoforge:2101.1.7` didn't match what `ftb-quests-neoforge:2101.1.27`'s own published
   POM declares (`2101.1.31` / `2101.1.9`). Fixed by reading the POM directly from
   `https://maven.ftb.dev/releases`.
3. **Gradle defaulted to Java 11**, but ModDevGradle requires Java 17+ to even apply the plugin.
   Fixed by setting the user-level `JAVA_HOME` env var to a JDK 21 install
   (`C:\Program Files\Java\jdk-21`) — this only affects *new* processes, so an already-open
   IntelliJ/terminal needs a restart to pick it up.
4. **Client crash: `IllegalStateException: Cannot get config value before config is loaded`.**
   Root cause: registering a *second* `ModConfigSpec` (`FtbStylingConfig`, `CLIENT` type)
   alongside the existing `Config` (`COMMON` type) meant both classes' `@SubscribeEvent
   onLoad(ModConfigEvent)` handlers fired for *both* specs' loading events (mod-bus subscribers
   aren't filtered by which spec is loading). Each tried to read its own values even when the
   *other* spec was the one that had just loaded. **Fix:** every `onLoad` now starts with
   `if (event.getConfig().getSpec() != SPEC) return;`. This is required any time a mod has more
   than one `ModConfigSpec` — not obvious from the wizard's single-config template.
5. **Ambiguous Mixin injection targets, caught before they became bugs (verified via decompiled
   bytecode, not assumed):** `Color4I.withAlpha(int)` is called 4 times inside
   `QuestButton#draw` (background tint, selection glow, locked overlay, mouse-over highlight) —
   needed `ordinal = 0` to hit only the background-tint call. `MutableComponent.withStyle(...)`
   has both a single-arg and vararg overload with different descriptors, so the 2-arg
   `withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)` call turned out to be uniquely
   identifiable by descriptor alone (confirmed by grepping the whole file for other multi-arg
   `withStyle` calls — there were none).
6. **`codeRemaps` config changes silently don't apply without a full client restart — confirmed
   by playtest, not just theorized.** Symptom the user hit: adding a new shortcode letter threw
   FTB's own `BadFormatException` ("Invalid character after &"), and remapping an existing letter's
   color had no visible effect, in both cases after only `/reload` + F3+T (no full restart). Root
   cause: `FtbTextColorOverrides.apply()` (the code that mutates FTB Library's static maps) only
   runs once, from `FMLClientSetupEvent`, which fires a single time at game startup — neither
   `/reload` nor F3+T re-fires it. A full quit-and-relaunch confirmed as the actual fix. Left as a
   documented limitation (a `KNOWN LIMITATION` line in the config comment, added by the user
   directly) rather than implemented as hot-reloadable, since that would require tracking and
   reverting previously-applied remaps to avoid stale entries — judged not worth the complexity
   for now.

### Key decisions

- **CLIENT vs COMMON config split**: `FtbStylingConfig` is `ModConfig.Type.CLIENT` since none of
  these settings has server relevance (FTB Quests' chapter map / quest panel are 100% client GUI).
  The wizard's original `Config.java` stays `COMMON` and untouched aside from removing the
  `ftbStyling` field that had been added there in an earlier pass (migrated to the new class).
- **`&`-code remapping is not a mixin.** FTB Library's `TextComponentParser` exposes its
  letter-to-color lookup as `public static final` but *internally mutable* maps — a plain method
  call (`.remove()`/`.put()`) does the job. This is simpler and more robust than bytecode
  transforms, but it does mean the code touches FTB Library classes directly, so it's placed
  outside any `mixin.*` package and is only ever invoked from behind the `ftbStylingActive()`
  guard (mirrors the same "don't touch FTB classes unless FTB is confirmed present" discipline
  the mixin plugin enforces for the bytecode-transform side of the feature).
- Reuse FTB's/vanilla's own color-string parsers instead of writing one, for both correctness and
  consistency with the format modpack authors already see in `ftb_quests_theme.txt`-style config.

### Lessons learned

- **Decompiling the actual dependency jars beats guessing from memory.** Vineflower is already
  present in the Gradle cache (`org.vineflower:vineflower:1.10.1` — the same decompiler
  ModDevGradle uses internally to produce Minecraft sources), so no extra download was needed.
  Every hardcoded value and injection-target claim in this session was verified this way, not
  assumed — this caught the `withAlpha` ordinal issue and confirmed `applyFormat`/`withColor`
  are behaviorally equivalent for color-type `ChatFormatting` (needed to confirm the default
  `codeRemaps` seed values are a true visual no-op).
- **Test on both `runServer` and `runClient`.** The dedicated server run alone gave false
  confidence — `client`-only mixins are silently skipped there, so the missing-mixin-class crash
  only surfaced on `runClient`.
- **`Stop-Process` on a `gradlew.bat`-launched PID doesn't necessarily kill the actual game JVM.**
  Gradle's `JavaExec`-style run tasks fork a child process under a different PID; killing the
  wrapper's PID can leave the real Minecraft process running. Verify with
  `Get-CimInstance Win32_Process -Filter "Name='java.exe'"` (or filter by command line) before
  assuming a background test run is actually stopped.
- Since FTB Quests/Library/Teams are `implementation` (not just `compileOnly`) dependencies,
  ModDevGradle's dev environment loads them as real active mods automatically — the "FTB present"
  path is exercised by `runClient`/`runServer` without any extra setup. There's currently no way
  to test the "FTB absent" path in this same project (would need a second run config or a
  separate scratch project without the FTB deps).

### Known issues (unresolved — revisit someday)

**Chapter tab hover highlight (`chapterHoverHighlightColor`) does not visibly apply, even after a
full client restart.** Confirmed by the user via playtest, on a non-selected chapter tab (ruling
out the obvious "you're hovering the already-selected tab, which takes the other branch" theory —
that was checked and isn't the cause). Notably, the *selected*-tab border override (same mixin,
same method, adjacent ordinal) **does** work correctly, which argues against a wholesale mixin
config/plugin/gating problem and points at something specific to this one injection.

What's already verified and can be ruled out:
- The injection target itself was confirmed against real bytecode with `javap -p -c` (not
  decompiled-source guesswork): `ChapterPanel$ChapterButton#draw` has exactly 3
  `Color4I.withAlpha(int)` calls in bytecode order — RED (ordinal 0, translation warning), GRAY
  (ordinal 1, selected border — **works**), WHITE (ordinal 2, hover highlight — **doesn't work**).
- Not a config-reload/restart issue (same class of bug as the shortcode remap) — a full restart
  was tried and made no difference.
- Not the "hovering the selected tab instead" theory — checked, still fails on a genuinely
  non-selected tab.

Not yet tried / worth investigating next time:
- Confirm the override is even being *evaluated* — e.g., temporarily hardcode an extremely
  obvious color (opaque red, no alpha) for this one override and see if *anything* changes on
  hover, to distinguish "wrong value" from "not applying at all."
- Check whether `isMouseOver()` on `ChapterButton` is actually returning `true` in practice for
  this widget under real mouse input — it's possible the hover state itself is being consumed or
  overridden elsewhere in the panel hierarchy (e.g., a parent panel or an overlapping widget)
  before this widget's own `isMouseOver()` check runs, independent of anything this mod does.
- Double check the live `run/config/broom_closet-client.toml` actually contains the edited value
  at the time of testing (rule out a typo'd hex or a value that didn't save).

### Not yet verified

The `widgetUndercoatColor` override was reverted before ever being playtested, so no data on
whether that one would have worked — moot now that it's removed. Everything else added this
session (shape tint, background tint, quest subtitle in both the panel and tooltip, chapter
subtitle in tooltip, chapter selected-tab border, shortcode remap) has been playtested and
confirmed working, except the chapter hover highlight noted above.

---

## Session 2026-07-01 (2) — Witchy Wearables merge + JEI scaffolding + template cleanup

### What was accomplished

Merged the standalone **Witchy Wearables** mod (`C:\Users\Katu\Documents\GitHub\witchywearables`,
source v1.2.0/NeoForge 1.21.1) into Broom Closet as a new `heykatu.broom_closet.wearables`
package, and separately stripped the NeoForge template's leftover "example block/item" boilerplate.

**Cosmetic hats (8 items):** `cat_ears`, `demon_horns`, `druid_horns`, `elf_ears`, `frog`,
`frog_top_hat`, `goblin_ears`, `antlers` — no-defense `ArmorItem`s that render a standalone block
model (via `HatModel extends HumanoidModel`, sampling the block atlas directly) instead of the
usual leather-dyeable texture-layer armor rendering. Ported over essentially unchanged from the
original mod's `CosmeticHatItem` / `HatModel` / client model-cache design.

- **Namespace: moved from `witchywearables:` to `broom_closet:`** — the user's explicit call
  (asked via `AskUserQuestion`), trading away save-compat with any pre-existing `witchywearables:`
  world for having everything under one namespace. Every reference was rewritten: item registry
  IDs, recipe results/ingredients, model `parent`/texture paths, the block atlas source, lang keys,
  and the `tags/items` file (renamed `witchywearables.json` → `wearables.json` for clarity, now
  resolves as `broom_closet:wearables`).
- **Code structure** (`src/main/java/heykatu/broom_closet/wearables/`): `Wearables` (entry point —
  `register(IEventBus)`, `id()`/`armorModelId()` helpers, creative-tab hookup into vanilla
  `COMBAT`), `WearableArmorMaterials`, `WearableItems`, `CosmeticHatItem`, and
  `client/{HatModel,WearablesClient}`. Deliberately uses its **own** `DeferredRegister` instances
  (not `BroomCloset.ITEMS`), mirroring the modular shape the standalone mod already had rather than
  folding it into the wizard's single registry.
- **JEI: dependency scaffolding only, no custom plugin** — also the user's explicit call. Added
  `compileOnly`/`runtimeOnly` on `mezz.jei:jei-${minecraft_version}-{common,neoforge}-api` /
  `-neoforge`, version `19.27.0.344` (verified live against `maven.blamejared.com` — training data
  is not trustworthy for exact JEI build numbers), plus a `maven.blamejared.com` repo block and an
  `optional`/`AFTER`/`CLIENT` dependency entry in `neoforge.mods.toml`, mirroring the existing FTB
  Quests optional-dependency pattern exactly. No plugin class exists or is needed — vanilla
  crafting recipes surface in JEI automatically.
- **Template cleanup:** removed `EXAMPLE_BLOCK`, `EXAMPLE_BLOCK_ITEM`, `EXAMPLE_ITEM`,
  `EXAMPLE_TAB`, and the `addCreative` listener from `BroomCloset.java` (the user asked to remove
  "example block" and "example item"; `EXAMPLE_TAB` was removed too since it existed solely to
  host `EXAMPLE_ITEM` and would've been dead/broken without it — not something the user asked for
  by name, but there was no reasonable way to leave it intact). Kept the generic
  `BLOCKS`/`ITEMS`/`CREATIVE_MODE_TABS` `DeferredRegister` declarations and their `.register(bus)`
  calls even though they're now empty — that's reusable scaffolding, not "example"-specific, and
  wasn't part of what was asked. Also dropped the now-orphaned `itemGroup.broom_closet`/
  `block.broom_closet.example_block`/`item.broom_closet.example_item` lang keys.

### Files touched this session

- `build.gradle` — added `maven.blamejared.com` repo; JEI `compileOnly`/`runtimeOnly` deps
- `gradle.properties` — added `jei_version=19.27.0.344`
- `src/main/templates/META-INF/neoforge.mods.toml` — added optional `jei` dependency block
- `src/main/java/heykatu/broom_closet/BroomCloset.java` — wired in `Wearables.register(...)`;
  later, removed all example block/item code + now-unused imports
- `src/main/java/heykatu/broom_closet/wearables/{Wearables,WearableArmorMaterials,WearableItems,
  CosmeticHatItem}.java` and `wearables/client/{HatModel,WearablesClient}.java` — new, ported from
  the standalone mod
- `src/main/resources/assets/broom_closet/models/{armor,item}/*.json` (16 files, new) — hat models,
  namespace rewritten
- `src/main/resources/assets/broom_closet/textures/broom_closet/armor/*.png` (8 files, new)
- `src/main/resources/assets/minecraft/atlases/blocks.json` (new) — stitches hat textures into the
  block atlas (`HatModel` samples the block atlas, not the armor-layer atlas)
- `src/main/resources/data/broom_closet/recipe/*.json` (8 files, new — see Bugs below for why it's
  `recipe/` singular, not `recipes/`) — namespace-rewritten crafting recipes
- `src/main/resources/data/broom_closet/tags/items/wearables.json` (new)
- `src/main/resources/assets/broom_closet/lang/en_us.json` — added 8 hat entries; later, removed
  the 3 orphaned example-block/item/tab entries

### Bugs found and root causes

1. **Hats rendered fine but had no crafting recipes, with zero errors in any log** (`latest.log`,
   `debug.log`, or the dedicated `run/logs/kubejs/*.log`) — the single most important finding this
   session. Root cause: **Minecraft 1.21 renamed several datapack folders** as part of a
   data-driven-content flattening — `recipes` → `recipe`, `advancements` → `advancement`,
   `loot_tables` → `loot_table`, `structures` → `structure` (confirmed by extracting the actual MC
   1.21.1 client jar from the `neoformruntime` Gradle cache and listing its `data/minecraft/`
   top-level folder names directly — do not trust memory on datapack folder names across MC
   versions, verify against the real jar). The copied-over witchywearables source still used the
   pre-1.21 plural `recipes/` folder (likely a leftover from before that mod was ported to 1.21.1).
   Since the folder name matched nothing the game looks for, **the recipe files were never even
   attempted to parse** — not a parse failure, just silently absent from consideration, which is
   exactly why no error appeared anywhere: `RecipeManager` logged "Loaded 1297 recipes" (everyone
   else's, correctly-named) with a completely clean bill of health, while item registration (pure
   Java, unrelated to datapacks) succeeded independently, so the items still rendered. Also ruled
   out along the way, in order: recipe JSON schema validity (byte-diffed against a real vanilla
   `leather_helmet.json` recipe and a known-working recipe the user supplied from another mod —
   identical shape); `ItemStack.STRICT_CODEC`'s `count` field being accidentally required (checked
   the actual decompiled codec — it's `.orElse(1)`, genuinely optional); KubeJS's
   `RecipeManagerMixin#catchFailingRecipes` silently eating a bad recipe (checked all three kubejs
   log files — 0 errors/warnings, so nothing was even offered to that catch). **Fix:** renamed
   `src/main/resources/data/broom_closet/recipes/` → `.../recipe/` (singular). Confirmed by the
   user in-game afterward: "it works!"
2. **Gradle failed to apply `net.neoforged.moddev` under the default Java 11** on this machine —
   same class of issue noted in the previous FTB session's notes (point 3 there). Worked around
   per-invocation with `JAVA_HOME="C:/Program Files/Java/jdk-21"` rather than changing anything
   global, since this repo (unlike the sibling `witchywearables` repo, which pins
   `org.gradle.java.home` in its own `gradle.properties`) doesn't set this itself.
3. **Typo'd Gradle property reference:** the JEI dependency lines were drafted using
   `${mc_version}`, copied verbatim from witchywearables' own `build.gradle` (which does define a
   property by that name) — but Broom Closet's `gradle.properties` calls the equivalent property
   `minecraft_version`. Caught immediately by the first real `compileJava` attempt
   ("Could not get unknown property 'mc_version'"); fixed by using the project's actual property
   name.

### Key decisions

- **`broom_closet:` namespace over `witchywearables:`** — explicit user choice via
  `AskUserQuestion`, prioritizing "one mod, one namespace" over save-compat with any world that
  already had the standalone witchywearables mod installed. If a pre-existing Cottage Witch 2 world
  ever had witchywearables installed, those old `witchywearables:*` item stacks will not
  auto-migrate to the new `broom_closet:*` IDs — worth flagging to the user if that scenario comes
  up later.
- **JEI scaffolding only, no custom category/plugin** — explicit user choice; the standalone mod's
  checked-out source (v1.2.0) had no JEI code at all to port in the first place; vanilla crafting
  recipes need nothing extra to show up in JEI.
- Kept `BroomCloset.BLOCKS`/`ITEMS`/`CREATIVE_MODE_TABS` as empty-but-present scaffolding after the
  example-content removal, rather than deleting them too — they're generic wizard infrastructure,
  not "example"-named content, and removing them wasn't asked for.

### Lessons learned

- **MC 1.21+'s datapack folder renames are a silent trap when copying data from an older mod's file
  layout.** No error, no log line, nothing — the game simply never looks in the old folder name. If
  a recipe/loot table/advancement "does nothing" with a completely clean log, check the actual
  folder names the target MC version's client jar ships data under (extract straight from the
  `neoformruntime`/NeoGradle Gradle cache — `unzip -l` the client artifact jar and look at
  `data/minecraft/*`) rather than trusting an existing mod's source tree at face value, especially
  if that source may predate the mod's own port to the current MC version.
- **A content grep for old-namespace references (`grep -rl "oldns:"`) does not catch folder-name
  bugs.** This session's earlier verification pass (before the user reported the missing-recipes
  symptom) confirmed every in-file reference was correctly rewritten to the new namespace and
  declared the merge complete — but a folder being named `recipes/` instead of `recipe/` isn't a
  namespace reference at all, so that grep-based check gave false confidence. Directory *names*
  need their own explicit check against the target MC version's real conventions, separate from
  checking file *contents*.
- **This modpack's KubeJS build has a `RecipeManagerMixin` (`catchFailingRecipes`) that silently
  swallows genuinely malformed recipes with no crash and no obvious log line** (its injectors carry
  `doesn't use its CallbackInfo` / "0 overrides" mixin-loading debug lines, not the failure itself —
  the actual swallow point wasn't directly observed this session since it never fired). Worth
  remembering as a *candidate* explanation next time a recipe "does nothing" with no error — but
  confirm by checking `run/logs/kubejs/server.log` for its error/warning counts before assuming
  that's the cause, since this session it turned out not to be.
- Compiling (`compileJava`) and processing resources (`processResources`) both succeeding is
  necessary but **not sufficient** to prove a merge is functionally correct — Gradle doesn't
  validate datapack folder-name conventions, JSON schema-vs-target-MC-version compatibility, or
  namespace consistency beyond what the Java compiler and a raw file copy check. The only real
  confirmation came from an actual client run + user playtest.

### Known issues

None new this session. The recipe bug above was found and fixed within the same session, confirmed
working by the user. The chapter-tab hover-highlight issue from the previous FTB Quests session
(`chapterHoverHighlightColor`) remains unresolved and untouched — see that session's "Known issues"
section above.

### Not yet verified

The template-cleanup pass (removing `EXAMPLE_BLOCK`/`EXAMPLE_ITEM`/`EXAMPLE_TAB` and their lang
keys) compiles clean but has **not** been re-confirmed with a fresh client launch — low risk since
it only deleted unused wizard boilerplate and touched no wearables-related code, but worth a quick
sanity check (does the game still start, is the "Example Mod Tab" gone from the creative inventory,
do the hats still work) before considering this fully closed out.

---

## Session 2026-07-01 (3) — Runic identification (obfuscated loot enchantments)

### What was accomplished

Designed and implemented a full feature: pre-enchanted loot (chests, fishing, mob drops) has its
enchantment tooltip text obfuscated in a runic font until the player identifies it. The
enchantments are real/functional from the moment the item is found — only the tooltip *text* is
obfuscated. Curses and normal enchantments render in one uniform color while obfuscated (normally
vanilla splits them red/gray). Three identification paths: wear an equippable item for a
configurable duration, use a new "identification rune" item (offhand + right-click on the
mainhand item), or right-click an obfuscated item onto an enchanting table.

Went through plan mode first: researched Runelic (Darkhax, `runelic:runelic` font) vs vanilla's
`minecraft:alt` (Standard Galactic Alphabet, the same font vanilla already uses for enchanting-table
hint text) via WebSearch/WebFetch rather than trusting training data on a specific third-party
mod's license/API/current version. User decided: use **both** — Runelic if installed
(soft/optional dependency, `compileOnly`/`runtimeOnly`, `ModList.get().isLoaded("runelic")` check
at runtime), vanilla `alt` as fallback — plus a config toggle for the whole feature (default on).
A Plan subagent then designed the implementation by actually decompiling and reading this
project's real NeoForge 21.1.233 / vanilla 1.21.1 sources (see Lessons) rather than working from
general API knowledge, which is what surfaced the correct mixin target before any code was written.

**New package `heykatu.broom_closet.identification`:**
- `IdentificationDataComponents` — one presence-only `UNIDENTIFIED` marker `DataComponentType<Unit>`,
  network-synchronized (needed client-side by the tooltip mixin).
- `IdentificationConfig` (STARTUP: `enabled`, `runeMaxDurability`) + new fields split into the
  existing `Config` (COMMON: `identificationWearTimeSeconds`, `identificationObfuscateBooks`) and
  `ClientConfig` (CLIENT: `unidentifiedColor`) — see Key decisions for why split this way.
- `IdentificationRuneItem` / `IdentificationItems` — offhand-held identify item; durability is
  config-driven (`0` = unlimited uses, no durability bar at all; `Item.Properties#durability(n)`
  only applied when `n > 0`, read at registration time since `RUNE_MAX_DURABILITY` is STARTUP).
- `loot/ObfuscateLootEnchantmentsModifier` + `IdentificationLootModifierSerializers` +
  `loot/IdentificationLootModifierProvider` — a NeoForge Global Loot Modifier (data-driven,
  `conditions: []`) that tags any loot-generated stack already carrying enchantments as
  `UNIDENTIFIED`. Runs at loot-generation time only, so player-applied enchanting-table/anvil
  enchantments are naturally never touched — no exclusion logic needed, it falls out of where the
  hook lives.
- `WearTimeTracker` — `PlayerTickEvent.Post`, in-memory `Map<UUID, EnumMap<EquipmentSlot,Progress>>`,
  armor slots only (`EquipmentSlot#isArmor()`). Plays a quieter/higher-pitched variant of the
  identify sound (`0.6F`/`1.2F` vs. the other two paths' `1.0F`/`1.0F`) since this one fires
  passively while the player's doing something else, not from a deliberate action.
- `IdentificationInteractions` — handles **both** the offhand-rune-on-mainhand-gear gesture and the
  right-click-onto-enchanting-table gesture, via `PlayerInteractEvent.RightClickBlock` **and**
  `RightClickItem` (see Bugs #2 and #3 below for why both events, and why plain
  `setUseBlock`/`setUseItem(FALSE)` wasn't sufficient).
- `IdentificationCommands` — `/broomcloset identify` / `/broomcloset obfuscate`, gamemaster-level
  (2) debug commands that force-toggle `UNIDENTIFIED` on the executing player's main-hand item, for
  exercising the mechanic without needing real pre-enchanted loot.
- `mixin/ItemStackTooltipMixin` + `client/ObfuscationTextUtil` — `@WrapOperation` (MixinExtras,
  already on the classpath via the existing FTB Quests mixin) wrapping `ItemStack#getTooltipLines`'s
  two calls to `addToTooltip(DataComponents.STORED_ENCHANTMENTS, ...)` (ordinal 2) and
  `addToTooltip(DataComponents.ENCHANTMENTS, ...)` (ordinal 3). `ObfuscationTextUtil` does **not**
  restyle the real enchantment text — see Bug #4 / Key decisions for why that changed mid-session.
- `mixin/AttributeTooltipMixin` — wraps NeoForge's `AttributeUtil.getSortedModifiers`, a completely
  separate tooltip-generation path (see Bug #5) that renders enchantment-granted attribute-modifier
  lines (e.g. Respiration's oxygen bonus) outside `ItemStackTooltipMixin`'s reach entirely.

### Bugs found and root causes

1. **Client/server-load crash: `RuntimeException: Detected config file conflict on
   broom_closet-startup.toml`.** Root cause: `ModConfig.Type.STARTUP`'s *default* config file name
   is derived from modid+type only, not modid+spec — so registering a second, distinct
   `ModConfigSpec` as STARTUP (`IdentificationConfig.SPEC`, alongside the pre-existing
   `WearablesConfig.SPEC`) collided on the same default filename the instant both were registered
   in `BroomCloset`'s constructor. Confirmed via `javap` against the real `ModContainer` class (no
   sources jar available for FancyModLoader in the local Gradle cache, so decompiled facts weren't
   available here — verified the fix via the compiled class's method signatures instead) that a
   3-arg `registerConfig(Type, IConfigSpec, String fileName)` overload exists specifically for this.
   **Fix:** pass an explicit distinct file name (`"broom_closet-identification-startup.toml"`) for
   the second STARTUP spec. **This will recur** for any future 3rd+ STARTUP-gated feature in this
   mod — the fix pattern (explicit filename argument) needs to be reused, not just this one spot.
2. **Offhand rune silently did nothing whenever the unidentified item was equippable (i.e. almost
   always) — found by the user in-game.** Root cause: the original design put the identify logic in
   `IdentificationRuneItem#use()`, only acting when held in the offhand. But right-clicking
   dispatches to the **main-hand** item's `Item#use()` first, and armor's own `use()` override
   equips it and returns a non-`PASS` result — ending the interaction before the offhand item's
   `use()` was ever reached. This was actually flagged as a risk during planning ("if the main-hand
   item has its own use() behavior... that may consume the click before the offhand rune fires")
   but under-weighted, since it turned out to apply to nearly every real target of this feature
   (equippable gear), not just an edge case. **Fix:** moved all identify-by-rune logic out of
   `Item#use()` entirely, into `PlayerInteractEvent.RightClickBlock`/`RightClickItem` handlers in
   `IdentificationInteractions` — both fire *before* vanilla dispatches to `Item#use()`. Hooked both
   events, not just one, since which one fires for a given click depends on whether the player is
   looking at a block within reach.
3. **Right-clicking armor onto the enchanting table both identified it AND equipped it on the same
   click — found by the user in-game, after fix #2 above.** Root cause: `setUseBlock`/
   `setUseItem(TriState.FALSE)` alone don't stop the interaction chain. Per `RightClickBlock`'s own
   javadoc, if the result of the click's block/item handling is `PASS` — which it still was with
   only those two flags cleared — the game proceeds to fire `RightClickItem` next, which is what
   dispatches to `Item#use()`. So the now-identified armor equipped itself right after, via the
   exact fallthrough that fix #2 was supposed to prevent (fix #2 only added the *event hooks*, it
   didn't address this specific fallthrough case, since the offhand-rune path hadn't hit it yet in
   testing). **Fix:** explicitly cancel the event (`setCanceled(true)`) *and* set
   `setCancellationResult(InteractionResult.SUCCESS)` — a non-`PASS` result is what actually stops
   the "proceed to `RightClickItem`" fallthrough, not the cancellation flag by itself. Applied to
   both the table-identify branch and the offhand-rune-on-a-block branch (the latter had the same
   latent bug, just hadn't been exercised yet).
4. **Not a crash, but a real design flaw the user caught by inspection, not testing: obfuscated
   enchantment names were guessable from word count/character count alone.** The original
   `ObfuscationTextUtil` re-styled the *real* enchantment `Component` (font + color only), which
   preserves the real name's word count and per-word length. Since curse names are consistently 3
   words ("Curse of Binding", "Curse of Vanishing"), an unidentified item's word count alone was
   enough to guess curse-vs-not with near certainty, without ever identifying it — undermining the
   one thing the mechanic most needs to keep uncertain. **Fix (after discussing tradeoffs with the
   user first — see Key decisions):** switched to generating deterministic flavor-word gibberish
   (seeded from a hash of the real line's rendered text, so the same enchantment+level always looks
   the same) instead of transliterating the real name — mirrors vanilla's own `EnchantmentNames`
   class, discovered during the original font research, which does the same thing for the same
   reason for enchanting-table hint text.
5. **Enchantment-granted attribute bonuses (e.g. Respiration's oxygen bonus) leaked through a
   tooltip line `ItemStackTooltipMixin` never touched — found by the user in-game.** Root cause:
   `ItemStack#getTooltipLines` calls NeoForge's `AttributeUtil.addAttributeTooltips(...)`
   immediately after the `LORE` line — a completely separate code path from
   `addToTooltip(DataComponents.ENCHANTMENTS, ...)`, which is the only call `ItemStackTooltipMixin`
   wraps. That method renders a "+X `<Attribute>`" line for *every* attribute modifier on the stack,
   including ones contributed by enchantments via `EnchantmentHelper.forEachModifier` — with no
   distinction retained between "the item's own base stat" and "an enchantment granted this."
   **Fix:** new `AttributeTooltipMixin` wraps `AttributeUtil.getSortedModifiers` and, for
   unidentified stacks, separately computes the set of `AttributeModifier` ids contributed by
   `EnchantmentHelper.forEachModifier` and removes just those entries — leaving the item's own base
   modifiers (Attack Damage/Speed, etc.) visible. Deliberately does **not** touch
   `EnchantmentHelper.forEachModifier` itself, since that's also what applies the enchantment's real
   stat bonus on equip — suppressing it would have silently disabled the enchantment's actual
   gameplay effect while unidentified, not just hidden its tooltip.

### Key decisions

- **`ItemTooltipEvent` was ruled out in favor of a mixin, with a concrete reason, not just
  preference.** The event fires *after* `ItemStack#getTooltipLines` has already flattened every
  data-component's contribution into one `List<Component>`, with no record of which line came from
  which component — reconstructing "which lines are the enchantment lines" from that flat list
  would mean re-deriving `Enchantment.getFullname(...)` output and string-matching, which is
  fragile. The mixin wraps `addToTooltip(...)` itself, which is the one place that still has both
  the `ItemStack` (to check `UNIDENTIFIED`) and the per-component `Consumer<Component>` before
  merging.
- **`OBFUSCATE_BOOKS` config is only consulted at loot-tagging time (server-side GLM), not again
  in the client tooltip mixin.** Once `UNIDENTIFIED` is set on a stack (network-synced), its mere
  *presence* is sufficient signal for the client to obfuscate that stack's tooltip lines —
  re-checking the config client-side would risk a client/server config mismatch silently
  suppressing obfuscation the server already committed to. This wasn't explicitly spelled out in
  the plan and was simplified during implementation — worth remembering if this feature is
  revisited, since it means the config is asymmetric (affects tagging only, not rendering).
- **Wear-time same-stack tracking uses reference identity (`current.stack != stack`), not content
  equality (`isSameItemSameComponents`).** Deliberate: `ItemStack#set`/`remove` mutate a stack's
  component patch *in place*, so durability damage taken while an armor piece is worn (e.g. from
  combat) does not change its Java object identity — but it *would* change `isSameItemSameComponents`
  equality (the `DAMAGE` component differs), which would have falsely reset wear-time progress on
  every hit taken while wearing an obfuscated armor piece. Verified this reasoning against the real
  `ItemStack#set` source before committing to reference identity over the more "obviously correct"
  looking content-equality check.
- **Wear-time tracking is in-memory only, resets on unequip** (explicit user choice over a
  stack-persisted counter) — no per-tick `ItemStack` component write/network resync cost, at the
  cost of requiring continuous wear (no credit for cumulative-but-interrupted wear, and no survival
  across a server restart mid-progress).
- Rune identification suppresses the enchanting table's own GUI on the identifying click
  (`TriState.FALSE`), treating it as a dedicated gesture rather than a normal table interaction —
  explicit user choice.
- **Obfuscated text is randomized flavor gibberish, not a font-substituted transliteration of the
  real name — decided via explicit discussion with the user before touching any code.** The user
  noticed word/character count leaked curse-vs-not with near certainty and asked whether that was
  fun or just annoying before any change was made. Judgment call: it's not fun, because it requires
  zero engagement with the actual runes (pure word-counting, solvable once from a wiki, forever) and
  it's *worst* for curses specifically — the one binary a player most wants uncertainty about. Fixed
  by switching to seeded flavor gibberish (see Bug #4) rather than any narrower patch (e.g. hiding
  the level suffix alone would not have fixed the curse-word-count leak by itself).

### Lessons learned

- **Decompiling the project's own real dependency sources (not just "a" NeoForge sources jar found
  online) is what caught the correct mixin ordinals before any code was written, not after a failed
  build.** The Plan subagent located and read `~/.gradle/caches/neoformruntime/intermediate_results/
  sourcesAndCompiledWithNeoForge_*.jar` (the merged vanilla+NeoForge decompile ModDevGradle already
  produced for *this exact* `neo_version=21.1.233`), confirming `ItemStack#getTooltipLines` calls
  `addToTooltip` in the fixed order `JUKEBOX_PLAYABLE(0), TRIM(1), STORED_ENCHANTMENTS(2),
  ENCHANTMENTS(3), DYED_COLOR(4), LORE(5)` — this was independently re-verified in the main
  conversation (not just trusted from the subagent's report) by re-extracting the same jar and
  grepping the actual line numbers before writing the mixin. The ordinals held: `./gradlew runData`
  showed `Mixing ItemStackTooltipMixin ... into net.minecraft.world.item.ItemStack` with no
  injector errors, meaning `defaultRequire: 1` was satisfied on the first try.
- **`./gradlew runData` is a cheap smoke test for "does this mixin's injection target actually
  resolve" — but only for classes that actually get loaded during data generation.** `ItemStack`
  gets loaded (and therefore Mixin-transformed) during data generation even though tooltips are
  never rendered in that run, so `ItemStackTooltipMixin` was genuinely verified this way. **Caveat
  found this round:** `AttributeUtil` is a much narrower, tooltip-rendering-only class that never
  gets touched during a normal `runData` pass at all — `AttributeTooltipMixin` silently went
  unverified (no "Mixing..." log line, no error either) until a throwaway forced call
  (`AttributeUtil.applyModifierTooltips(ItemStack.EMPTY, ...)`, added temporarily to `gatherData`
  and removed right after) was added specifically to force the class to load. It then threw an NPE
  from *inside the original vanilla/NeoForge method* (needs `Minecraft.getInstance()`, unavailable
  in a data-gen context) — which was actually the useful signal: the stack trace showed the mixin's
  wrapper method executing and correctly calling through to the real implementation before that
  unrelated NPE, confirming the injection point itself was sound. Lesson: don't assume `runData`
  exercises a mixin just because the build+datagen succeeded silently — check the log for an actual
  "Mixing X from ... into Y" line for that specific mixin before trusting it.
- **Not every NeoForge/FML class has a sources jar in the local Gradle cache** — `net.neoforged.fml.
  ModContainer` (FancyModLoader) had none, unlike `net.neoforged.neoforge.*` classes. `javap` against
  the compiled `loader-4.0.42.jar` directly (method signatures only, no bytecode-level detail) was
  enough to confirm the 3-arg `registerConfig` overload existed and fix the STARTUP-filename-collision
  bug without needing full decompilation.
- Reconfirmed the pattern from the wearables session: WebSearch+WebFetch for a specific third-party
  mod's current facts (Runelic's license terms, exact font resource ID, latest Maven version
  `21.1.5` as of 2026-07-01) beats trusting training data, and confirming the existing
  `maven.blamejared.com` repo (already there for JEI) also hosts Runelic meant zero new Gradle
  repository entries were needed.
- **`PlayerInteractEvent.RightClickBlock`'s `setUseBlock`/`setUseItem(TriState.FALSE)` do not, by
  themselves, stop the interaction from proceeding to `RightClickItem` afterward.** Per the event's
  own javadoc, that fallthrough is gated on whether the *result* of the click (the block/item
  handling chain, or the cancellation result if cancelled) equals `PASS` — and the default
  `cancellationResult` is `PASS`. Clearing the two `TriState` flags (or even calling
  `setCanceled(true)`, which just clears those same two flags internally) doesn't change what
  `cancellationResult` is. The only way to actually stop the fallthrough is to explicitly call
  `setCancellationResult(InteractionResult.SUCCESS)` (or any non-`PASS` value) in addition to
  cancelling. This is easy to get wrong silently — the click still visually "worked" (GUI suppressed,
  item identified), so the bug (armor also equipping) only surfaced because the *next* thing in the
  chain (`Item#use()`) had its own independent, visible side effect. Worth checking for this same
  class of bug in any other `RightClickBlock` handler that cancels/suppresses without also touching
  `cancellationResult`.
- **Tooltip-line generation for enchantment-granted stat bonuses runs through an entirely separate
  NeoForge code path (`AttributeUtil`) than the plain enchantment-name line**
  (`ItemStack#addToTooltip(DataComponents.ENCHANTMENTS, ...)`). Worth remembering if any other
  "extra descriptive tooltip line tied to an enchantment" surfaces later (this modpack's Respiration
  apparently grants an attribute-modifier-based oxygen bonus, which is what leaked) — it will very
  likely also route through `AttributeUtil.addAttributeTooltips`/`getSortedModifiers`, not through
  anything `ItemStackTooltipMixin` already covers.

### Known issues

**The identification rune item has no model or texture asset.** It will render as the
missing-texture placeholder in-game until `assets/broom_closet/models/item/identification_rune.json`
and a texture PNG are added — this needs actual art, which wasn't something available to produce
in this session. Not a functional bug (the item registers and works mechanically), purely visual.

### Not yet verified

The core mechanic **has** now been playtested in-game by the user (that's how bugs #2, #3, #4, and
#5 above were all found), and everything above is fixed and build-verified as of this writeup. Not
yet re-confirmed in-game since being fixed, moments before this note was written:
- The offhand-rune identify gesture (bug #2's fix) and enchanting-table identify not also equipping
  the item (bug #3's fix).
- The wear-time identify sound (added this round, never tested).
- The switch to flavor-gibberish obfuscated text (bug #4's fix) — not yet seen rendered in-game.
- The attribute-tooltip leak fix (bug #5) — not yet confirmed that Respiration's oxygen bonus (or
  any other enchantment-granted attribute line) is actually hidden now, nor that base item stats
  (e.g. weapon Attack Damage/Speed) are still correctly visible on an otherwise-unidentified item.

---

## Session 2026-07-09 — FTB Chunks optional world-map/minimap disable

### What was accomplished

Built an optional, client-side feature to fully disable FTB Chunks' fullscreen world map, minimap,
and its inventory-screen sidebar icon, via one new `ClientConfig` toggle — while leaving chunk
claiming (a separate FTB Chunks screen/keybind) completely untouched. Followed the same
optional-dependency shape as the existing FTB Quests styling feature (see the first session above),
but for a mod (`ftb-chunks-neoforge`) not previously depended on at all.

**Scaffolding:**
- `build.gradle` — added `dev.ftb.mods:ftb-chunks-neoforge:2101.1.20` (`implementation`,
  compile-time only, same ARR-adjacent treatment as FTB Quests). Deliberately pinned to the
  `2101.x` line rather than the newer `2111.x` — see Key decisions.
- `FTBChunksMixinPlugin` (`heykatu.broom_closet.mixin`) — `IMixinConfigPlugin` gating on
  `LoadingModList.get().getModFileById("ftbchunks") != null`, identical shape to the existing
  `FTBQuestsMixinPlugin`.
- `broom_closet.ftbchunks.mixins.json` — sibling mixin config, `package:
  heykatu.broom_closet.mixin.ftbchunks`, gated by the plugin above.
- `neoforge.mods.toml` template — added the new `[[mixins]]` block + an `optional`/`AFTER`/`CLIENT`
  dependency block for `ftbchunks`.

**The feature itself:**
- `ClientConfig` — new `disableFtbChunksMap` boolean (default `false`), plus a
  `ftbChunksMapDisabled()` helper (`ModList.isLoaded("ftbchunks") && disableFtbChunksMap`),
  mirroring `ftbStylingActive()`'s existing shape exactly.
- `LargeMapScreenMixin` (targets `dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen#openMap`,
  which returns `boolean` in this version) — `@Inject` at `HEAD`, cancellable, sets the return
  value to `false` when the option is on. This is the single choke point every path funnels
  through (the `M` key, the HUD map button, and the claim-manager screen's own "large map"
  button), so one injection covers all three.
- `FTBChunksClientMixin` (targets `dev.ftb.mods.ftbchunks.client.FTBChunksClient#renderHud`) —
  went through two designs this session (see Bugs #3); the shipped version does **not** cancel
  the method. It injects at `HEAD` and forces `FTBChunksClientConfig.MINIMAP_ENABLED.set(false)`
  every frame when the option is on, reusing FTB Chunks' own later, already-tested guard (further
  down the same method) that skips only the visible-panel draw.
- `FtbChunksSidebarOverrides` (`heykatu.broom_closet.ftbchunks`, **not** a mixin) — registers a
  listener on FTB Library's public `SidebarButtonCreatedEvent`, adds a visibility condition
  (`() -> !ClientConfig.ftbChunksMapDisabled()`) to the `ftbchunks:chunks` button (FTB Chunks' own
  inventory-screen sidebar icon), via the public `RegisteredSidebarButton.addVisibilityCondition`
  API. No bytecode transform needed for this one at all — same "not a mixin" shape as the existing
  `FtbTextColorOverrides`.
- `BroomCloset.java` — wired `FtbChunksSidebarOverrides.register()` into `ClientModEvents
  .onClientSetup`, guarded by `ModList.get().isLoaded("ftbchunks")` (the listener's own lambda
  re-checks `ftbChunksMapDisabled()` on every evaluation, so the registration itself doesn't need
  to be re-gated on the config value too).

All four pieces (world map hidden, minimap hidden, claim-screen background intact, sidebar icon
hidden) confirmed working in-game by the user by the end of the session ("that's perfect").

### Bugs found and root causes

1. **First compile attempt failed entirely: `package dev.ftb.mods.ftbchunks.client.gui.map does
   not exist`, `cannot find symbol: class MinimapRenderer`, etc.** Root cause: the mixins were
   initially written against FTB Chunks' GitHub `main` branch, which has diverged substantially
   from `2101.1.20` (the version actually compatible with this project's MC 1.21.1 /
   FTB Library `2101.1.31` pins) — different package layout (`client.gui.LargeMapScreen`, not
   `client.gui.map.LargeMapScreen`), different minimap architecture entirely (minimap logic lives
   directly in `FTBChunksClient#renderHud` in `2101.1.20`; `main` has since split it into a
   separate `MinimapRenderer` class with a different method signature), and `LargeMapScreen#openMap`
   returns `boolean` in `2101.1.20` vs `void` on `main`. **Fix:** located the actual resolved jar in
   the Gradle module cache (`~/.gradle/caches/modules-2/files-2.1/dev.ftb.mods/
   ftb-chunks-neoforge/2101.1.20/.../ftb-chunks-neoforge-2101.1.20.jar`) and used `javap -p`/
   `javap -c -p` against the real bytecode to get correct class names, packages, and method
   signatures, rather than trusting GitHub source for a dependency pinned to an older release line.
2. **`GL_OUT_OF_MEMORY` spam during the loading-screen→main-menu transition, on a test launch with
   FTB Chunks in the mods folder — initially suspected by the user to be caused by this session's
   mixin changes ("I am almost 100% certain the issue is our code somehow").** Root cause: **not**
   the mod at all — an earlier `runClient` process (still running, its own active session) was
   competing for GPU/VRAM with a second, newly-launched client, and the loading→menu transition is
   exactly when vanilla uploads its single largest fresh texture batch (the title-screen panorama),
   making it the natural point for a VRAM ceiling from two simultaneous clients to surface.
   Confirmed by closing the older session and retesting clean. Before landing on that explanation,
   `FTBChunksClient#renderHud`/`#clientTick`/`#screenOpened`/`#reloadShaders` bytecode was
   disassembled directly and confirmed to all safely no-op without a loaded world (`mc.player`/
   `mc.level` null checks) — ruling out both this session's mixin and FTB Chunks' own per-frame
   logic as the source *before* looking for an environmental cause.
3. **Chunk-claim-manager screen's map background went blank once the option was enabled — found
   by the user in-game.** Root cause: the first `FTBChunksClientMixin` design cancelled the whole
   `renderHud()` method at `HEAD`, which also prevented the lazy `minimapTextureId` generation/
   refresh that happens earlier in that same method — and `ChunkScreenPanel#drawBackground` reads
   `FTBChunksClient.INSTANCE.getMinimapTextureId()` directly (confirmed via `javap -c`:
   `RenderSystem.setShaderTexture(0, getMinimapTextureId())`) to render its own background, so it
   went blank right alongside the minimap. **Fix:** stopped cancelling the method; instead force
   FTB Chunks' own `MINIMAP_ENABLED` config value off every frame, since `javap -c` on `renderHud`
   showed FTB's own code already has a second, later guard — right after the texture-refresh
   section — that checks exactly this flag before drawing the visible panel. Reused that existing
   split instead of re-implementing it via a fragile injection into the middle of a 1500+
   instruction method.
4. **Client crash applying the bug-#3 fix: `InvalidInjectionException: Invalid descriptor... Expected
   (...;CallbackInfo;)V but found (...)V`.** Root cause: dropped the `CallbackInfo` parameter from
   the new non-cancelling `@Inject`, assuming it was optional when nothing gets cancelled — Mixin
   0.8.7 requires it unconditionally, and `compileJava` doesn't catch this (the descriptor is only
   validated when Mixin actually applies the transform, in-game). **Fix:** added the (unused)
   `CallbackInfo` parameter back.

### Key decisions

- **Pinned `ftb-chunks-neoforge` to `2101.1.20`, not the newer `2111.x` line.** `2101.1.20`'s own
  published POM declares `ftb-teams-neoforge:2101.1.9` (exact match with the version already
  pinned for FTB Quests) and `ftb-library-neoforge:2101.1.30` (same minor family as the `2101.1.31`
  already pinned) — `2111.x` pulls in `2111.1.0` of both instead, which would conflict with the
  existing FTB Quests/Library/Teams pins.
- **Force FTB Chunks' own `MINIMAP_ENABLED` flag rather than mixin-splitting `renderHud()`
  ourselves.** `BaseValue#set()` is a plain in-memory field write (confirmed via `javap -c` — no
  disk I/O), so forcing it every frame is cheap and, critically, never persists to FTB Chunks' own
  saved config file — the user's real FTB Chunks settings are untouched if this mod's option is
  later turned off.
- **Sidebar icon hidden via FTB Library's public `SidebarButtonCreatedEvent` API, not a mixin** —
  same "not a mixin" precedent as `FtbTextColorOverrides`, since this is a deliberately public
  integration point (the `.api.` package), not an internal implementation detail.
- **Chunk claiming left entirely untouched, by design, confirmed via source reading before writing
  any code:** `ChunkScreen`/`CLAIM_MANAGER_KEY` is an independent code path from `LargeMapScreen`/
  `MAP_KEY` — separate keybind, separate screen class, separate click-handler branch in
  `FTBChunksClient#customClick`. Disabling the map was verified not to disable claiming before
  ever touching FTBChunksClient with a mixin.

### Lessons learned

- **A dependency's GitHub `main` branch is not a substitute for the actual resolved jar pinned in
  `build.gradle`** — they can differ substantially in package layout and method signatures even
  for "the same mod," especially across MC-version-encoded release lines (`2101.x` vs `2111.x`
  here). `javap -p` / `javap -c -p` against the real jar in
  `~/.gradle/caches/modules-2/files-2.1/...` is the reliable source of truth; `compileJava` failing
  against a mismatched assumption is what caught this, not a runtime issue.
- **When two unrelated things change at once (a code change *and* an unrelated environmental
  factor), bytecode-level verification of the suspected code path — tracing the actual guard
  conditions, not just re-reading the diff — is worth doing before assuming the code is at fault.**
  It correctly pointed at two overlapping game clients competing for GPU memory here, instead of
  sending the debugging effort in circles on mod code that turned out to be provably safe.
- **A single method doing two logically separate things (refresh backing state + draw visible UI)
  is a trap for "just cancel it" mixin fixes.** Worth checking whether *other* code reads
  intermediate state that method produces (here, `ChunkScreenPanel` reading `minimapTextureId`)
  before fully short-circuiting a method, not just checking what the target method itself does in
  isolation.
- **Mixin's `@Inject` requires a trailing `CallbackInfo`/`CallbackInfoReturnable` parameter
  unconditionally, even when nothing is ever cancelled** — this is only validated when Mixin
  actually applies the transform at runtime, not caught by `compileJava`.
- Reconfirms the running theme from the FTB Quests session: decompiling/`javap`-ing the actual
  pinned dependency version, not a same-mod-different-version source tree, is what catches
  signature mismatches before they become runtime crashes.

### Known issues

None reported. All four pieces of this feature (world map hidden, minimap hidden, claim-screen
background intact, sidebar icon hidden) were confirmed working in-game by the user.

### Not yet verified

- Toggling `disableFtbChunksMap` back to `false` at runtime (does the world map, minimap, and
  sidebar icon all correctly reappear without a full client restart) — not tested this session,
  every test this session went one direction (default `false` → set to `true`).
- Actually claiming a chunk (clicking through the claim-manager UI to completion) while the option
  is enabled — the user confirmed the claim screen *opens* and its background is back, but didn't
  explicitly narrate performing an actual claim action during this session.
- Server-side behavior / multiplayer: this session's testing was all singleplayer-dev-client
  (`runClient`); the option is a client-only (`ModConfig.Type.CLIENT`) toggle so this should be
  inherently per-player, but that hasn't been exercised with more than one client.

---

## Session 2026-07-09 — Config consolidation (StartupConfig merge)

### What was accomplished

User asked to cut down from per-feature config classes to just the mod's three intended buckets
(common/client/server). Audited the actual config classes first: `Config` (COMMON) and
`ClientConfig` (CLIENT) were already properly general-purpose, but `WearablesConfig` and
`IdentificationConfig` were each their own `ModConfig.Type.STARTUP` spec/file, one per feature.

Asked the user how to handle the STARTUP case specifically, since `enabled` on both of those
gates whether the feature's items get registered *at all* — that decision has to be made before
`RegisterEvent` fires, which only STARTUP-type specs support (COMMON/CLIENT load too late to
affect what gets registered). User chose to keep a single combined STARTUP spec as an explicit
4th bucket rather than downgrading those flags to plain runtime toggles.

**Changes:**
- Added `StartupConfig.java` (`heykatu.broom_closet`, top-level) — one `ModConfig.Type.STARTUP`
  spec holding `wearablesEnabled`, `identificationEnabled`, `identificationRuneMaxDurability`.
- Deleted `wearables/WearablesConfig.java` and `identification/IdentificationConfig.java`.
- `BroomCloset.java` — now registers one STARTUP spec (`StartupConfig.SPEC`) instead of two, which
  also removed the explicit-filename workaround (`"broom_closet-identification-startup.toml"`)
  that existed only because two STARTUP specs collided on NeoForge's per-modid (not per-spec)
  default STARTUP filename (see the original bug in the 2026-07-01 (3) identification session).
- Updated all downstream references to the old classes: `mixin/AttributeTooltipMixin.java`,
  `mixin/ItemStackTooltipMixin.java`, `identification/IdentificationInteractions.java`,
  `identification/IdentificationItems.java` (field reads), and a stale comment in
  `identification/Identification.java`.

Confirmed via `./gradlew compileJava` — builds clean (only pre-existing deprecation warnings,
unrelated to this change).

### Key decisions

- **Kept STARTUP as an explicit 4th config bucket instead of forcing a strict 3-file split.**
  Asked the user directly via AskUserQuestion rather than assuming; the alternative (moving
  `enabled`/`runeMaxDurability` into COMMON) would have silently dropped the "fully unregistered
  when disabled" behavior (no memory footprint, invisible to JEI/creative/recipes) for both
  Wearables and Identification, which the original per-feature configs were deliberately built to
  preserve.
- Old per-feature `run/config/*.toml` dev artifacts (`broom_closet-startup.toml`,
  `broom_closet-identification-startup.toml`) were left alone — `run/` is gitignored, and NeoForge
  will just write the new merged spec's keys into `broom_closet-startup.toml` (default STARTUP
  filename) on next launch rather than needing manual cleanup.

### Lessons learned

- When a "consolidate configs" request runs into a feature that has a real architectural reason
  for being separate (here, STARTUP's registration-gating requirement), surface the tradeoff and
  ask rather than silently picking either "honor the request literally" or "keep it separate" —
  both are defensible and the cost of guessing wrong is a real behavior regression (items always
  registered, feature-off no longer memory/JEI-invisible).

### Known issues

None. Not yet verified in a live client launch this session (only `compileJava`, not `runClient`)
— low risk since the change is a pure class-merge/rename with no logic change, but worth a quick
sanity check (does the game still start, do both features' enable/disable toggles in
`run/config/broom_closet-startup.toml` still work) before considering this fully closed out.

---

## `ui-plan.md`

Not applicable to this project — no `ui-plan.md` exists, and this isn't a UI/checklist-tracked
project. Skipping that step.
