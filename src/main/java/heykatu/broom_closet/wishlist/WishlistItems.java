package heykatu.broom_closet.wishlist;

import heykatu.broom_closet.BroomCloset;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WishlistItems {

    private static final DeferredRegister<Item> REGISTER =
            DeferredRegister.create(Registries.ITEM, BroomCloset.MODID);

    public static final DeferredHolder<Item, WishlistItem> WISHLIST =
            REGISTER.register("wishlist", () -> new WishlistItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
    }
}
