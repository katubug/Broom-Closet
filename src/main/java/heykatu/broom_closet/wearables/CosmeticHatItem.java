package heykatu.broom_closet.wearables;

import heykatu.broom_closet.wearables.client.WearablesClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CosmeticHatItem extends ArmorItem {

    public CosmeticHatItem(net.minecraft.core.Holder<ArmorMaterial> material, Item.Properties properties) {
        super(material, ArmorItem.Type.HELMET, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @Nullable HumanoidModel<?> getGenericArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (slot == EquipmentSlot.HEAD) {
                    var model = WearablesClient.getHatModel(stack.getItem());
                    model.updateParent(original);
                    return model;
                }
                return original;
            }
        });
    }
}
