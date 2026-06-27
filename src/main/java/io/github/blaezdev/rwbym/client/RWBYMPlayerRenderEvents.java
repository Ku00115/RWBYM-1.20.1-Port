package io.github.blaezdev.rwbym.client;

import com.mojang.math.Axis;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.RWBYMArmorItem;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Restores original RWBYM player-render layer masking for skin-layout armor.
 * RWBYM armor textures are full player-skin maps, so vanilla outer skin parts must be hidden while the matching
 * armor slot renders or the player's second skin layer can cover the armor artwork.
 */
@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, value = Dist.CLIENT)
public final class RWBYMPlayerRenderEvents {
    private static final Map<UUID, SkinLayerState> HIDDEN_LAYER_STATES = new HashMap<>();

    private RWBYMPlayerRenderEvents() {
    }

    @SubscribeEvent
    public static void beforePlayerRender(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        SkinLayerState previous = SkinLayerState.capture(model);

        boolean changed = false;
        changed |= hideSkinLayersForArmor(player, model);
        changed |= hideReplacedLimbsForAppearance(player, model);

        if (changed) {
            HIDDEN_LAYER_STATES.put(player.getUUID(), previous);
        }
    }

    @SubscribeEvent
    public static void afterPlayerRender(RenderPlayerEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }
        SkinLayerState previous = HIDDEN_LAYER_STATES.remove(player.getUUID());
        if (previous != null) {
            previous.restore(event.getRenderer().getModel());
        }
        renderActiveHoverboard(event, player);
    }

    private static void renderActiveHoverboard(RenderPlayerEvent.Post event, AbstractClientPlayer player) {
        ItemStack board = player.getMainHandItem();
        if (!isActiveBoard(player, board)) {
            return;
        }

        event.getPoseStack().pushPose();
        if (player.isCrouching()) {
            event.getPoseStack().translate(0.0D, -0.125D, 0.0D);
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The 1.12 PlayerRenderHandler rendered the active board in player-root space with HEAD transforms.
        event.getPoseStack().translate(0.0D, 0.1D, 0.0D);
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(90.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(player, board, ItemDisplayContext.HEAD,
                false, event.getPoseStack(), event.getMultiBufferSource(), player.level(), event.getPackedLight(), 0,
                player.getId());
        event.getPoseStack().popPose();
    }

    private static boolean isActiveBoard(AbstractClientPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RWBYMWeaponItem.isActiveKineticBoard(stack, player);
    }

    private static boolean hideSkinLayersForArmor(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model) {
        boolean changed = false;
        if (isRwbyArmor(player, EquipmentSlot.HEAD)) {
            model.hat.visible = false;
            changed = true;
        }
        if (isRwbyArmor(player, EquipmentSlot.CHEST)) {
            model.jacket.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
            changed = true;
        }
        if (isRwbyArmor(player, EquipmentSlot.LEGS)) {
            model.leftPants.visible = false;
            model.rightPants.visible = false;
            changed = true;
        }
        return changed;
    }

    private static boolean hideReplacedLimbsForAppearance(AbstractClientPlayer player,
            PlayerModel<AbstractClientPlayer> model) {
        boolean changed = false;
        if (hasAppearance(player, "Head")) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Original LayerAccessories used replace=true for these slots; hiding before base render avoids overlap.
            model.head.visible = false;
            model.hat.visible = false;
            changed = true;
        }
        if (hasAppearance(player, "RightArm")) {
            model.rightArm.visible = false;
            model.rightSleeve.visible = false;
            changed = true;
        }
        if (hasAppearance(player, "LeftArm")) {
            model.leftArm.visible = false;
            model.leftSleeve.visible = false;
            changed = true;
        }
        if (hasAppearance(player, "RightLeg")) {
            model.rightLeg.visible = false;
            model.rightPants.visible = false;
            changed = true;
        }
        if (hasAppearance(player, "LeftLeg")) {
            model.leftLeg.visible = false;
            model.leftPants.visible = false;
            changed = true;
        }
        if (hasAppearance(player, "Body")) {
            model.body.visible = false;
            model.jacket.visible = false;
            changed = true;
        }
        return changed;
    }

    private static boolean hasAppearance(AbstractClientPlayer player, String slot) {
        return !RWBYMLimbItem.getAppearance(player, slot).isBlank();
    }

    private static boolean isRwbyArmor(AbstractClientPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.getItem() instanceof RWBYMArmorItem;
    }

    private record SkinLayerState(boolean head, boolean hat, boolean body, boolean jacket, boolean leftArm,
            boolean rightArm, boolean leftSleeve, boolean rightSleeve, boolean leftLeg, boolean rightLeg,
            boolean leftPants, boolean rightPants) {
        static SkinLayerState capture(PlayerModel<AbstractClientPlayer> model) {
            return new SkinLayerState(model.head.visible, model.hat.visible, model.body.visible,
                    model.jacket.visible, model.leftArm.visible, model.rightArm.visible, model.leftSleeve.visible,
                    model.rightSleeve.visible, model.leftLeg.visible, model.rightLeg.visible, model.leftPants.visible,
                    model.rightPants.visible);
        }

        void restore(PlayerModel<AbstractClientPlayer> model) {
            model.head.visible = this.head;
            model.hat.visible = this.hat;
            model.body.visible = this.body;
            model.jacket.visible = this.jacket;
            model.leftArm.visible = this.leftArm;
            model.rightArm.visible = this.rightArm;
            model.leftSleeve.visible = this.leftSleeve;
            model.rightSleeve.visible = this.rightSleeve;
            model.leftLeg.visible = this.leftLeg;
            model.rightLeg.visible = this.rightLeg;
            model.leftPants.visible = this.leftPants;
            model.rightPants.visible = this.rightPants;
        }
    }
}
