package io.github.blaezdev.rwbym.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.blaezdev.rwbym.RWBYM;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Renders Reese/Lucid Rose hoverboard items beneath the player while their ride channel is active.
 */
public class RWBYMHoverboardLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public RWBYMHoverboardLayer(PlayerRenderer renderer) {
        super((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
            float headPitch) {
        ItemStack board = player.getMainHandItem();
        if (!isActiveBoard(player, board)) {
            return;
        }

        poseStack.pushPose();
        if (player.isCrouching()) {
            poseStack.translate(0.0D, 0.12D, 0.0D);
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original PlayerRenderHandler rendered the active board item at the player's feet instead of in-hand.
        poseStack.translate(0.0D, 1.15D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(1.15F, 1.15F, 1.15F);
        Minecraft.getInstance().getItemRenderer().renderStatic(player, board, ItemDisplayContext.HEAD,
                false, poseStack, buffer, player.level(), packedLight, 0, player.getId());
        poseStack.popPose();
    }

    private static boolean isActiveBoard(AbstractClientPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !player.isUsingItem() || player.getUseItem() != stack) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getNamespace().equals(RWBYM.MOD_ID)
                && (id.getPath().equals("lucidroseboard") || id.getPath().equals("reese"));
    }
}
