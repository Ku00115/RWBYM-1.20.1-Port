package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.RWBYMGliderItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RWBYMPlayerEvents {
    @SubscribeEvent
    public static void pickupExperience(PlayerXpEvent.PickupXp event) {
        if (!event.getEntity().level().isClientSide()
                && event.getEntity().getItemBySlot(EquipmentSlot.FEET)
                        .is(RWBYMItems.SIMPLE_ITEMS.get("relicofknowledge").get())) {
            event.getEntity().giveExperiencePoints(10);
        }
    }

    @SubscribeEvent
    public static void tickGliderFlight(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        ItemStack active = player.getUseItem();
        if (!(active.getItem() instanceof RWBYMGliderItem) || !RWBYMGliderItem.isUsable(active)) {
            return;
        }
        if (player.onGround() || player.isPassenger() || player.isInWaterOrBubble() || player.getAbilities().flying) {
            return;
        }
        if (player.getDeltaMovement().y < 0.0D || player.isFallFlying()) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // 1.12 kept RWBYGliderItem active by repeatedly setting Elytra flight on the server tick.
            player.startFallFlying();
            player.fallDistance = 1.0F;
        }
    }

    private RWBYMPlayerEvents() {
    }
}
