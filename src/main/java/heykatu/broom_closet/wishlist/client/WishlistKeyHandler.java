package heykatu.broom_closet.wishlist.client;

import heykatu.broom_closet.wishlist.client.jei.WishlistJeiBridge;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.Nullable;

// Resolves whichever item is currently hovered and toggles it in the wishlist. Hooks
// ScreenEvent.KeyPressed rather than polling KeyMapping#isDown() per tick:
// screens capture keyboard input, so tick-polling doesn't fire reliably while one has focus.
public class WishlistKeyHandler {

    @SubscribeEvent
    public void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!WishlistClient.TOGGLE_WISHLIST.matches(event.getKeyCode(), event.getScanCode())) {
            return;
        }

        WishlistHover hover = resolveHover(event.getScreen());
        if (hover == null) {
            return;
        }

        boolean added = WishlistData.toggle(hover.stack().getItem());
        WishlistFlash.trigger(hover.x(), hover.y(), added);
        event.setCanceled(true);
    }

    @Nullable
    private WishlistHover resolveHover(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot slot = containerScreen.getSlotUnderMouse();
            if (slot != null && slot.hasItem()) {
                int x = containerScreen.getGuiLeft() + slot.x;
                int y = containerScreen.getGuiTop() + slot.y;
                return new WishlistHover(slot.getItem(), x, y);
            }
        }

        if (screen instanceof WishlistScreen wishlistScreen) {
            WishlistHover hover = wishlistScreen.getHover();
            if (hover != null) {
                return hover;
            }
        }

        if (ModList.get().isLoaded("jei")) {
            ItemStack jeiHovered = WishlistJeiBridge.getHoveredItemStack();
            if (jeiHovered != null) {
                // No exact icon rect available from JEI's public API - anchor on the cursor
                return new WishlistHover(jeiHovered, WishlistMouseTracker.x() - 8, WishlistMouseTracker.y() - 8);
            }
        }

        return null;
    }
}
