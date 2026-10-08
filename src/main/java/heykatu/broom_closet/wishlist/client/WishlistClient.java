package heykatu.broom_closet.wishlist.client;

import heykatu.broom_closet.BroomCloset;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = BroomCloset.MODID, value = Dist.CLIENT)
public class WishlistClient {

    // Defaults to W
    public static final KeyMapping TOGGLE_WISHLIST = new KeyMapping(
            "key.broom_closet.toggle_wishlist", GLFW.GLFW_KEY_W, "key.categories.broom_closet");

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_WISHLIST);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        WishlistData.load();

        // Registered here rather than from Wishlist.register() for dedi server reasons
        NeoForge.EVENT_BUS.register(new WishlistKeyHandler());
        NeoForge.EVENT_BUS.register(new WishlistSlotHighlightRenderer());
        NeoForge.EVENT_BUS.register(new WishlistWorldHighlightRenderer());
    }
}
