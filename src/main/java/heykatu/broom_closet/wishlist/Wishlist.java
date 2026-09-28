package heykatu.broom_closet.wishlist;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

// Lets the player mark items to keep an eye out for
public class Wishlist {

    public static void register(IEventBus modEventBus) {
        WishlistItems.register(modEventBus);
        modEventBus.addListener(Wishlist::addToCreativeTab);

        // Everything else goes behind WishlistClient's self-registering @EventBusSubscriber(..., CLIENT).
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(WishlistItems.WISHLIST.get());
        }
    }
}
