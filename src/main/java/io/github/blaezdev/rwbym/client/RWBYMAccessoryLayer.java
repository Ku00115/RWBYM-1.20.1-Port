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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RWBYMAccessoryLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final int MAX_ITEM_CACHE_SIZE = 512;
    private static final Map<String, Item> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, ItemStack> STACK_CACHE = new ConcurrentHashMap<>();

    public RWBYMAccessoryLayer(PlayerRenderer renderer) {
        super((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
            float headPitch) {
        PlayerModel<AbstractClientPlayer> model = getParentModel();
        renderHeadWearable(poseStack, buffer, packedLight, player, model.head);
        renderSlot(poseStack, buffer, packedLight, player, model.head, "Head", Part.HEAD);
        renderSlot(poseStack, buffer, packedLight, player, model.head, "Ears", Part.HEAD);
        renderSlot(poseStack, buffer, packedLight, player, model.rightArm, "RightArm", Part.RIGHT_ARM);
        renderSlot(poseStack, buffer, packedLight, player, model.leftArm, "LeftArm", Part.LEFT_ARM);
        renderSlot(poseStack, buffer, packedLight, player, model.rightLeg, "RightLeg", Part.RIGHT_LEG);
        renderSlot(poseStack, buffer, packedLight, player, model.leftLeg, "LeftLeg", Part.LEFT_LEG);
        renderSlot(poseStack, buffer, packedLight, player, model.body, "Body", Part.BODY);
        renderSlot(poseStack, buffer, packedLight, player, model.body, "Tail", Part.BODY);
    }

    private static void renderHeadWearable(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            AbstractClientPlayer player, ModelPart head) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(stack.getItem() instanceof RWBYMWearableItem)) {
            return;
        }

        poseStack.pushPose();
        if (player.isCrouching()) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
        head.translateAndRotate(poseStack);
        applyBaseAttachmentTransform(poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        Minecraft.getInstance().getItemRenderer().renderStatic(player, stack, ItemDisplayContext.HEAD,
                false, poseStack, buffer, player.level(), packedLight, 0, player.getId());
        poseStack.popPose();
    }

    private static void renderSlot(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            AbstractClientPlayer player, ModelPart part, String slot, Part attachment) {
        String itemId = RWBYMLimbItem.getAppearance(player, slot);
        if (itemId.isBlank()) {
            return;
        }
        Item item = resolveItem(itemId);
        if (item == Items.AIR || !(item instanceof RWBYMLimbItem)
                || item instanceof RWBYMArmorItem || item instanceof RWBYMWearableItem) {
            return;
        }

        poseStack.pushPose();
        if (player.isCrouching()) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
        part.translateAndRotate(poseStack);
        applyBaseAttachmentTransform(poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        applyPostScaleAttachmentTransform(poseStack, attachment);
        ItemDisplayContext context = ItemDisplayContext.HEAD;
        Minecraft.getInstance().getItemRenderer().renderStatic(player, stackFor(item), context,
                false, poseStack, buffer, player.level(), packedLight, 0, player.getId());
        poseStack.popPose();
    }

    private static void applyBaseAttachmentTransform(PoseStack poseStack) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original LayerAccessories applies this before rotating/scaling every non-armor accessory item.
        poseStack.translate(0.0F, -0.25F, 0.0F);
    }

    private static void applyPostScaleAttachmentTransform(PoseStack poseStack, Part attachment) {
        switch (attachment) {
            case RIGHT_ARM -> poseStack.translate(-0.5F, 0.1875F, 0.0F);
            case LEFT_ARM -> poseStack.translate(0.5F, 0.1875F, 0.0F);
            case RIGHT_LEG -> poseStack.translate(-0.1875F, 1.1875F, 0.0F);
            case LEFT_LEG -> poseStack.translate(0.1875F, 1.1875F, 0.0F);
            case HEAD, BODY -> {
            }
        }
    }

    private static Item resolveItem(String itemId) {
        if (ITEM_CACHE.size() > MAX_ITEM_CACHE_SIZE) {
            ITEM_CACHE.clear();
        }
        return ITEM_CACHE.computeIfAbsent(itemId, id -> {
            try {
                return BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            } catch (RuntimeException ignored) {
                return Items.AIR;
            }
        });
    }

    private static ItemStack stackFor(Item item) {
        return STACK_CACHE.computeIfAbsent(item, ItemStack::new);
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
