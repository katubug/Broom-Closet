package heykatu.broom_closet.soulhome.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

// A copy of SoulHome's own SoulDimensionRenderInfo but constantAmbientLight goes from true to false.
public class SoulHomeSkyEffects extends DimensionSpecialEffects {
    public SoulHomeSkyEffects() {
        super(128.0F, false, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        // Carried over verbatim from SoulHome's version, which took it from the overworld's.
        return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }
}
