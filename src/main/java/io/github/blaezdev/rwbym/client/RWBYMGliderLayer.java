package io.github.blaezdev.rwbym.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.blaezdev.rwbym.item.RWBYMGliderItem;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

/**
 * Renders the deployed RWBYM glider model while the player is actively using a glider item.
 */
public class RWBYMGliderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ItemInHandRenderer itemRenderer;

    public RWBYMGliderLayer(PlayerRenderer renderer, ItemInHandRenderer itemRenderer) {
        super((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
            float headPitch) {
        if (!(player.getUseItem().getItem() instanceof RWBYMGliderItem)) {
            return;
        }
        RegistryObject<net.minecraft.world.item.Item> deployed = RWBYMItems.SIMPLE_ITEMS.get("gliderdeployed");
        if (deployed == null || !deployed.isPresent()) {
            return;
        }
        poseStack.pushPose();
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Keep the legacy LayerGlider transform here; the item model's HEAD display already anchors it to the player.
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.5F, 0.7F, 0.5F);
        this.itemRenderer.renderItem(player, new ItemStack(deployed.get()), ItemDisplayContext.HEAD, false,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
