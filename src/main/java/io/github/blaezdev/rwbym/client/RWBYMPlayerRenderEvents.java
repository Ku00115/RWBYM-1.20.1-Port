package io.github.blaezdev.rwbym.client;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.RWBYMArmorItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
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

        if (hideSkinLayersForArmor(player, model)) {
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

    private static boolean isRwbyArmor(AbstractClientPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.getItem() instanceof RWBYMArmorItem;
    }

    private record SkinLayerState(boolean hat, boolean jacket, boolean leftSleeve, boolean rightSleeve,
            boolean leftPants, boolean rightPants) {
        static SkinLayerState capture(PlayerModel<AbstractClientPlayer> model) {
            return new SkinLayerState(model.hat.visible, model.jacket.visible, model.leftSleeve.visible,
                    model.rightSleeve.visible, model.leftPants.visible, model.rightPants.visible);
        }

        void restore(PlayerModel<AbstractClientPlayer> model) {
            model.hat.visible = this.hat;
            model.jacket.visible = this.jacket;
            model.leftSleeve.visible = this.leftSleeve;
            model.rightSleeve.visible = this.rightSleeve;
            model.leftPants.visible = this.leftPants;
            model.rightPants.visible = this.rightPants;
        }
    }
}
