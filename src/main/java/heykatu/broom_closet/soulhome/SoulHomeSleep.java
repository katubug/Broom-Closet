package heykatu.broom_closet.soulhome;

import heykatu.broom_closet.BroomCloset;
import heykatu.broom_closet.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

// Makes sleeping in a soul home actually pass the night. Allows sleepers in the SoulHome to pass the overworld's
// night as well, assuming the sleep percentage gamerule would allow for it
@EventBusSubscriber(modid = BroomCloset.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class SoulHomeSleep {
    private SoulHomeSleep() {}

    private static boolean active() {
        return Config.soulhomeSleep && SoulHomeCompat.isLoaded();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!active()) return;

        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();

        List<ServerPlayer> sleepers = new ArrayList<>();
        List<ServerPlayer> counted = new ArrayList<>();
        boolean anyInSoulHome = false;
        int deepSleeping = 0;

        for (ServerLevel level : server.getAllLevels()) {
            boolean soulHome = SoulHomeCompat.isSoulHome(level);
            if (level != overworld && !soulHome) continue;

            for (ServerPlayer player : level.players()) {
                // Spectators are excluded from the total, matching SleepStatus.update.
                if (player.isSpectator()) continue;
                counted.add(player);
                if (!player.isSleeping()) continue;

                sleepers.add(player);
                anyInSoulHome |= soulHome;
                // isSleepingLongEnough is vanilla's "deep sleep" gate (100 ticks in bed)
                if (player.isSleepingLongEnough()) deepSleeping++;
            }
        }

        // Nothing here that vanilla can't already see for itself.
        if (!anyInSoulHome) return;

        // Vanilla's own overworld check may have already fired earlier this same tick, since it
        // only needs the players standing in the overworld, so check for that rather than running twice
        if (overworld.isDay()) return;

        int percentage = server.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
        if (deepSleeping < Math.max(1, Mth.ceil(counted.size() * percentage / 100.0F))) return;

        if (server.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            long next = overworld.getDayTime() + 24000L;
            // Routed through the same hook vanilla uses (it posts SleepFinishedTimeEvent), so other
            // sleep mods can still adjust the wake-up time the way they would normally.
            overworld.setDayTime(EventHooks.onSleepFinished(overworld, next - next % 24000L, overworld.getDayTime()));
        }

        // Wake everyone across every dimension. Passing true for updateLevelForSleepingPlayers is necessary:
        // the way vanilla does it isn't available for us.
        // ALSO -
        // DO NOT move this above the setDayTime call. stopSleepInBed fires PlayerWakeUpEvent, and
        // sleep mods use that to tell "went to bed and saw the night through" apart from "got up
        // early" by checking whether it's still night at that moment.Vanilla sets the time first for the same reason.
        sleepers.forEach(player -> player.stopSleepInBed(false, true));

        if (server.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && overworld.isRaining()) {
            overworld.resetWeatherCycle();
        }

        // making it so soulhome sleep settings don't steamroll the phantom relief feature from midnght thoughts
        if (Config.soulhomeResetPhantomTimer) {
            counted.forEach(player -> player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)));
        }
    }

    // SoulHome's dimension_type sets fixed_time, which makes Level.isDay() permanently false.
    // Our dimension_type override drops fixed_time, which makes both of these redundant, they
    // are kept for safety net purposes
    @SubscribeEvent
    public static void onCanPlayerSleep(CanPlayerSleepEvent event) {
        if (!active() || event.getProblem() != null) return;
        if (!SoulHomeCompat.isSoulHome(event.getLevel())) return;

        if (event.getEntity().server.overworld().isDay()) {
            event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
        }
    }

    @SubscribeEvent
    public static void onCanContinueSleeping(CanContinueSleepingEvent event) {
        if (!active() || !event.mayContinueSleeping()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!SoulHomeCompat.isSoulHome(player.level())) return;

        if (player.server.overworld().isDay()) {
            event.setContinueSleeping(false);
        }
    }
}
