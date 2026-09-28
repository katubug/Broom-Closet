package heykatu.broom_closet.soulhome.client;

import heykatu.broom_closet.network.ChooseSoulIslandPayload;
import heykatu.broom_closet.soulhome.island.SoulIslandOption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Carousel of soul home islands shown on a player's first soul key use. Closing it without
// picking sends nothing, so the server just shows it again on the next use.
public class SoulIslandPickerScreen extends Screen {
    private static final ResourceLocation PANEL = ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe");
    private static final WidgetSprites PREV = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/page_backward"),
            ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted"));
    private static final WidgetSprites NEXT = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/page_forward"),
            ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted"));

    private static final int PAD = 8;
    private static final int GAP = 6;
    private static final int ARROW_W = 23;
    private static final int ARROW_H = 13;
    private static final int BUTTON_W = 80;
    private static final int BUTTON_H = 20;
    private static final int DESC_LINES = 3;
    private static final int MAX_PANEL_W = 340;
    // Dark text on the gray panel, like vanilla container screens
    private static final int TITLE_COLOR = 0x404040;
    private static final int NAME_COLOR = 0x404040;
    private static final int DESC_COLOR = 0x555555;
    private static final int NO_PREVIEW_COLOR = 0xAAAAAA;
    private static final int PLACEHOLDER_COLOR = 0xFF202020;

    private final List<SoulIslandOption.Entry> entries;
    private final InteractionHand hand;
    private final Map<ResourceLocation, Boolean> textureExists = new HashMap<>();
    // Survives init() so coming back from the confirm screen keeps the same slide.
    private int index;

    private int panelX, panelY, panelW, panelH;
    private int imageX, imageY, imageW, imageH;
    private int textY;

    public SoulIslandPickerScreen(List<SoulIslandOption.Entry> entries, InteractionHand hand) {
        super(Component.translatable("gui.broom_closet.soul_island.title"));
        this.entries = entries;
        this.hand = hand;
    }

    @Override
    protected void init() {
        int sideW = PAD + ARROW_W + GAP;
        // Everything in the panel except the screenshot itself.
        int chrome = PAD + font.lineHeight + GAP + GAP + font.lineHeight + 4 + DESC_LINES * font.lineHeight
                + GAP + font.lineHeight + GAP + BUTTON_H + PAD;

        imageW = Math.min(this.width - 16, MAX_PANEL_W) - 2 * sideW;
        imageH = imageW * 9 / 16;
        int maxImageH = this.height - 16 - chrome;
        if (imageH > maxImageH) {
            imageH = Math.max(maxImageH, 36);
            imageW = imageH * 16 / 9;
        }

        panelW = imageW + 2 * sideW;
        panelH = imageH + chrome;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        imageX = panelX + sideW;
        imageY = panelY + PAD + font.lineHeight + GAP;
        textY = imageY + imageH + GAP;

        int arrowY = imageY + (imageH - ARROW_H) / 2;
        addRenderableWidget(new ImageButton(panelX + PAD, arrowY, ARROW_W, ARROW_H, PREV, b -> step(-1)));
        addRenderableWidget(new ImageButton(panelX + panelW - PAD - ARROW_W, arrowY, ARROW_W, ARROW_H, NEXT, b -> step(1)));

        int buttonY = panelY + panelH - PAD - BUTTON_H;
        int centerX = panelX + panelW / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.broom_closet.soul_island.exit"), b -> onClose())
                .bounds(centerX - BUTTON_W - 4, buttonY, BUTTON_W, BUTTON_H).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.broom_closet.soul_island.select"), b -> confirm())
                .bounds(centerX + 4, buttonY, BUTTON_W, BUTTON_H).build());
    }

    private void step(int delta) {
        index = Math.floorMod(index + delta, entries.size());
    }

    private void confirm() {
        SoulIslandOption.Entry entry = entries.get(index);
        minecraft.setScreen(new ConfirmScreen(yes -> {
            if (yes) {
                PacketDistributor.sendToServer(new ChooseSoulIslandPayload(entry.id(), hand));
                minecraft.setScreen(null);
            } else {
                minecraft.setScreen(this);
            }
        }, Component.translatable("gui.broom_closet.soul_island.confirm.title", entry.name()),
                Component.translatable("gui.broom_closet.soul_island.confirm.message")));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            step(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            step(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(PANEL, panelX, panelY, panelW, panelH);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = panelX + panelW / 2;
        drawCentered(graphics, title.getVisualOrderText(), centerX, panelY + PAD, TITLE_COLOR);

        SoulIslandOption.Entry entry = entries.get(index);
        if (hasTexture(entry.screenshot())) {
            // Stretches the whole texture into the 16:9 box, whatever its pixel size.
            graphics.blit(entry.screenshot(), imageX, imageY, imageW, imageH, 0, 0, 1, 1, 1, 1);
        } else {
            graphics.fill(imageX, imageY, imageX + imageW, imageY + imageH, PLACEHOLDER_COLOR);
            graphics.drawCenteredString(font, Component.translatable("gui.broom_closet.soul_island.no_preview"),
                    centerX, imageY + (imageH - font.lineHeight) / 2, NO_PREVIEW_COLOR);
        }

        int y = textY;
        drawCentered(graphics, entry.name().copy().withStyle(ChatFormatting.BOLD).getVisualOrderText(), centerX, y, NAME_COLOR);
        y += font.lineHeight + 4;

        List<FormattedCharSequence> lines = font.split(entry.description(), imageW);
        for (int i = 0; i < Math.min(lines.size(), DESC_LINES); i++) {
            drawCentered(graphics, lines.get(i), centerX, y + i * font.lineHeight, DESC_COLOR);
        }
        y += DESC_LINES * font.lineHeight + GAP;

        drawCentered(graphics, Component.literal((index + 1) + " / " + entries.size()).getVisualOrderText(), centerX, y, DESC_COLOR);
    }

    // drawCenteredString always adds a shadow
    private void drawCentered(GuiGraphics graphics, FormattedCharSequence text, int centerX, int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    // A server datapack can list islands this client has no screenshot for.
    private boolean hasTexture(ResourceLocation texture) {
        return textureExists.computeIfAbsent(texture, t -> minecraft.getResourceManager().getResource(t).isPresent());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
