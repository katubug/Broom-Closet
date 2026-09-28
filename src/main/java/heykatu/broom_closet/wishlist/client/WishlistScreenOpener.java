package heykatu.broom_closet.wishlist.client;

import net.minecraft.client.Minecraft;

// Exists so WishlistItem can open the screen without naming a client class itself. needed to avoid dedi server
// crashes
public final class WishlistScreenOpener {
    private WishlistScreenOpener() {}

    public static void open() {
        Minecraft.getInstance().setScreen(new WishlistScreen());
    }
}
