package io.github.blaezdev.rwbym.capability;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.aura.AuraProvider;
import io.github.blaezdev.rwbym.capability.semblance.SemblanceProvider;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge event bridge for RWBYM player capabilities.
 *
 * <p>This class attaches Aura and Semblance capabilities to players, copies them during
 * clone events, performs server tick upkeep, and applies Semblance-specific combat hooks.
 * It is the main runtime owner for {@code Aura.java} and {@code Semblance.java}.</p>
 *
 * <p>Linked files: {@code RWBYMCapabilities.java}, {@code AuraProvider.java},
 * {@code SemblanceProvider.java}, {@code RWBYMNetwork.java}, and {@code RWBYMLimbItem.java}.</p>
 */
@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RWBYMCapabilityEvents {
    private static final ResourceLocation AURA_ID = new ResourceLocation(RWBYM.MOD_ID, "aura");
    private static final ResourceLocation SEMBLANCE_ID = new ResourceLocation(RWBYM.MOD_ID, "semblance");
    private static final String JOINED_BEFORE_KEY = RWBYM.MOD_ID + ".joinedBefore";

    @SubscribeEvent
    /**
     * Attaches persistent Aura/Semblance capability providers to every player entity.
     */
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            // Providers are created per player so LazyOptional invalidation stays scoped to that entity.
            AuraProvider auraProvider = new AuraProvider();
            SemblanceProvider semblanceProvider = new SemblanceProvider();
            event.addCapability(AURA_ID, auraProvider);
            event.addCapability(SEMBLANCE_ID, semblanceProvider);
            event.addListener(auraProvider::invalidate);
            event.addListener(semblanceProvider::invalidate);
        }
    }

    @SubscribeEvent
    /**
     * Copies player-owned capability state during death respawn and dimension clone events.
     */
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
        copyPersistedFlag(event.getOriginal(), event.getEntity(), JOINED_BEFORE_KEY);
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    /**
     * Handles first-login starter items and pushes initial client sync state.
     */
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.getPersistentData().getBoolean(JOINED_BEFORE_KEY)) {
            // Persistent data avoids re-granting the scroll after death or reconnect.
            player.getPersistentData().putBoolean(JOINED_BEFORE_KEY, true);
            giveFirstSpawnScroll(player);
            refillAura(player);
        }
        if (player.getPersistentData().contains(RWBYMLimbItem.DATA_KEY)) {
            RWBYMNetwork.syncAppearance(player);
        }
        RWBYMNetwork.syncSemblance(player);
    }

    @SubscribeEvent
    /**
     * Refills Aura and re-syncs Semblance state after respawn.
     */
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refillAura(player);
            RWBYMNetwork.syncSemblance(player);
        }
    }

    @SubscribeEvent
    /**
     * Syncs cosmetic appearance to new trackers when another player comes into range.
     */
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getTarget().getPersistentData().contains(RWBYMLimbItem.DATA_KEY)) {
            RWBYMNetwork.syncAppearanceTo(event.getTarget(), player);
        }
    }

    @SubscribeEvent
    /**
     * Runs server-side Aura regeneration and Semblance behavior.
     */
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            event.player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.tick(event.player));
            event.player.getCapability(RWBYMCapabilities.SEMBLANCE)
                    .ifPresent(semblance -> semblance.tick(event.player));
            // Periodic sync keeps client HUD/key-side state fresh without sending every tick.
            if (event.player instanceof ServerPlayer player && player.tickCount % 20 == 0) {
                RWBYMNetwork.syncSemblance(player);
            }
        }
    }

    @SubscribeEvent
    /**
     * Applies original Semblance combat side effects that were event-driven in 1.12.2.
     */
    public static void modifySemblanceDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        LivingEntity target = event.getEntity();
        player.getCapability(RWBYMCapabilities.SEMBLANCE).ifPresent(semblance -> {
            int level = semblance.getLevel();
            switch (semblance.getName()) {
                // Clover grants deterministic good-luck damage scaling by level.
                case "clover" -> event.setAmount(event.getAmount() * (1.1F + 0.1F * Math.min(3, level)));
                case "qrow" -> event.setAmount(event.getAmount()
                        // Qrow intentionally randomizes damage around the original per-level floor.
                        * (player.getRandom().nextFloat() + 0.4F + 0.1F * Math.min(3, level)));
                case "lysette" -> {
                    if (semblance.isActive()) {
                        // Lysette spends extra Aura on hit to apply the original extreme slowness control.
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, level * 150,
                                128, true, false));
                        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.useAura(15.0F, false));
                    }
                }
                case "harriet" -> {
                    if (semblance.isActive()) {
                        // Harriet's active hit effect pushes targets away and punishes the attacker with hunger.
                        target.knockback(level, -player.getLookAngle().x, -player.getLookAngle().z);
                        if (level > 4) {
                            target.hurt(player.damageSources().magic(), 12.0F);
                        }
                        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 5, 126, true, false));
                        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.useAura(15.0F, false));
                    }
                }
                default -> {
                }
            }
        });
    }

    private RWBYMCapabilityEvents() {
    }

    private static void giveFirstSpawnScroll(ServerPlayer player) {
        ItemStack scroll = new ItemStack(RWBYMItems.SIMPLE_ITEMS.get("scroll").get());
        Inventory inventory = player.getInventory();
        if (!inventory.add(scroll)) {
            // Dropping preserves the original grant even when a modpack starts players with a full inventory.
            player.drop(scroll, false);
        }
    }

    private static void refillAura(Player player) {
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.setAmount(aura.getMaxAura()));
    }

    private static void copyPersistedFlag(Player original, Player copy, String key) {
        if (original.getPersistentData().contains(key)) {
            copy.getPersistentData().putBoolean(key, original.getPersistentData().getBoolean(key));
        }
    }
}
