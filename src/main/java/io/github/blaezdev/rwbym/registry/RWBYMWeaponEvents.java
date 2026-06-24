package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.RWBYMWeaponModifierHelper;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RWBYMWeaponEvents {
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            RWBYMWeaponModifierHelper.applyDefaultModifiersIfMissing(
                    event.getCrafting(), event.getEntity().getRandom());
        }
    }

    private RWBYMWeaponEvents() {
    }
}
