package heykatu.broom_closet.ftbchunks;

import dev.ftb.mods.ftblibrary.api.sidebar.SidebarButtonCreatedEvent;
import heykatu.broom_closet.ClientConfig;
import net.minecraft.resources.ResourceLocation;

public class FtbChunksSidebarOverrides {

    private static final ResourceLocation MAP_BUTTON_ID = ResourceLocation.fromNamespaceAndPath("ftbchunks", "chunks");

    public static void register() {
        SidebarButtonCreatedEvent.EVENT.register(event -> {
            if (event.getButton().getId().equals(MAP_BUTTON_ID)) {
                event.getButton().addVisibilityCondition(() -> !ClientConfig.ftbChunksMapDisabled());
            }
        });
    }
}
