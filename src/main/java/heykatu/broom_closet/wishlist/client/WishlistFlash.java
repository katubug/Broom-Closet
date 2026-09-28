package heykatu.broom_closet.wishlist.client;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

// Brief green/red fade-out overlay confirming a wishlist add/remove, drawn at a fixed screen
// position rather than tracked per-item
public class WishlistFlash {

    private record Entry(int x, int y, boolean added, long startMs) {
    }

    private static final long DURATION_MS = 500;
    private static final int SIZE = 16;
    private static final List<Entry> ACTIVE = new ArrayList<>();

    private WishlistFlash() {
    }

    public static void trigger(int x, int y, boolean added) {
        ACTIVE.add(new Entry(x, y, added, System.currentTimeMillis()));
    }

    public static void render(GuiGraphics graphics) {
        if (ACTIVE.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        ACTIVE.removeIf(entry -> now - entry.startMs() > DURATION_MS);

        for (Entry entry : ACTIVE) {
            float progress = (now - entry.startMs()) / (float) DURATION_MS;
            int alpha = (int) (0xB0 * (1.0F - progress));
            int rgb = entry.added() ? 0x40C040 : 0xC04040;
            int color = (alpha << 24) | rgb;
            fillCircle(graphics, entry.x() + SIZE / 2, entry.y() + SIZE / 2, SIZE / 2, color);
        }
    }

    private static void fillCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = (int) Math.round(Math.sqrt((double) (radius * radius - dy * dy)));
            graphics.fill(centerX - halfWidth, centerY + dy, centerX + halfWidth, centerY + dy + 1, color);
        }
    }
}
