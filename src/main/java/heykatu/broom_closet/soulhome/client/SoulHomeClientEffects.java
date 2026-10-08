package heykatu.broom_closet.soulhome.client;

import heykatu.broom_closet.BroomCloset;
import heykatu.broom_closet.ClientConfig;
import heykatu.broom_closet.soulhome.SoulHomeCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

// Replaces SoulHome's registered DimensionSpecialEffects with ours, so thunderstorms can dim the
// soul home
@EventBusSubscriber(modid = BroomCloset.MODID, value = Dist.CLIENT)
public final class SoulHomeClientEffects {
    private SoulHomeClientEffects() {}

    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        if (!SoulHomeCompat.isLoaded() || !ClientConfig.soulhomeStormDarkening) return;

        event.register(SoulHomeCompat.SKY_EFFECTS, new SoulHomeSkyEffects());
    }
}
