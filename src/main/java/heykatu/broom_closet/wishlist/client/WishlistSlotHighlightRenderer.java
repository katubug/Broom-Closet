package heykatu.broom_closet.wishlist.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Glows any slot whose stack is wishlisted, in ANY open container/inventory screen
//
// Also the only place that fires on every screen render regardless of type, so it's where the mouse tracker
// gets refreshed and pending add/remove flashes get drawn
public class WishlistSlotHighlightRenderer {

    // Translucent gold fill tinted overlays
    private static final int HIGHLIGHT_COLOR = 0x80FFD700;

    @SubscribeEvent
    public void onScreenRenderPost(ScreenEvent.Render.Post event) {
        WishlistMouseTracker.update(event.getMouseX(), event.getMouseY());

        if (event.getScreen() instanceof AbstractContainerScreen<?> containerScreen) {
            GuiGraphics gui = event.getGuiGraphics();
            int guiLeft = containerScreen.getGuiLeft();
            int guiTop = containerScreen.getGuiTop();

            for (Slot slot : containerScreen.getMenu().slots) {
                if (!slot.hasItem() || !WishlistData.contains(slot.getItem())) {
                    continue;
                }

                int x = guiLeft + slot.x;
                int y = guiTop + slot.y;
                gui.fill(x, y, x + 16, y + 16, HIGHLIGHT_COLOR);
            }
        }

        WishlistFlash.render(event.getGuiGraphics());
    }
}
