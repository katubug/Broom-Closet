package heykatu.broom_closet.mixin.ftbquests;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import heykatu.broom_closet.ClientConfig;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChapterPanel.ChapterButton.class)
public abstract class ChapterPanelMixin {
    // Color4I.GRAY.withAlpha(192) -- border drawn around the currently-selected chapter tab.
    // Verified via javap: draw() has 3 withAlpha(int) calls in bytecode order RED (translation
    // warning, ordinal 0), GRAY (this one, ordinal 1), WHITE (hover highlight, ordinal 2).
    @ModifyExpressionValue(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ftb/mods/ftblibrary/icon/Color4I;withAlpha(I)Ldev/ftb/mods/ftblibrary/icon/Color4I;",
                    ordinal = 1
            )
    )
    private Color4I broomcloset$modifySelectedBorder(Color4I original) {
        return ClientConfig.ftbStylingActive() ? Color4I.fromString(ClientConfig.chapterSelectedBorderColor) : original;
    }

    // Color4I.WHITE.withAlpha(40) -- hover highlight on a non-selected chapter tab. Ordinal 2 of
    // the same 3 withAlpha(int) calls described above.
    @ModifyExpressionValue(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ftb/mods/ftblibrary/icon/Color4I;withAlpha(I)Ldev/ftb/mods/ftblibrary/icon/Color4I;",
                    ordinal = 2
            )
    )
    private Color4I broomcloset$modifyHoverHighlight(Color4I original) {
        return ClientConfig.ftbStylingActive() ? Color4I.fromString(ClientConfig.chapterHoverHighlightColor) : original;
    }

    // this.chapter.getRawSubtitle().stream().map(line -> ...withStyle(ChatFormatting.GRAY)) --
    // the chapter's own subtitle, shown in its tab's tooltip. This lambda (inside the
    // constructor) compiles to its own synthetic method -- confirmed via javap: "lambda$new$0".
    @ModifyExpressionValue(
            method = "lambda$new$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private MutableComponent broomcloset$modifyChapterSubtitleColor(MutableComponent original) {
        if (!ClientConfig.ftbStylingActive()) {
            return original;
        }

        TextColor color = TextColor.parseColor(ClientConfig.chapterSubtitleColor).result().orElseThrow();
        return original.setStyle(original.getStyle().withColor(color));
    }
}
