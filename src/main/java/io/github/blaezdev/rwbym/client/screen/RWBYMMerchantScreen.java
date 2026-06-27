package io.github.blaezdev.rwbym.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.menu.RWBYMMerchantMenu;
import io.github.blaezdev.rwbym.network.MerchantTradeActionPacket;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * RWBYM's book-style merchant GUI, ported from the original GuiVillager/GuiTradingBook flow.
 *
 * <p>The visual trade list and search panel follow the legacy RWBYM layout, while the actual
 * payment/result slots remain backed by {@link RWBYMMerchantMenu} so Forge 1.20.1 keeps vanilla
 * merchant synchronization and trade validation.</p>
 */
public class RWBYMMerchantScreen extends AbstractContainerScreen<RWBYMMerchantMenu> {
    private static final ResourceLocation MERCHANT_TEXTURE =
            new ResourceLocation(RWBYM.MOD_ID, "textures/gui/container/merchant.png");
    private static final ResourceLocation BOOK_TEXTURE =
            new ResourceLocation(RWBYM.MOD_ID, "textures/gui/container/merchant_book.png");
    private static final int BOOK_WIDTH = 112;
    private static final int BOOK_HEIGHT = 166;
    private static final int MERCHANT_PANEL_X = 100;
    private static final int VISIBLE_TRADES = 6;
    private static final int TRADE_ROW_X = 10;
    private static final int TRADE_ROW_Y = 24;
    private static final int TRADE_ROW_WIDTH = 84;
    private static final int TRADE_ROW_HEIGHT = 22;
    private static final int TRADE_ROW_GAP = 22;

    private final List<Integer> filteredOffers = new ArrayList<>();
    private EditBox searchBox;
    private String lastSearch = "";
    private int selectedOffer;
    private int scrollOffset;
    private int lastOfferCount = -1;
    private int ghostOfferIndex = -1;

    public RWBYMMerchantScreen(RWBYMMerchantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
        this.imageHeight = 166;
        this.inventoryLabelX = 108;
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = new EditBox(this.font, this.leftPos + 9, this.topPos + 9, 80, 9,
                Component.translatable("gui.rwbym.merchant.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(0xFFFFFF);
        this.searchBox.setCanLoseFocus(true);
        this.searchBox.setResponder(this::setSearchQuery);
        this.refreshFilteredOffers();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.searchBox != null) {
            this.searchBox.tick();
        }
        int offerCount = this.menu.getOffers().size();
        if (this.lastOfferCount != offerCount) {
            this.refreshFilteredOffers();
        }
        if (this.hasPaymentSlotsContents()) {
            this.ghostOfferIndex = -1;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BOOK_TEXTURE, this.leftPos, this.topPos, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        graphics.blit(MERCHANT_TEXTURE, this.leftPos + MERCHANT_PANEL_X, this.topPos, 0, 0, 176, this.imageHeight);
        this.renderMerchantEntity(graphics, mouseX, mouseY);
        this.renderTradeBook(graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.searchBox != null) {
            this.searchBox.render(graphics, mouseX, mouseY, partialTick);
        }
        this.renderGhostTrade(graphics);
        this.renderHoveredTradeTooltips(graphics, mouseX, mouseY);
        this.renderGhostTradeTooltip(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.searchBox != null && this.searchBox.charTyped(codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.searchBox != null && this.searchBox.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.searchBox != null && this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.searchBox);
            return true;
        }
        int clickedOffer = this.getHoveredOfferIndex((int) mouseX, (int) mouseY);
        if (clickedOffer >= 0 && (button == 0 || button == 1)) {
            if (button == 1 && this.hasRecipeContents(this.menu.getOffers().get(clickedOffer))) {
                this.quickTradeOffer(clickedOffer, Screen.hasShiftDown());
            } else {
                this.selectOffer(clickedOffer);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.canScroll()) {
            int maxScroll = Math.max(0, this.filteredOffers.size() - VISIBLE_TRADES);
            this.scrollOffset = Mth.clamp((int) (this.scrollOffset - delta), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void setSearchQuery(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        if (!normalized.equals(this.lastSearch)) {
            this.lastSearch = normalized;
            this.scrollOffset = 0;
            this.refreshFilteredOffers();
        }
    }

    private void refreshFilteredOffers() {
        this.filteredOffers.clear();
        MerchantOffers offers = this.menu.getOffers();
        this.lastOfferCount = offers.size();
        for (int i = 0; i < offers.size(); i++) {
            if (this.matchesSearch(offers.get(i), this.lastSearch)) {
                this.filteredOffers.add(i);
            }
        }
        int maxScroll = Math.max(0, this.filteredOffers.size() - VISIBLE_TRADES);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, maxScroll);
    }

    private boolean matchesSearch(MerchantOffer offer, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String trimmed = query.trim();
        int mode = 0;
        if (trimmed.startsWith("<")) {
            mode = 1;
            trimmed = trimmed.substring(1).trim();
        } else if (trimmed.startsWith(">")) {
            mode = 2;
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isEmpty()) {
            return true;
        }
        return (mode < 2 && (stackMatches(offer.getCostA(), trimmed) || stackMatches(offer.getCostB(), trimmed)))
                || (mode != 1 && stackMatches(offer.getResult(), trimmed));
    }

    private boolean stackMatches(ItemStack stack, String query) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        TooltipFlag tooltipFlag = this.minecraft != null && this.minecraft.options.advancedItemTooltips
                ? TooltipFlag.ADVANCED
                : TooltipFlag.NORMAL;
        return stack.getTooltipLines(this.minecraft == null ? null : this.minecraft.player, tooltipFlag).stream()
                .map(component -> component.getString().toLowerCase(Locale.ROOT))
                .anyMatch(line -> line.contains(query));
    }

    private void renderTradeBook(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.filteredOffers.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("merchant.trades"), this.leftPos + 12,
                    this.topPos + 34, 0x6F6F6F, false);
            return;
        }

        for (int visible = 0; visible < VISIBLE_TRADES; visible++) {
            int listIndex = this.scrollOffset + visible;
            if (listIndex >= this.filteredOffers.size()) {
                break;
            }
            int offerIndex = this.filteredOffers.get(listIndex);
            MerchantOffer offer = this.menu.getOffers().get(offerIndex);
            int rowX = this.leftPos + TRADE_ROW_X;
            int rowY = this.topPos + TRADE_ROW_Y + visible * TRADE_ROW_GAP;
            boolean selected = offerIndex == this.selectedOffer;
            boolean hovered = isInBox(mouseX, mouseY, rowX, rowY, TRADE_ROW_WIDTH, TRADE_ROW_HEIGHT);
            boolean hasContents = this.hasRecipeContents(offer);
            int textureY = hovered ? 22 : 0;
            if (!hasContents) {
                textureY += 88;
            }
            if (selected) {
                textureY += 44;
            }
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // The legacy merchant book stores button states vertically in merchant_book.png.
            graphics.blit(BOOK_TEXTURE, rowX, rowY, 112, textureY, TRADE_ROW_WIDTH, TRADE_ROW_HEIGHT);
            this.renderOfferRow(graphics, offer, rowX, rowY);
        }
        this.renderBookScrollBar(graphics);
    }

    private void renderOfferRow(GuiGraphics graphics, MerchantOffer offer, int rowX, int rowY) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult();
        graphics.renderFakeItem(costA, rowX + 2, rowY + 2);
        graphics.renderItemDecorations(this.font, costA, rowX + 2, rowY + 2);
        if (!costB.isEmpty()) {
            graphics.renderFakeItem(costB, rowX + 24, rowY + 2);
            graphics.renderItemDecorations(this.font, costB, rowX + 24, rowY + 2);
        }
        graphics.drawString(this.font, ">", rowX + 49, rowY + 7, offer.isOutOfStock() ? 0xAA3333 : 0x3F3F3F, false);
        graphics.renderFakeItem(result, rowX + 64, rowY + 2);
        graphics.renderItemDecorations(this.font, result, rowX + 64, rowY + 2);
        if (offer.isOutOfStock()) {
            graphics.blit(BOOK_TEXTURE, rowX + 47, rowY + 3, this.hasRecipeContents(offer) ? 0 : 10, 166, 10, 15);
        }
    }

    private void renderBookScrollBar(GuiGraphics graphics) {
        int scrollX = this.leftPos + 98;
        int scrollY = this.topPos + 8;
        int trackHeight = 149;
        boolean scrollable = this.canScroll();
        int thumbHeight = scrollable
                ? Math.max(18, (int) (trackHeight * (VISIBLE_TRADES / (float) this.filteredOffers.size())))
                : trackHeight;
        int maxTravel = trackHeight - thumbHeight;
        int thumbY = scrollY + (scrollable
                ? Math.round(maxTravel * (this.scrollOffset / (float) Math.max(1, this.filteredOffers.size() - VISIBLE_TRADES)))
                : 0);
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        graphics.blit(BOOK_TEXTURE, scrollX, thumbY, scrollable ? 196 : 202, 0, 6, thumbHeight);
    }

    private void renderMerchantEntity(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (minecraft.level.getEntity(this.menu.getMerchantEntityId()) instanceof LivingEntity merchant) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, this.leftPos + 133, this.topPos + 75,
                    30, this.leftPos + 133 - mouseX, this.topPos + 25 - mouseY, merchant);
        }
    }

    private void renderHoveredTradeTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int hoveredOffer = this.getHoveredOfferIndex(mouseX, mouseY);
        if (hoveredOffer < 0 || hoveredOffer >= this.menu.getOffers().size()) {
            return;
        }
        MerchantOffer offer = this.menu.getOffers().get(hoveredOffer);
        int visible = this.filteredOffers.indexOf(hoveredOffer) - this.scrollOffset;
        int rowX = this.leftPos + TRADE_ROW_X;
        int rowY = this.topPos + TRADE_ROW_Y + visible * TRADE_ROW_GAP;
        if (isInBox(mouseX, mouseY, rowX + 2, rowY + 2, 16, 16)) {
            graphics.renderTooltip(this.font, offer.getCostA(), mouseX, mouseY);
        } else if (!offer.getCostB().isEmpty() && isInBox(mouseX, mouseY, rowX + 24, rowY + 2, 16, 16)) {
            graphics.renderTooltip(this.font, offer.getCostB(), mouseX, mouseY);
        } else if (isInBox(mouseX, mouseY, rowX + 64, rowY + 2, 16, 16)) {
            graphics.renderTooltip(this.font, offer.getResult(), mouseX, mouseY);
        } else if (offer.isOutOfStock() && isInBox(mouseX, mouseY, rowX + 47, rowY + 3, 10, 15)) {
            graphics.renderTooltip(this.font, Component.translatable("merchant.deprecated"), mouseX, mouseY);
        }
    }

    private int getHoveredOfferIndex(int mouseX, int mouseY) {
        int rowX = this.leftPos + TRADE_ROW_X;
        for (int visible = 0; visible < VISIBLE_TRADES; visible++) {
            int listIndex = this.scrollOffset + visible;
            if (listIndex >= this.filteredOffers.size()) {
                return -1;
            }
            int rowY = this.topPos + TRADE_ROW_Y + visible * TRADE_ROW_GAP;
            if (isInBox(mouseX, mouseY, rowX, rowY, TRADE_ROW_WIDTH, TRADE_ROW_HEIGHT)) {
                return this.filteredOffers.get(listIndex);
            }
        }
        return -1;
    }

    private void selectOffer(int offerIndex) {
        this.selectedOffer = offerIndex;
        MerchantOffer offer = this.menu.getOffers().get(offerIndex);
        this.menu.setSelectionHint(offerIndex);
        if (this.hasRecipeContents(offer)) {
            this.ghostOfferIndex = -1;
            this.menu.tryMoveItems(offerIndex);
            // Original GuiVillager mirrored recipe-button ingredient moves on the server with MessageTradingData.
            RWBYMNetwork.CHANNEL.sendToServer(new MerchantTradeActionPacket(offerIndex, false, false));
        } else {
            this.ghostOfferIndex = offerIndex;
            if (this.hasPaymentSlotsContents()) {
                // Original GuiVillager cleared stale payment items before showing a ghost recipe for a missing trade.
                this.menu.returnPaymentSlotsToInventory();
                RWBYMNetwork.CHANNEL.sendToServer(new MerchantTradeActionPacket(-1, false));
            }
        }
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(new ServerboundSelectTradePacket(offerIndex));
        }
    }

    private void quickTradeOffer(int offerIndex, boolean tradeAll) {
        this.selectedOffer = offerIndex;
        this.ghostOfferIndex = -1;
        this.menu.setSelectionHint(offerIndex);
        // The server performs the result-slot take; the client only mirrors selection for immediate UI feedback.
        RWBYMNetwork.CHANNEL.sendToServer(new MerchantTradeActionPacket(offerIndex, tradeAll));
    }

    private void renderGhostTrade(GuiGraphics graphics) {
        if (this.ghostOfferIndex < 0 || this.ghostOfferIndex >= this.menu.getOffers().size()) {
            return;
        }
        MerchantOffer offer = this.menu.getOffers().get(this.ghostOfferIndex);
        this.renderGhostStack(graphics, 0, offer.getCostA(), false);
        this.renderGhostStack(graphics, 1, offer.getCostB(), false);
        this.renderGhostStack(graphics, 2, offer.getResult(), true);
    }

    private void renderGhostStack(GuiGraphics graphics, int slotIndex, ItemStack stack, boolean outputSlot) {
        if (stack.isEmpty()) {
            return;
        }
        Slot slot = this.menu.getSlot(slotIndex);
        int x = this.leftPos + slot.x;
        int y = this.topPos + slot.y;
        if (outputSlot) {
            graphics.fill(x - 4, y - 4, x + 20, y + 20, 0x33000000);
        } else {
            graphics.fill(x, y, x + 16, y + 16, 0x33000000);
        }
        graphics.renderFakeItem(stack, x, y);
        graphics.fill(x, y, x + 16, y + 16, 0x55FFFFFF);
        graphics.renderItemDecorations(this.font, stack, x, y);
    }

    private void renderGhostTradeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.ghostOfferIndex < 0 || this.ghostOfferIndex >= this.menu.getOffers().size()) {
            return;
        }
        MerchantOffer offer = this.menu.getOffers().get(this.ghostOfferIndex);
        ItemStack hovered = this.hoveredGhostStack(mouseX, mouseY, offer);
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(this.font, hovered, mouseX, mouseY);
        }
    }

    private ItemStack hoveredGhostStack(int mouseX, int mouseY, MerchantOffer offer) {
        for (int slotIndex = 0; slotIndex <= 2; slotIndex++) {
            Slot slot = this.menu.getSlot(slotIndex);
            if (isInBox(mouseX, mouseY, this.leftPos + slot.x, this.topPos + slot.y, 16, 16)) {
                return switch (slotIndex) {
                    case 0 -> offer.getCostA();
                    case 1 -> offer.getCostB();
                    default -> offer.getResult();
                };
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean hasPaymentSlotsContents() {
        return this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem();
    }

    private boolean hasRecipeContents(MerchantOffer offer) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        boolean hasFirst = costA.isEmpty() || this.countAvailable(costA) >= costA.getCount();
        boolean hasSecond = costB.isEmpty() || this.countAvailable(costB) >= costB.getCount();
        return hasFirst && hasSecond;
    }

    private int countAvailable(ItemStack expected) {
        int count = 0;
        for (int i = 0; i < this.menu.slots.size(); i++) {
            if (i == 2) {
                continue;
            }
            Slot slot = this.menu.slots.get(i);
            ItemStack stack = slot.getItem();
            // Original RWBYM counted matching items by item type so renamed currency still works in trades.
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, expected)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean canScroll() {
        return this.filteredOffers.size() > VISIBLE_TRADES;
    }

    private static boolean isInBox(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
