package heykatu.broom_closet.wishlist.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Not container/menu-based: the wishlist is purely client-side data (see WishlistData)
public class WishlistScreen extends Screen {

    private static final int ROW_WIDTH = 200;
    private static final int ROW_HEIGHT = 20;
    private static final int MAX_VISIBLE_ROWS = 9;
    private static final int ICON_SIZE = 16;
    private static final int ICON_PADDING = 3;
    private static final int TEXT_PADDING = 6;
    private static final int PANEL_PADDING = 7;
    private static final int TITLE_HEIGHT = 17;
    private static final int ROW_HOVER_COLOR = 0x80FFFFFF;
    private static final int TEXT_COLOR = 0x404040;
    private static final int HELP_COLOR = 0x555555;
    private static final int HELP_GAP = 5;
    // Everything but the title is drawn smaller
    private static final float TEXT_SCALE = 0.75F;

    // Vanilla's recipe book popup panel
    private static final ResourceLocation PANEL_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe");
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int listLeft;
    private int listTop;
    private int visibleRows;
    private int scrollRows;
    private int lastMouseX;
    private int lastMouseY;
    private List<Item> entries = List.of();
    private List<FormattedCharSequence> helpLines = List.of();

    public WishlistScreen() {
        super(Component.translatable("gui.broom_closet.wishlist.title"));
    }

    @Override
    protected void init() {
        entries = WishlistData.items();
        // Shows whatever key the toggle is actually bound to, since it's rebindable
        helpLines = this.font.split(Component.translatable("gui.broom_closet.wishlist.help",
                WishlistClient.TOGGLE_WISHLIST.getTranslatedKeyMessage()), (int) (ROW_WIDTH / TEXT_SCALE));
        int helpHeight = Mth.ceil(helpLines.size() * smallLineHeight()) + HELP_GAP;

        // Drop rows on small windows so the taller panel still fits on screen
        int chrome = TITLE_HEIGHT + helpHeight + PANEL_PADDING;
        visibleRows = Mth.clamp((this.height - 16 - chrome) / ROW_HEIGHT, 1, MAX_VISIBLE_ROWS);

        panelWidth = ROW_WIDTH + PANEL_PADDING * 2;
        panelHeight = chrome + visibleRows * ROW_HEIGHT;
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;
        listLeft = panelLeft + PANEL_PADDING;
        listTop = panelTop + TITLE_HEIGHT + helpHeight;
        scrollRows = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Re-read every frame so removals (via click or the W-toggle) show up immediately.
        entries = WishlistData.items();
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        graphics.blitSprite(PANEL_SPRITE, panelLeft, panelTop, panelWidth, panelHeight);
        graphics.drawString(this.font, this.title, panelLeft + 8, panelTop + 6, TEXT_COLOR, false);

        float helpY = panelTop + TITLE_HEIGHT;
        for (FormattedCharSequence line : helpLines) {
            drawSmall(graphics, line, listLeft, helpY, HELP_COLOR);
            helpY += smallLineHeight();
        }

        if (entries.isEmpty()) {
            Component message = Component.translatable("gui.broom_closet.wishlist.empty",
                    WishlistClient.TOGGLE_WISHLIST.getTranslatedKeyMessage());
            List<FormattedCharSequence> lines = this.font.split(message, (int) ((ROW_WIDTH - 10) / TEXT_SCALE));
            float y = listTop + (visibleRows * ROW_HEIGHT - lines.size() * smallLineHeight()) / 2;
            for (FormattedCharSequence line : lines) {
                drawSmall(graphics, line, this.width / 2F - this.font.width(line) * TEXT_SCALE / 2, y, TEXT_COLOR);
                y += smallLineHeight();
            }
            return;
        }

        int lastIndexExclusive = Math.min(entries.size(), scrollRows + visibleRows);

        for (int i = scrollRows; i < lastIndexExclusive; i++) {
            int row = i - scrollRows;
            int y = listTop + row * ROW_HEIGHT;
            boolean hovered = mouseX >= listLeft && mouseX < listLeft + ROW_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT;

            if (hovered) {
                graphics.fill(listLeft, y, listLeft + ROW_WIDTH, y + ROW_HEIGHT, ROW_HOVER_COLOR);
            }

            ItemStack stack = new ItemStack(entries.get(i));
            int iconY = y + (ROW_HEIGHT - ICON_SIZE) / 2;
            graphics.blitSprite(SLOT_SPRITE, listLeft + ICON_PADDING - 1, iconY - 1, ICON_SIZE + 2, ICON_SIZE + 2);
            graphics.renderItem(stack, listLeft + ICON_PADDING, iconY);

            float textY = y + (ROW_HEIGHT - smallLineHeight()) / 2;
            drawSmall(graphics, stack.getHoverName().getVisualOrderText(), listLeft + ICON_PADDING + ICON_SIZE + TEXT_PADDING, textY, TEXT_COLOR);
        }

        WishlistHover hover = hoverAt(mouseX, mouseY);
        if (hover != null) {
            graphics.renderTooltip(this.font, hover.stack(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            WishlistHover hover = hoverAt((int) mouseX, (int) mouseY);
            if (hover != null) {
                boolean added = WishlistData.toggle(hover.stack().getItem());
                WishlistFlash.trigger(hover.x(), hover.y(), added);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, entries.size() - visibleRows);
        scrollRows = Mth.clamp(scrollRows - (int) Math.signum(scrollY), 0, maxScroll);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private float smallLineHeight() {
        return this.font.lineHeight * TEXT_SCALE;
    }

    private void drawSmall(GuiGraphics graphics, FormattedCharSequence text, float x, float y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1);
        graphics.drawString(this.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private int indexAt(int mouseX, int mouseY) {
        if (mouseX < listLeft || mouseX >= listLeft + ROW_WIDTH || mouseY < listTop) {
            return -1;
        }
        int row = (mouseY - listTop) / ROW_HEIGHT;
        if (row < 0 || row >= visibleRows) {
            return -1;
        }
        int index = scrollRows + row;
        return index < entries.size() ? index : -1;
    }

    @Nullable
    private WishlistHover hoverAt(int mouseX, int mouseY) {
        int index = indexAt(mouseX, mouseY);
        if (index < 0) {
            return null;
        }
        int row = index - scrollRows;
        int x = listLeft + ICON_PADDING;
        int y = listTop + row * ROW_HEIGHT + (ROW_HEIGHT - ICON_SIZE) / 2;
        return new WishlistHover(new ItemStack(entries.get(index)), x, y);
    }

    // Used by WishlistKeyHandler so the toggle also works as a remove action here,
    // consistent with every other place it's used. KeyPressed events don't carry mouse
    // coordinates, hence the cached lastMouseX/lastMouseY
    @Nullable
    public WishlistHover getHover() {
        return hoverAt(lastMouseX, lastMouseY);
    }
}
