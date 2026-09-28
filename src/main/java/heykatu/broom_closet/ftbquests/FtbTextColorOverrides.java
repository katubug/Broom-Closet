package heykatu.broom_closet.ftbquests;

import dev.ftb.mods.ftblibrary.util.TextComponentParser;
import heykatu.broom_closet.ClientConfig;
import net.minecraft.network.chat.TextColor;

public class FtbTextColorOverrides {
    public static void apply() {
        for (String entry : ClientConfig.codeRemaps) {
            char code = entry.charAt(0);
            String hex = entry.substring(2);
            TextColor color = TextColor.parseColor(hex).result().orElseThrow();
            TextComponentParser.CODE_TO_FORMATTING.remove(code);
            TextComponentParser.SPECIAL_COLOR_CODES.put(code, color);
        }
    }
}
