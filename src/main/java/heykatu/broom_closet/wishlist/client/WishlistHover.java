package heykatu.broom_closet.wishlist.client;

import net.minecraft.world.item.ItemStack;

// The item currently under the mouse, plus the on-screen position of its icon, is used both to
// resolve what the W-toggle/click gestures act on, and to anchor WishlistFlash's confirmation
// flash at the right ish spot. TODO: make this less janky
public record WishlistHover(ItemStack stack, int x, int y) {
}
