package io.github.blaezdev.rwbym.capability;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.aura.AuraProvider;
import io.github.blaezdev.rwbym.capability.semblance.SemblanceProvider;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RWBYMCapabilityEvents {
    private static final ResourceLocation AURA_ID = new ResourceLocation(RWBYM.MOD_ID, "aura");
    private static final ResourceLocation SEMBLANCE_ID = new ResourceLocation(RWBYM.MOD_ID, "semblance");

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(AURA_ID, new AuraProvider());
            event.addCapability(SEMBLANCE_ID, new SemblanceProvider());
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(RWBYMCapabilities.AURA).ifPresent(oldAura ->
                event.getEntity().getCapability(RWBYMCapabilities.AURA).ifPresent(newAura -> newAura.copyFrom(oldAura)));
        event.getOriginal().getCapability(RWBYMCapabilities.SEMBLANCE).ifPresent(oldSemblance ->
                event.getEntity().getCapability(RWBYMCapabilities.SEMBLANCE)
                        .ifPresent(newSemblance -> newSemblance.copyFrom(oldSemblance)));
        if (event.getOriginal().getPersistentData().contains(RWBYMLimbItem.DATA_KEY)) {
            event.getEntity().getPersistentData().put(RWBYMLimbItem.DATA_KEY,
                    event.getOriginal().getPersistentData().getCompound(RWBYMLimbItem.DATA_KEY).copy());
        }
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.getPersistentData().contains(RWBYMLimbItem.DATA_KEY)) {
            RWBYMNetwork.syncAppearance(player);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getTarget().getPersistentData().contains(RWBYMLimbItem.DATA_KEY)) {
            RWBYMNetwork.syncAppearanceTo(event.getTarget(), player);
        }
    }

    @SubscribeEvent
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            event.player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.tick(event.player));
            event.player.getCapability(RWBYMCapabilities.SEMBLANCE)
                    .ifPresent(semblance -> semblance.tick(event.player));
        }
    }

    private RWBYMCapabilityEvents() {
    }
}
