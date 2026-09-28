package heykatu.broom_closet.wearables;

import heykatu.broom_closet.BroomCloset;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WearableItems {

    private static final DeferredRegister<Item> REGISTER =
            DeferredRegister.create(Registries.ITEM, BroomCloset.MODID);

    public static final DeferredHolder<Item, CosmeticHatItem> CAT_EARS =
            REGISTER.register("cat_ears", () -> new CosmeticHatItem(WearableArmorMaterials.EARS, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, CosmeticHatItem> DEMON_HORNS =
            REGISTER.register("demon_horns", () -> new CosmeticHatItem(WearableArmorMaterials.DEMON_HORNS, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, CosmeticHatItem> DRUID_HORNS =
            REGISTER.register("druid_horns", () -> new CosmeticHatItem(WearableArmorMaterials.DRUID_HORNS, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, CosmeticHatItem> ELF_EARS =
            REGISTER.register("elf_ears", () -> new CosmeticHatItem(WearableArmorMaterials.EARS, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, CosmeticHatItem> FROG =
            REGISTER.register("frog", () -> new CosmeticHatItem(WearableArmorMaterials.FROG, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, CosmeticHatItem> FROG_TOP_HAT =
            REGISTER.register("frog_top_hat", () -> new CosmeticHatItem(WearableArmorMaterials.TOP_HAT, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, CosmeticHatItem> GOBLIN_EARS =
            REGISTER.register("goblin_ears", () -> new CosmeticHatItem(WearableArmorMaterials.EARS, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, CosmeticHatItem> ANTLERS =
            REGISTER.register("antlers", () -> new CosmeticHatItem(WearableArmorMaterials.ANTLERS, new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
    }
}
