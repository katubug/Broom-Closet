package heykatu.broom_closet.mixin.ftbquests;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import heykatu.broom_closet.ClientConfig;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ViewQuestPanel.class)
public abstract class ViewQuestPanelMixin {
    // Component.empty().withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY) cuz
    // re-applying just the color preserves the italic styling.
    @ModifyExpressionValue(
            method = "addWidgets",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;withStyle([Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private MutableComponent broomcloset$modifySubtitleColor(MutableComponent original) {
        if (!ClientConfig.ftbStylingActive()) {
            return original;
        }

        TextColor color = TextColor.parseColor(ClientConfig.questSubtitleColor).result().orElseThrow();
        return original.setStyle(original.getStyle().withColor(color));
    }
}
