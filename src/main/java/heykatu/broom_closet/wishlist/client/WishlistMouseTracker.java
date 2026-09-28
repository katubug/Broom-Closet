package heykatu.broom_closet.wishlist.client;

// Last known GUI-space mouse position, refreshed every screen render. Used as a fallback flash
// anchor for JEI ingredient hovers
class WishlistMouseTracker {
    private static int x;
    private static int y;

    private WishlistMouseTracker() {
    }

    static void update(int mouseX, int mouseY) {
        x = mouseX;
        y = mouseY;
    }

    static int x() {
        return x;
    }

    static int y() {
        return y;
    }
}
