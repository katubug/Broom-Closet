package heykatu.broom_closet.mixin.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Theme;
import heykatu.broom_closet.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Theme.drawStringis the one method every FTB Library/Quests text draw funnels through
@Mixin(Theme.class)
public abstract class ThemeMixin {
    @Redirect(
            method = "drawString(Lnet/minecraft/client/gui/GuiGraphics;Ljava/lang/Object;IILdev/ftb/mods/ftblibrary/icon/Color4I;I)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I"
            )
    )
    private int broomcloset$drawCharSequenceShadow(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        if (!broomcloset$customizeShadow(dropShadow)) {
            return graphics.drawString(font, text, x, y, color, dropShadow);
        }
        if (ClientConfig.ftbTextShadowEnabled) {
            graphics.drawString(font, text, x + ClientConfig.ftbTextShadowOffsetX, y + ClientConfig.ftbTextShadowOffsetY, broomcloset$shadowColor(color), false);
        }
        return graphics.drawString(font, text, x, y, color, false);
    }

    @Redirect(
            method = "drawString(Lnet/minecraft/client/gui/GuiGraphics;Ljava/lang/Object;IILdev/ftb/mods/ftblibrary/icon/Color4I;I)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
            )
    )
    private int broomcloset$drawComponentShadow(GuiGraphics graphics, Font font, Component text, int x, int y, int color, boolean dropShadow) {
        if (!broomcloset$customizeShadow(dropShadow)) {
            return graphics.drawString(font, text, x, y, color, dropShadow);
        }
        if (ClientConfig.ftbTextShadowEnabled) {
            graphics.drawString(font, text, x + ClientConfig.ftbTextShadowOffsetX, y + ClientConfig.ftbTextShadowOffsetY, broomcloset$shadowColor(color), false);
        }
        return graphics.drawString(font, text, x, y, color, false);
    }

    // Catches both plain Strings and the generic String.valueOf(...) fallback for any other Object
    // type Theme#drawString doesn't have a dedicated case for.
    @Redirect(
            method = "drawString(Lnet/minecraft/client/gui/GuiGraphics;Ljava/lang/Object;IILdev/ftb/mods/ftblibrary/icon/Color4I;I)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"
            )
    )
    private int broomcloset$drawStringShadow(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean dropShadow) {
        if (!broomcloset$customizeShadow(dropShadow)) {
            return graphics.drawString(font, text, x, y, color, dropShadow);
        }
        if (ClientConfig.ftbTextShadowEnabled) {
            graphics.drawString(font, text, x + ClientConfig.ftbTextShadowOffsetX, y + ClientConfig.ftbTextShadowOffsetY, broomcloset$shadowColor(color), false);
        }
        return graphics.drawString(font, text, x, y, color, false);
    }

    private static boolean broomcloset$customizeShadow(boolean dropShadow) {
        return dropShadow && ClientConfig.ftbStylingActive();
    }

    // Reproduces vanilla Font's own shadow color formula.
    private static int broomcloset$shadowColor(int color) {
        if (!ClientConfig.ftbTextShadowColor.isEmpty()) {
            return Color4I.fromString(ClientConfig.ftbTextShadowColor).rgba();
        }
        int argb = (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
        int r = (argb >> 16 & 0xFF) / 4;
        int g = (argb >> 8 & 0xFF) / 4;
        int b = (argb & 0xFF) / 4;
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
