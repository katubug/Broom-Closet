package heykatu.broom_closet.soulhome.client;

import heykatu.broom_closet.soulhome.island.SoulIslandOption;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

import java.util.List;

// Keeps Screen out of the common payload class. Same reasoning as WishlistScreenOpener.
public final class SoulIslandPickerOpener {
    private SoulIslandPickerOpener() {}

    public static void open(List<SoulIslandOption.Entry> entries, InteractionHand hand) {
        Minecraft minecraft = Minecraft.getInstance();
        // The client predicted the key use before the server said no; stop the particles.
        if (minecraft.player != null) minecraft.player.stopUsingItem();
        minecraft.setScreen(new SoulIslandPickerScreen(entries, hand));
    }
}
