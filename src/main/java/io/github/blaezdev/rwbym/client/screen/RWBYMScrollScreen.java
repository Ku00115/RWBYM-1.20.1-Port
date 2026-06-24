package io.github.blaezdev.rwbym.client.screen;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.network.OpenScrollScreenPacket;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import io.github.blaezdev.rwbym.network.ScrollAuraLevelPacket;
import io.github.blaezdev.rwbym.network.ScrollTeamActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * First-pass 1.20.1 port of the original RWBYM Scroll GUI.
 */
public class RWBYMScrollScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RWBYM.MOD_ID, "textures/gui/scroll.png");
    private static final int IMAGE_WIDTH = 128;
    private static final int IMAGE_HEIGHT = 210;
    private static final int AURA_BAR_WIDTH = 81;
    private static final int AURA_BAR_HEIGHT = 9;

    private final OpenScrollScreenPacket snapshot;
    private Tab tab = Tab.TEAM;
    private int leftPos;
    private int topPos;
    private EditBox requestBox;

    public RWBYMScrollScreen(OpenScrollScreenPacket snapshot) {
        super(Component.translatable("screen.rwbym.scroll"));
        this.snapshot = snapshot;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;
        rebuildScrollWidgets();
    }

    private void rebuildScrollWidgets() {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Screen owns separate render/child/narration lists, so rebuild through public widget helpers.
        clearWidgets();
        addTabButton(0, Tab.TEAM);
        addTabButton(1, Tab.REQUESTS);
        addTabButton(2, Tab.LEVEL);
        addTeamButtons();
        addRequestButtons();
        addLevelButtons();
    }

    private void addTabButton(int index, Tab tab) {
        addRenderableWidget(Button.builder(Component.literal(tab.label), button -> {
            this.tab = tab;
            rebuildScrollWidgets();
        }).bounds(this.leftPos + 4 + index * 34, this.topPos + 4, 30, 12).build());
    }

    private void addLevelButtons() {
        if (this.tab == Tab.LEVEL) {
            int y = this.topPos + 122;
            addRenderableWidget(Button.builder(Component.literal("Lvl Aura"), button ->
                    RWBYMNetwork.CHANNEL.sendToServer(new ScrollAuraLevelPacket(false)))
                    .bounds(this.leftPos + 20, y, 88, 14).build());
            addRenderableWidget(Button.builder(Component.literal("Lvl Max"), button ->
                    RWBYMNetwork.CHANNEL.sendToServer(new ScrollAuraLevelPacket(true)))
                    .bounds(this.leftPos + 20, y + 18, 88, 14).build());
        }
    }

    private void addTeamButtons() {
        if (this.tab == Tab.TEAM && !this.snapshot.teamMembers().isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal("Leave Team"), button ->
                    RWBYMNetwork.CHANNEL.sendToServer(new ScrollTeamActionPacket(ScrollTeamActionPacket.Action.LEAVE, "")))
                    .bounds(this.leftPos + 20, this.topPos + 150, 88, 14).build());
        }
    }

    private void addRequestButtons() {
        if (this.tab != Tab.REQUESTS) {
            this.requestBox = null;
            return;
        }
        this.requestBox = new EditBox(this.font, this.leftPos + 18, this.topPos + 102, 92, 14,
                Component.literal("Player"));
        this.requestBox.setMaxLength(32);
        addRenderableWidget(this.requestBox);
        addRenderableWidget(Button.builder(Component.literal("Send"), button -> {
            String target = this.requestBox.getValue().trim();
            if (!target.isEmpty()) {
                RWBYMNetwork.CHANNEL.sendToServer(new ScrollTeamActionPacket(ScrollTeamActionPacket.Action.SEND, target));
            }
        }).bounds(this.leftPos + 38, this.topPos + 120, 52, 14).build());
        int y = this.topPos + 144;
        for (int i = 0; i < Math.min(2, this.snapshot.receivedRequests().size()); i++) {
            String sender = this.snapshot.receivedRequests().get(i);
            int rowY = y + i * 26;
            addRenderableWidget(Button.builder(Component.literal("Accept"), button ->
                    RWBYMNetwork.CHANNEL.sendToServer(new ScrollTeamActionPacket(ScrollTeamActionPacket.Action.ACCEPT, sender)))
                    .bounds(this.leftPos + 8, rowY + 10, 54, 12).build());
            addRenderableWidget(Button.builder(Component.literal("Decline"), button ->
                    RWBYMNetwork.CHANNEL.sendToServer(new ScrollTeamActionPacket(ScrollTeamActionPacket.Action.DECLINE, sender)))
                    .bounds(this.leftPos + 66, rowY + 10, 54, 12).build());
        }
        for (int i = 0; i < Math.min(2, this.snapshot.sentRequests().size()); i++) {
            String receiver = this.snapshot.sentRequests().get(i);
            int rowY = this.topPos + 68 + i * 18;
            addRenderableWidget(Button.builder(Component.literal("Remove"), button ->
                    RWBYMNetwork.CHANNEL.sendToServer(new ScrollTeamActionPacket(ScrollTeamActionPacket.Action.REMOVE_SENT, receiver)))
                    .bounds(this.leftPos + 68, rowY, 52, 12).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        drawAuraBar(graphics);
        drawTabContent(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawAuraBar(GuiGraphics graphics) {
        int x = this.leftPos + (IMAGE_WIDTH - AURA_BAR_WIDTH) / 2;
        int y = this.topPos + 55;
        float percent = this.snapshot.auraMax() <= 0.0F ? 0.0F : this.snapshot.auraAmount() / this.snapshot.auraMax();
        int filled = Math.max(0, Math.min(AURA_BAR_WIDTH - 2, Math.round((AURA_BAR_WIDTH - 2) * percent)));
        graphics.blit(TEXTURE, x + 1, y + 1, 0, IMAGE_HEIGHT + 10, filled, AURA_BAR_HEIGHT - 2);
        graphics.blit(TEXTURE, x, y, 0, IMAGE_HEIGHT, AURA_BAR_WIDTH, AURA_BAR_HEIGHT);
    }

    private void drawTabContent(GuiGraphics graphics) {
        int center = this.leftPos + IMAGE_WIDTH / 2;
        graphics.drawCenteredString(this.font, this.title, center, this.topPos + 20, 0xFFFFFF);
        String aura = Math.round(this.snapshot.auraAmount()) + " / " + Math.round(this.snapshot.auraMax());
        graphics.drawCenteredString(this.font, Component.literal("Aura: " + aura), center, this.topPos + 70, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.literal("Semblance: " + this.snapshot.semblanceName() + " Lv " + this.snapshot.semblanceLevel()),
                center, this.topPos + 84, 0xFFFFFF);

        switch (this.tab) {
            case TEAM -> drawTeamTab(graphics, center);
            case REQUESTS -> drawRequestsTab(graphics, center);
            case LEVEL -> drawLevelTab(graphics, center);
        }
    }

    private void drawTeamTab(GuiGraphics graphics, int center) {
        String name = Minecraft.getInstance().player == null ? "" : Minecraft.getInstance().player.getGameProfile().getName();
        graphics.drawCenteredString(this.font, Component.literal(name), center, this.topPos + 104, 0xD8D8D8);
        if (this.snapshot.teamMembers().isEmpty()) {
            graphics.drawCenteredString(this.font, Component.literal("Solo"), center, this.topPos + 122, 0xA8A8A8);
            return;
        }
        for (int i = 0; i < Math.min(3, this.snapshot.teamMembers().size()); i++) {
            graphics.drawCenteredString(this.font, Component.literal(this.snapshot.teamMembers().get(i)), center,
                    this.topPos + 122 + i * 12, 0xFFFFFF);
        }
    }

    private void drawRequestsTab(GuiGraphics graphics, int center) {
        graphics.drawCenteredString(this.font, Component.literal("Sent"), this.leftPos + 36, this.topPos + 58, 0xFFFFFF);
        for (int i = 0; i < Math.min(2, this.snapshot.sentRequests().size()); i++) {
            graphics.drawString(this.font, this.snapshot.sentRequests().get(i), this.leftPos + 8,
                    this.topPos + 70 + i * 18, 0xD8D8D8, false);
        }
        graphics.drawCenteredString(this.font, Component.literal("Invite"), center, this.topPos + 92, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Received"), center, this.topPos + 134, 0xFFFFFF);
        for (int i = 0; i < Math.min(2, this.snapshot.receivedRequests().size()); i++) {
            graphics.drawCenteredString(this.font, Component.literal(this.snapshot.receivedRequests().get(i)), center,
                    this.topPos + 144 + i * 26, 0xD8D8D8);
        }
    }

    private void drawLevelTab(GuiGraphics graphics, int center) {
        String cost = this.snapshot.auraExpCost() == Integer.MAX_VALUE ? "Maxed" : this.snapshot.auraExpCost() + " XP";
        graphics.drawCenteredString(this.font, Component.literal("Next Aura: " + cost), center, this.topPos + 104, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Selected Lv: " + this.snapshot.selectedLevel()), center,
                this.topPos + 158, 0xD8D8D8);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum Tab {
        TEAM("Team"),
        REQUESTS("Req"),
        LEVEL("Level");

        private final String label;

        Tab(String label) {
            this.label = label;
        }
    }
}
