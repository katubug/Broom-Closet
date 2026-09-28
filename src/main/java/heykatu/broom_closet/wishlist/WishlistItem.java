package heykatu.broom_closet.wishlist;

import heykatu.broom_closet.wishlist.client.WishlistScreenOpener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Right-click opens the wishlist view/edit screen. The screen call is routed through
// WishlistScreenOpener rather than made here due to dedicated server crashing
public class WishlistItem extends Item {

    public WishlistItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            WishlistScreenOpener.open();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
