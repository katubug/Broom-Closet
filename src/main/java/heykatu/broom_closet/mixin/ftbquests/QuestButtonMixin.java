package heykatu.broom_closet.mixin.ftbquests;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import heykatu.broom_closet.ClientConfig;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(QuestButton.class)
public abstract class QuestButtonMixin {
    // shape.getShape().withColor(Color4I.DARK_GRAY) -- the quest icon's shape tint.
    @ModifyExpressionValue(
            method = "draw",
            at = @At(value = "FIELD", target = "Ldev/ftb/mods/ftblibrary/icon/Color4I;DARK_GRAY:Ldev/ftb/mods/ftblibrary/icon/Color4I;")
    )
    private Color4I broomcloset$modifyShapeTint(Color4I original) {
        return ClientConfig.ftbStylingActive() ? Color4I.fromString(ClientConfig.questShapeTintColor) : original;
    }

    // shape.getBackground().withColor(Color4I.WHITE.withAlpha(150)) - the quest icon's background tint
    @ModifyExpressionValue(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ftb/mods/ftblibrary/icon/Color4I;withAlpha(I)Ldev/ftb/mods/ftblibrary/icon/Color4I;",
                    ordinal = 0
            )
    )
    private Color4I broomcloset$modifyBackgroundTint(Color4I original) {
        return ClientConfig.ftbStylingActive() ? Color4I.fromString(ClientConfig.questBackgroundTintColor) : original;
    }

    // description.copy().withStyle(ChatFormatting.GRAY) in addMouseOverText -the quest's
    // subtitle as shown in its chapter-map tooltip
    @ModifyExpressionValue(
            method = "addMouseOverText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 1
            )
    )
    private MutableComponent broomcloset$modifyTooltipSubtitleColor(MutableComponent original) {
        if (!ClientConfig.ftbStylingActive()) {
            return original;
        }

        TextColor color = TextColor.parseColor(ClientConfig.questSubtitleColor).result().orElseThrow();
        return original.setStyle(original.getStyle().withColor(color));
    }
}
