package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.RWBYMArmorItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RWBYMCombatEvents {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof Player attacker && RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.KINGSPAWN)) {
            boolean supportedByGambit = attacker.level().getEntitiesOfClass(Player.class,
                    attacker.getBoundingBox().inflate(10.0D),
                    player -> player != attacker && RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.KINGSGAMBIT))
                    .size() > 0;
            if (supportedByGambit) {
                event.setAmount(event.getAmount() * 1.5F);
            }
        }
    }

    private RWBYMCombatEvents() {
    }
}
