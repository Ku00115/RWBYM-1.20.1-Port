package io.github.blaezdev.rwbym;

import io.github.blaezdev.rwbym.registry.RWBYMBlocks;
import io.github.blaezdev.rwbym.registry.RWBYMBlockEntities;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMEnchantments;
import io.github.blaezdev.rwbym.registry.RWBYMFluids;
import io.github.blaezdev.rwbym.registry.RWBYMCreativeTabs;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.registry.RWBYMLootModifiers;
import io.github.blaezdev.rwbym.registry.RWBYMMenuTypes;
import io.github.blaezdev.rwbym.registry.RWBYMMobEffects;
import io.github.blaezdev.rwbym.registry.RWBYMSounds;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(RWBYM.MOD_ID)
public final class RWBYM {
    public static final String MOD_ID = "rwbym";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public RWBYM() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        RWBYMBlocks.BLOCKS.register(modBus);
        RWBYMBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        RWBYMEntityTypes.ENTITY_TYPES.register(modBus);
        RWBYMEnchantments.ENCHANTMENTS.register(modBus);
        RWBYMFluids.FLUID_TYPES.register(modBus);
        RWBYMFluids.FLUIDS.register(modBus);
        RWBYMItems.ITEMS.register(modBus);
        RWBYMLootModifiers.LOOT_MODIFIERS.register(modBus);
        RWBYMMenuTypes.MENU_TYPES.register(modBus);
        RWBYMMobEffects.MOB_EFFECTS.register(modBus);
        RWBYMSounds.SOUND_EVENTS.register(modBus);
        RWBYMCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        RWBYMNetwork.register();
        LOGGER.info("Loaded RWBYM 1.20.1 port scaffold");
    }
}
