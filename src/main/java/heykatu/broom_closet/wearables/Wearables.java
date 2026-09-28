package heykatu.broom_closet.wearables;

import heykatu.broom_closet.BroomCloset;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

// Merged in from the standalone Witchy Wearables mod
public class Wearables {

    public static void register(IEventBus modEventBus) {
        WearableArmorMaterials.register(modEventBus);
        WearableItems.register(modEventBus);
        modEventBus.addListener(Wearables::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(WearableItems.CAT_EARS.get());
            event.accept(WearableItems.DEMON_HORNS.get());
            event.accept(WearableItems.DRUID_HORNS.get());
            event.accept(WearableItems.ELF_EARS.get());
            event.accept(WearableItems.FROG.get());
            event.accept(WearableItems.FROG_TOP_HAT.get());
            event.accept(WearableItems.GOBLIN_EARS.get());
            event.accept(WearableItems.ANTLERS.get());
        }
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(BroomCloset.MODID, path);
    }

    public static ModelResourceLocation armorModelId(String name) {
        return ModelResourceLocation.standalone(id("armor/" + name));
    }
}
