package heykatu.broom_closet.wishlist.client.jei;

import heykatu.broom_closet.BroomCloset;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

// only classloaded by JEI's own ServiceLoader scan (see the META-INF/services/mezz.jei.api.IModPlugin registration)
@JeiPlugin
public class WishlistJeiPlugin implements IModPlugin {
    private static volatile IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(BroomCloset.MODID, "wishlist");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    static IJeiRuntime runtime() {
        return runtime;
    }
}
