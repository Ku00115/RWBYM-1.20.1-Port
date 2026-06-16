package io.github.blaezdev.rwbym.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.menu.CrusherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrusherScreen extends AbstractContainerScreen<CrusherMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RWBYM.MOD_ID, "textures/gui/crusher.png");

    public CrusherScreen(CrusherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = this.leftPos;
        int top = this.topPos;
        graphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight);
        int burn = this.menu.burnProgress(13);
        if (burn > 0) {
            graphics.blit(TEXTURE, left + 8, top + 54 + 12 - burn, 176, 12 - burn, 14, burn + 1);
        }
        int cook = this.menu.cookProgress(24);
        graphics.blit(TEXTURE, left + 44, top + 36, 176, 14, cook + 1, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
