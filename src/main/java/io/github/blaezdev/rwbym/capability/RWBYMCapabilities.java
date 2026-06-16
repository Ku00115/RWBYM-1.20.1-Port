package io.github.blaezdev.rwbym.capability;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.aura.IAura;
import io.github.blaezdev.rwbym.capability.semblance.ISemblance;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RWBYMCapabilities {
    public static final Capability<IAura> AURA = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ISemblance> SEMBLANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IAura.class);
        event.register(ISemblance.class);
    }

    private RWBYMCapabilities() {
    }
}
