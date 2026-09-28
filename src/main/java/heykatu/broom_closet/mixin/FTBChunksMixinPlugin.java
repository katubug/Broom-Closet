package heykatu.broom_closet.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
// needs to be outside heykatu.broom_closet.mixin.ftbchunks - Mixin loads the
// configured mixin package specially, and a plugin class can't live inside the
// package it's gating.
public class FTBChunksMixinPlugin implements IMixinConfigPlugin {
    private boolean ftbChunksPresent;

    @Override
    public void onLoad(String mixinPackage) {
        ftbChunksPresent = LoadingModList.get().getModFileById("ftbchunks") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return ftbChunksPresent;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
