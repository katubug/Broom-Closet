package heykatu.broom_closet.wishlist.client.jei;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// only called from behind a ModList.get().isLoaded("jei") guard (see
// WishlistKeyHandler)
public class WishlistJeiBridge {

    @Nullable
    public static ItemStack getHoveredItemStack() {
        IJeiRuntime runtime = WishlistJeiPlugin.runtime();
        if (runtime == null) {
            return null;
        }

        IIngredientListOverlay overlay = runtime.getIngredientListOverlay();
        return overlay.getIngredientUnderMouse()
                .flatMap(ITypedIngredient::getItemStack)
                .orElse(null);
    }
}
