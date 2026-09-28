package heykatu.broom_closet.wearables;

import heykatu.broom_closet.BroomCloset;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class WearableArmorMaterials {

    // One dummy layer so HumanoidArmorLayer's per-layer loop fires and calls renderToBuffer
    private static final List<ArmorMaterial.Layer> DUMMY_LAYERS =
            List.of(new ArmorMaterial.Layer(Wearables.id("dummy")));

    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, BroomCloset.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EARS =
            ARMOR_MATERIALS.register("ears", () -> new ArmorMaterial(
                    zeroDefense(), 9,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ItemTags.WOOL),
                    DUMMY_LAYERS, 0f, 0f
            ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TOP_HAT =
            ARMOR_MATERIALS.register("top_hat", () -> new ArmorMaterial(
                    zeroDefense(), 9,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ItemTags.WOOL),
                    DUMMY_LAYERS, 0f, 0f
            ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FROG =
            ARMOR_MATERIALS.register("frog", () -> new ArmorMaterial(
                    zeroDefense(), 9,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ItemTags.WOOL),
                    DUMMY_LAYERS, 0f, 0f
            ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DEMON_HORNS =
            ARMOR_MATERIALS.register("demon_horns", () -> new ArmorMaterial(
                    zeroDefense(), 9,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ItemTags.WOOL),
                    DUMMY_LAYERS, 0f, 0f
            ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DRUID_HORNS =
            ARMOR_MATERIALS.register("druid_horns", () -> new ArmorMaterial(
                    zeroDefense(), 9,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ItemTags.WOOL),
                    DUMMY_LAYERS, 0f, 0f
            ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ANTLERS =
            ARMOR_MATERIALS.register("antlers", () -> new ArmorMaterial(
                    zeroDefense(), 9,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ItemTags.WOOL),
                    DUMMY_LAYERS, 0f, 0f
            ));

    private static Map<ArmorItem.Type, Integer> zeroDefense() {
        EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            map.put(type, 0);
        }
        return map;
    }

    public static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }
}
