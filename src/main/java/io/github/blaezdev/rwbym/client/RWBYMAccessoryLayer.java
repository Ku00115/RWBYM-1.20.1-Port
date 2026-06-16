package io.github.blaezdev.rwbym.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.blaezdev.rwbym.item.RWBYMArmorItem;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.item.RWBYMWearableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RWBYMAccessoryLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public RWBYMAccessoryLayer(PlayerRenderer renderer) {
        super((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
            float headPitch) {
        PlayerModel<AbstractClientPlayer> model = getParentModel();
        renderSlot(poseStack, buffer, packedLight, player, model.head, "Head", Part.HEAD);
        renderSlot(poseStack, buffer, packedLight, player, model.head, "Ears", Part.HEAD);
        renderSlot(poseStack, buffer, packedLight, player, model.rightArm, "RightArm", Part.RIGHT_ARM);
        renderSlot(poseStack, buffer, packedLight, player, model.leftArm, "LeftArm", Part.LEFT_ARM);
        renderSlot(poseStack, buffer, packedLight, player, model.rightLeg, "RightLeg", Part.RIGHT_LEG);
        renderSlot(poseStack, buffer, packedLight, player, model.leftLeg, "LeftLeg", Part.LEFT_LEG);
        renderSlot(poseStack, buffer, packedLight, player, model.body, "Body", Part.BODY);
        renderSlot(poseStack, buffer, packedLight, player, model.body, "Tail", Part.BODY);
    }

    private static void renderSlot(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            AbstractClientPlayer player, ModelPart part, String slot, Part attachment) {
        String itemId = RWBYMLimbItem.getAppearance(player, slot);
        if (itemId.isBlank()) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        if (item == Items.AIR || !(item instanceof RWBYMLimbItem)
                || item instanceof RWBYMArmorItem || item instanceof RWBYMWearableItem) {
            return;
        }

        poseStack.pushPose();
        if (player.isCrouching()) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
        part.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        switch (attachment) {
            case RIGHT_ARM -> poseStack.translate(-0.5F, 0.1875F, 0.0F);
            case LEFT_ARM -> poseStack.translate(0.5F, 0.1875F, 0.0F);
            case RIGHT_LEG -> poseStack.translate(-0.1875F, 1.1875F, 0.0F);
            case LEFT_LEG -> poseStack.translate(0.1875F, 1.1875F, 0.0F);
            case HEAD, BODY -> {
            }
        }
        ItemDisplayContext context = attachment == Part.HEAD ? ItemDisplayContext.HEAD : ItemDisplayContext.FIXED;
        Minecraft.getInstance().getItemRenderer().renderStatic(player, new ItemStack(item), context,
                false, poseStack, buffer, player.level(), packedLight, 0, player.getId());
        poseStack.popPose();
    }

    private enum Part {
        HEAD,
        RIGHT_ARM,
        LEFT_ARM,
        RIGHT_LEG,
        LEFT_LEG,
        BODY
    }
}
