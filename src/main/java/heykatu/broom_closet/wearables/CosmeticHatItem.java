package heykatu.broom_closet.wearables;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

// client model hookup lives in WearablesClient
public class CosmeticHatItem extends ArmorItem {

    public CosmeticHatItem(net.minecraft.core.Holder<ArmorMaterial> material, Item.Properties properties) {
        super(material, ArmorItem.Type.HELMET, properties);
    }
}
