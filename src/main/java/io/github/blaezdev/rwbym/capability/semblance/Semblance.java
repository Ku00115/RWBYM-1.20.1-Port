package io.github.blaezdev.rwbym.capability.semblance;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.capability.aura.IAura;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.entity.BlakeSummonEntity;
import io.github.blaezdev.rwbym.entity.RagoraEntity;
import io.github.blaezdev.rwbym.entity.RenSummonEntity;
import io.github.blaezdev.rwbym.entity.WinterArmorgeistEntity;
import io.github.blaezdev.rwbym.entity.WinterSummonEntity;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Server-authoritative implementation of RWBYM player Semblances.
 *
 * <p>This class ports the original Blaez_Dev 1.12.2 design, where every character
 * had a distinct capability, into a single Forge 1.20.1 capability that dispatches
 * by {@link #name}. It owns active state, selected level, cooldowns, Aura drain,
 * summons, particles, movement impulses, damage modifiers, and NBT persistence.</p>
 *
 * <p>Linked files: {@code ISemblance.java} defines the capability contract,
 * {@code RWBYMCapabilityEvents.java} calls {@link #tick(Player)} and combat hooks,
 * {@code SemblanceActionPacket.java} sends key intent, {@code SemblanceSyncPacket.java}
 * mirrors NBT to the client, and {@code RWBYMEntityTypes.java} supplies summon entities.</p>
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
public class Semblance implements ISemblance {
    // Original RWBYConfig used 250 ticks to delay Aura recharge after Semblance drain.
    private static final int AURA_RECHARGE_DELAY = 250;

    // Drain rates mirror the old RWBYConfig defaults so balancing stays close to 1.12.2.
    private static final float BLAKE_DRAIN = 5.0F;
    private static final float NORA_DRAIN = 0.5F;
    private static final float REN_DRAIN = 0.3F;
    private static final float RUBY_DRAIN = 0.1F;
    private static final float YANG_DRAIN = 0.3F;
    private static final float JAUNE_TRANSFER = 0.15F;
    private static final float HARRIET_DRAIN = 0.05F;
    private static final int HARRIET_SPEED_MULTIPLIER = 5;

    private static final UUID DUST_SPEED_MODIFIER = UUID.fromString("8d07e08c-c9b3-4e52-b499-8d36f4be8ca0");
    private static final UUID DUST_ARMOR_MODIFIER = UUID.fromString("fdc03815-3173-4e9c-9e9d-0812588b5637");
    private static final UUID DUST_HEALTH_MODIFIER = UUID.fromString("107f8be6-056f-4d55-8e5d-f9f6b37a2c18");
    private static final UUID DUST_ATTACK_MODIFIER = UUID.fromString("eb264e05-eb9e-4324-9bd3-b99d7c96dfc6");
    private static final UUID DUST_ATTACK_SPEED_MODIFIER = UUID.fromString("fc53ab3d-2b6a-46ba-a944-e060b74495d7");

    private static final int RAGORA_MAX_LEVEL = 4;
    private static final int RAGORA_SUMMON_TIME = 100;
    private static final int RAGORA_COOLDOWN = 100;
    private static final int RAGORA_SUMMON_PARTICLES = 100;
    private static final float RAGORA_INITIAL_AURA_COST = 1.0F;
    private static final float RAGORA_UPKEEP_AURA_COST = 0.1F;

    private static final int[] WEISS_COOLDOWNS = {100, 300, 500, 1200};
    private static final float[] WEISS_AURA_COSTS = {5.0F, 15.0F, 25.0F, 60.0F};

    private String name = "blank";
    private boolean active;
    private int level = 1;
    private int selectedLevel = 1;
    private int timer;
    private int cooldown;

    private int blakeShadows;
    private int blakeShadowCooldown;
    private int blakeShadowCooldownTime = 200;
    private int blakeActiveTicks;
    private int blakeAirTime;

    private final List<UUID> jauneTargets = new ArrayList<>(5);

    private int ragoraCooldown;
    private int ragoraSummonTime;
    @Nullable
    private UUID ragoraEntityUuid;
    @Nullable
    private RagoraEntity ragoraEntity;

    /**
     * Main server-side dispatcher for all Semblance tick behavior.
     *
     * <p>Called by {@code RWBYMCapabilityEvents.tickPlayer}. Keeping the dispatch here
     * avoids reintroducing the 1.12 capability-per-character tree while still keeping
     * each character behavior independent.</p>
     */
    @Override
    public void tick(Player player) {
        tickDustSemblanceModifiers(player);
        if ("ragora".equals(this.name)) {
            tickRagora(player);
            return;
        }

        // Switching away from Ragora must clean up its owned entity before another ability takes over.
        if (hasRagoraState()) {
            stopRagora(player, true);
        }

        tickSharedCooldown();
        switch (this.name) {
            case "ruby" -> tickRuby(player);
            case "blake" -> tickBlake(player);
            case "weiss" -> tickWeiss();
            case "yang" -> tickYang(player);
            case "nora" -> tickNora(player);
            case "ren" -> tickRen(player);
            case "jaune" -> tickJaune(player);
            case "harriet" -> tickHarriet(player);
            case "lysette" -> tickLysette(player);
            case "qrow" -> tickQrow(player);
            case "clover" -> tickClover(player);
            case "valour" -> {
            }
            case "fall" -> tickFall(player);
            case "hazel", "pyrrha", "penny", "raven" -> this.active = false;
            case "blank" -> {
            }
            default -> this.active = false;
        }
    }

    /**
     * Handles the key-press edge sent by {@code SemblanceActionPacket}.
     *
     * <p>Instant abilities perform their action here, while sustained abilities set
     * {@link #active} and continue through {@link #tick(Player)}.</p>
     */
    @Override
    public boolean activate(Player player) {
        return switch (this.name) {
            case "ruby" -> activateRuby(player);
            case "blake" -> activateBlake(player);
            case "weiss" -> activateWeiss(player);
            case "yang", "nora", "harriet" -> activateTimed(120);
            case "ren" -> activateRen(player);
            case "jaune" -> activateJaune(player);
            case "lysette" -> activateContinuous();
            case "valour" -> activateValour(player);
            case "fall" -> activateRuby(player);
            case "hazel", "pyrrha", "penny", "raven" -> false;
            case "ragora" -> {
                this.active = true;
                yield true;
            }
            case "blank" -> false;
            default -> false;
        };
    }

    /**
     * Handles the key-release edge sent by {@code SemblanceActionPacket}.
     *
     * <p>Only original release-driven abilities stop here; timed abilities such as
     * Yang, Nora, Ren, and Harriet intentionally run out through their timers.</p>
     */
    @Override
    public boolean deactivate(Player player) {
        if ("ruby".equals(this.name)) {
            this.active = false;
            return true;
        }
        if ("fall".equals(this.name) || "lysette".equals(this.name)) {
            this.active = false;
            return true;
        }
        if ("ragora".equals(this.name)) {
            stopRagora(player, true);
            return true;
        }
        return false;
    }

    /**
     * Restores the old selected-level keybind used by Ruby, Weiss, Fall, and other
     * multi-rank Semblances.
     */
    @Override
    public boolean cycleSelectedLevel(Player player) {
        if (this.selectedLevel == -1 || this.level <= 0) {
            return false;
        }
        this.selectedLevel = this.selectedLevel >= this.level ? 1 : this.selectedLevel + 1;
        player.sendSystemMessage(Component.literal("Level Set to: " + this.selectedLevel)
                .withStyle(ChatFormatting.GRAY));
        return true;
    }

    /**
     * Starts Ruby/Fall movement. Level 1 is ground-only, while higher levels can fly.
     */
    private boolean activateRuby(Player player) {
        return switch (this.selectedLevel) {
            case 1 -> {
                if (!player.onGround()) {
                    yield false;
                }
                this.active = true;
                yield true;
            }
            case 2, 3 -> {
                this.active = true;
                yield true;
            }
            default -> false;
        };
    }

    /**
     * Consumes one stored Blake shadow and spawns the correct clone variant.
     */
    private boolean activateBlake(Player player) {
        // The old cooldown scales down with sqrt(level), letting higher levels regain shadows faster.
        this.blakeShadowCooldownTime = Math.max(1, (int) (200.0D / Math.sqrt(Math.max(1, this.level))));
        if (this.blakeShadows <= 0 || !useAura(player, BLAKE_DRAIN)) {
            return false;
        }
        spawnBlakeShadow(player);
        player.fallDistance = 0.0F;
        this.blakeActiveTicks = 10;
        this.blakeShadows--;
        return true;
    }

    /**
     * Spawns Weiss winter summons using the original selected-level cost/cooldown table.
     */
    private boolean activateWeiss(Player player) {
        // Clamp selected level because sync/NBT may briefly hold stale values after a coin change.
        int selected = Math.max(1, Math.min(this.selectedLevel, WEISS_AURA_COSTS.length));
        if (!useAura(player, WEISS_AURA_COSTS[selected - 1]) || this.cooldown > 0 || !player.onGround()) {
            return false;
        }
        this.cooldown = WEISS_COOLDOWNS[selected - 1];
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return true;
        }
        switch (selected) {
            case 1 -> spawnWinterSummon(serverLevel, player, RWBYMEntityTypes.WINTER_BOARBATUSK);
            case 2 -> spawnWinterSummon(serverLevel, player, RWBYMEntityTypes.WINTER_BEOWOLF);
            case 3 -> spawnWinterSummon(serverLevel, player, RWBYMEntityTypes.WINTER_URSA);
            case 4 -> spawnWinterArmorgeist(serverLevel, player);
            default -> {
            }
        }
        return true;
    }

    /**
     * Starts a timed active window for burst Semblances.
     */
    private boolean activateTimed(int ticks) {
        if (this.level <= 0) {
            return false;
        }
        this.active = true;
        this.timer = ticks;
        return true;
    }

    /**
     * Starts a hold-to-maintain Semblance that is stopped by key release.
     */
    private boolean activateContinuous() {
        if (this.level <= 0) {
            return false;
        }
        this.active = true;
        return true;
    }

    /**
     * Performs Valour's short flash movement.
     *
     * <p>The 1.12 version sent a custom MoveMessage to the player. The port uses
     * {@link Player#move(MoverType, Vec3)} server-side so collision rules still apply
     * and the server remains authoritative.</p>
     */
    // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
    private boolean activateValour(Player player) {
        if (this.level <= 0 || !player.onGround() || !hasValourFlashSurface(player)
                || currentAura(player) <= 10.0F || !useAura(player, 0.5F)) {
            return false;
        }
        // Original Valour scales flash distance linearly with Semblance level.
        Vec3 dash = player.getLookAngle().scale(6.0D * this.level);
        player.move(MoverType.SELF, dash);
        player.level().playSound(null, player.blockPosition(), SoundEvents.WEATHER_RAIN,
                SoundSource.PLAYERS, 0.4F, 1.0F);
        return true;
    }

    /**
     * Starts Ren's mask effect and creates the original decoy summon.
     */
    private boolean activateRen(Player player) {
        if (!activateTimed(120)) {
            return false;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            RenSummonEntity ren = RWBYMEntityTypes.REN.get().create(serverLevel);
            if (ren != null) {
                ren.moveTo(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
                serverLevel.addFreshEntity(ren);
            }
        }
        return true;
    }

    /**
     * Toggles Jaune's Aura transfer links to nearby players.
     */
    private boolean activateJaune(Player player) {
        if (!this.jauneTargets.isEmpty()) {
            this.jauneTargets.clear();
            return true;
        }
        // The original stored at most five linked player UUIDs for ongoing Aura transfer.
        for (Player other : player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(5.0D),
                other -> other != player && other.isAlive())) {
            if (this.jauneTargets.size() >= 5) {
                break;
            }
            this.jauneTargets.add(other.getUUID());
        }
        return !this.jauneTargets.isEmpty();
    }

    /**
     * Runs Ruby's movement, invisibility, petal trail, and high-level collision damage.
     */
    private void tickRuby(Player player) {
        if (!this.active || !useAura(player, RUBY_DRAIN)) {
            this.active = false;
            return;
        }
        if (!player.onGround() && this.selectedLevel <= 1) {
            return;
        }

        player.fallDistance = 0.0F;
        Vec3 look = player.getLookAngle();
        Vec3 motion = player.getDeltaMovement();
        boolean fallFlying = player.isFallFlying();
        // Low drag blends current momentum into the look vector instead of snapping instantly.
        double drag = 0.1D;
        double x = motion.x / (fallFlying ? 0.99D : 0.91D);
        double y = motion.y / 0.98D + (fallFlying ? 0.0D : 0.08D);
        double z = motion.z / (fallFlying ? 0.99D : 0.91D);
        Vec3 adjusted = new Vec3(x, y, z);
        // Preserve current speed while rotating motion toward the direction the player is facing.
        Vec3 scaledLook = look.scale(adjusted.length());
        double nx = Math.abs(scaledLook.x) < Math.abs(look.x) ? look.x : x + (look.x - x) * drag;
        double ny = Math.abs(scaledLook.y) < Math.abs(look.y) ? look.y : y + (look.y - y) * drag;
        double nz = Math.abs(scaledLook.z) < Math.abs(look.z) ? look.z : z + (look.z - z) * drag;
        player.setDeltaMovement(nx, ny, nz);
        player.hasImpulse = true;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 0, true, false));
        if (this.selectedLevel > 1) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, true, false));
        }
        if (this.selectedLevel > 2) {
            damageRubyCollision(player);
        }
        spawnRubyPetals(player);
    }

    /**
     * Regenerates Blake shadow charges and applies the brief burst movement after activation.
     */
    private void tickBlake(Player player) {
        // Air time gates the stronger ground burst from being used repeatedly while airborne.
        this.blakeAirTime = player.onGround() ? 0 : this.blakeAirTime + 1;
        if (this.blakeActiveTicks > 0) {
            if (this.blakeActiveTicks > 8) {
                if (!useAura(player, BLAKE_DRAIN)) {
                    this.blakeActiveTicks = 0;
                    return;
                }
                applyBlakeBurst(player);
            }
            this.blakeActiveTicks--;
        }

        // Blake regains one shadow per cooldown window until reaching the current Semblance level.
        if (this.blakeShadows < this.level) {
            if (this.blakeShadowCooldown > 0) {
                this.blakeShadowCooldown--;
            } else {
                this.blakeShadowCooldown = this.blakeShadowCooldownTime;
                this.blakeShadows++;
            }
        }
        if (this.blakeShadows > this.level) {
            this.blakeShadows = this.level;
        }
    }

    /**
     * Ticks Weiss summon cooldown.
     */
    private void tickWeiss() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
    }

    /**
     * Applies Yang's passive/active strength scaling from missing Aura.
     */
    private void tickYang(Player player) {
        if (player.isSpectator()) {
            return;
        }
        float percentage = auraPercentage(player);
        if (this.active) {
            if (!useAura(player, YANG_DRAIN) || percentage < 0.01F) {
                this.active = false;
                return;
            }
            // Original Yang doubles the missing-Aura damage boost while actively empowered.
            int strength = Math.round((1.0F - percentage) * 6.0F * 2.0F * this.level);
            if (strength > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, strength, false, false));
            }
        } else {
            // Passive Yang still rewards low Aura, but at a much lower multiplier.
            int strength = Math.round((1.0F - percentage) * 6.0F / 2.0F * this.level);
            if (strength > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, strength, false, false));
            }
        }
        tickTimedActive();
    }

    /**
     * Applies Nora's lightning-crystal empowerment window.
     */
    private void tickNora(Player player) {
        ItemStack offhand = player.getOffhandItem();
        boolean hasLightningCrystal = itemPath(offhand).equals("lightdustcrystal");
        if (this.active && hasLightningCrystal && auraPercentage(player) > 0.01F && useAura(player, NORA_DRAIN)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60 * this.level, 8 * this.level, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * this.level, 6 * this.level, false, false));
        }
        tickTimedActive();
    }

    /**
     * Applies Ren's concealment, ally masking, and shift-based glow reveal.
     */
    private void tickRen(Player player) {
        if (this.level > 1 && player.isShiftKeyDown()) {
            // Level 2+ Ren can reveal nearby living entities while crouching at an Aura trickle cost.
            AABB area = player.getBoundingBox().inflate(12.0D * this.level);
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != player)) {
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, this.level * 20, 1, true, false));
                useAura(player, 0.1F);
            }
        }

        if (this.active && auraPercentage(player) > 0.01F && useAura(player, REN_DRAIN)) {
            int duration = Math.round(this.level * 90.0F);
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 1, true, false));
            if (this.level > 1) {
                List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class,
                        player.getBoundingBox().inflate(4.0D), other -> other != player);
                if (!nearbyPlayers.isEmpty()) {
                    // Original Ren chooses one random nearby ally instead of always masking list order.
                    Player other = nearbyPlayers.get(player.getRandom().nextInt(nearbyPlayers.size()));
                    other.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 1, true, false));
                }
            }
        }
        tickTimedActive();
    }

    /**
     * Transfers Jaune Aura to linked players and blocks horizontal movement while channeling.
     */
    private void tickJaune(Player player) {
        if (this.jauneTargets.isEmpty()) {
            return;
        }
        player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
        player.hasImpulse = true;
        this.jauneTargets.removeIf(uuid -> transferJauneAura(player, uuid));
    }

    /**
     * Applies Harriet's speed window and crit-particle feedback.
     */
    private void tickHarriet(Player player) {
        int duration = Math.round(this.level * 90.0F);
        if (this.active && auraPercentage(player) > 0.01F && useAura(player, HARRIET_DRAIN * this.level)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration,
                    this.level * HARRIET_SPEED_MULTIPLIER, true, false));
        }
        if (this.timer > 0 && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    player.getX() + 1.0D - player.getRandom().nextInt(2),
                    player.getY() + 1.0D,
                    player.getZ() + 1.0D - player.getRandom().nextInt(2),
                    1,
                    3.0D - player.getRandom().nextInt(6),
                    3.0D - player.getRandom().nextInt(6),
                    3.0D - player.getRandom().nextInt(6),
                    0.0D);
        }
        tickTimedActive();
    }

    /**
     * Applies Lysette's water-freezing aura and high-level defensive knockback.
     */
    private void tickLysette(Player player) {
        if (!this.active || auraPercentage(player) <= 0.01F || !useAura(player, 0.05F)) {
            if (auraPercentage(player) <= 0.01F) {
                this.active = false;
            }
            return;
        }
        freezeWaterUnderfoot(player);
        if (this.level > 2 && player.isBlocking()) {
            AABB area = player.getBoundingBox().inflate(3.0D);
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != player)) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, this.level * 150,
                        124, true, false));
                useAura(player, 5.0F);
                entity.knockback(this.level + 2, -player.getLookAngle().x, -player.getLookAngle().z);
            }
        }
    }

    /**
     * Applies Qrow's bad-luck passive and periodically throws negative effects to nearby players.
     */
    private void tickQrow(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, Integer.MAX_VALUE, 8 * this.level, false, false));
        tickLuckTransfer(player, false);
    }

    /**
     * Applies Clover's good-luck passive and periodically throws positive effects to nearby players.
     */
    private void tickClover(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, Integer.MAX_VALUE, 8 * this.level, false, false));
        tickLuckTransfer(player, true);
    }

    /**
     * Runs Fall Maiden movement. The original class mirrors Ruby movement but has no drain rate.
     */
    private void tickFall(Player player) {
        if (!this.active || auraPercentage(player) <= 0.01F) {
            this.active = false;
            return;
        }
        tickMovementSemblance(player, false);
    }

    /**
     * Converts Ruby/Fall high-speed movement into contact damage.
     */
    private void damageRubyCollision(Player player) {
        double speed = player.getDeltaMovement().length();
        float damage = (float) (speed * 10.0D);
        if (damage <= 0.0F) {
            return;
        }
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(2.0D), entity -> entity != player)) {
            entity.hurt(player.damageSources().playerAttack(player), damage);
        }
    }

    /**
     * Shared Ruby/Fall movement math ported from the original motion-vector logic.
     */
    // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
    private void tickMovementSemblance(Player player, boolean consumeAura) {
        if (consumeAura && !useAura(player, RUBY_DRAIN)) {
            this.active = false;
            return;
        }
        if (!player.onGround() && this.selectedLevel <= 1) {
            return;
        }

        player.fallDistance = 0.0F;
        Vec3 look = player.getLookAngle();
        Vec3 motion = player.getDeltaMovement();
        boolean fallFlying = player.isFallFlying();
        double x = motion.x / (fallFlying ? 0.99D : 0.91D);
        double y = motion.y / 0.98D + (fallFlying ? 0.0D : 0.08D);
        double z = motion.z / (fallFlying ? 0.99D : 0.91D);
        Vec3 adjusted = new Vec3(x, y, z);
        // Match the legacy feel by preserving current speed while steering toward look direction.
        Vec3 scaledLook = look.scale(adjusted.length());
        double nx = Math.abs(scaledLook.x) < Math.abs(look.x) ? look.x : x + (look.x - x) * 0.1D;
        double ny = Math.abs(scaledLook.y) < Math.abs(look.y) ? look.y : y + (look.y - y) * 0.1D;
        double nz = Math.abs(scaledLook.z) < Math.abs(look.z) ? look.z : z + (look.z - z) * 0.1D;
        player.setDeltaMovement(nx, ny, nz);
        player.hasImpulse = true;
        if (this.selectedLevel > 2) {
            damageRubyCollision(player);
        }
        spawnRubyPetals(player);
    }

    /**
     * Freezes source water under Lysette, matching the Frost Walker-style original behavior.
     */
    private void freezeWaterUnderfoot(Player player) {
        int radius = Math.min(16, 2 + this.level);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Keep the frozen area circular so higher levels expand smoothly around the player.
                if (x * x + z * z > radius * radius) {
                    continue;
                }
                var pos = player.blockPosition().offset(x, -1, z);
                var state = player.level().getBlockState(pos);
                if (state.getFluidState().isSourceOfType(Fluids.WATER)
                        && player.level().getBlockState(pos.above()).isAir()) {
                    player.level().setBlock(pos, Blocks.FROSTED_ICE.defaultBlockState(), 3);
                    player.level().scheduleTick(pos, Blocks.FROSTED_ICE, 60 + player.getRandom().nextInt(61));
                }
            }
        }
    }

    /**
     * Periodically transfers Qrow/Clover fortune effects to a nearby player.
     */
    private void tickLuckTransfer(Player player, boolean positive) {
        if (this.cooldown > 0) {
            return;
        }
        List<Player> nearby = player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(20.0D),
                other -> other != player && other.isAlive());
        if (nearby.isEmpty()) {
            return;
        }
        // The legacy behavior chooses a random nearby player so the passive feels unpredictable.
        Player target = nearby.get(player.getRandom().nextInt(nearby.size()));
        MobEffectsHolder effects = positive ? MobEffectsHolder.POSITIVE : MobEffectsHolder.NEGATIVE;
        target.addEffect(new MobEffectInstance(effects.first(player), 600, 1, true, false));
        target.addEffect(new MobEffectInstance(effects.second(player), 600, 1, true, false));
        this.cooldown = 1200 + player.getRandom().nextInt(2400);
    }

    /**
     * Recreates Valour's legacy check that requires nearby solid footing before flashing.
     */
    private boolean hasValourFlashSurface(Player player) {
        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                if (!player.level().getBlockState(player.blockPosition().offset(0, y, z)).isAir()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Emits Ruby/Fall rose-petal feedback around the moving player.
     */
    private void spawnRubyPetals(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        // Higher selected levels are intentionally much denser to match the original visual burst.
        int count = this.selectedLevel > 1 ? 32 : 2;
        for (int i = 0; i < count; i++) {
            serverLevel.sendParticles(ParticleTypes.CHERRY_LEAVES,
                    player.getX() + (player.getRandom().nextDouble() - 0.5D) * player.getBbWidth(),
                    player.getY() + player.getRandom().nextDouble() * player.getBbHeight(),
                    player.getZ() + (player.getRandom().nextDouble() - 0.5D) * player.getBbWidth(),
                    1,
                    (player.getRandom().nextDouble() - 0.5D) * 0.1D,
                    player.getRandom().nextDouble() * 0.08D,
                    (player.getRandom().nextDouble() - 0.5D) * 0.1D,
                    0.02D);
        }
    }

    /**
     * Applies Blake's short dash impulse after a shadow is spawned.
     */
    private void applyBlakeBurst(Player player) {
        Vec3 motion = player.getDeltaMovement();
        if (motion.lengthSqr() > 0.01D && this.blakeAirTime < 8) {
            // Grounded movement gets the strong backward/side burst; airborne use is intentionally weaker.
            Vec3 burst = motion.normalize().scale(player.onGround() ? 3.5D : 1.0D);
            player.setDeltaMovement(burst.x, burst.y / 4.0D, burst.z);
        } else {
            Vec3 look = player.getLookAngle().scale(2.0D);
            player.setDeltaMovement(player.onGround() ? look.scale(-1.0D) : look);
        }
        player.fallDistance = 0.0F;
        player.hasImpulse = true;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 12, 0, true, false));
    }

    /**
     * Spawns Blake's normal, fire, or ice clone based on offhand dust.
     *
     * <p>Linked file: {@code BlakeSummonEntity.java} implements the clone's monster attraction
     * and fire/ice death effects.</p>
     */
    private void spawnBlakeShadow(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        EntityType<BlakeSummonEntity> type = RWBYMEntityTypes.BLAKE.get();
        ItemStack offhand = player.getOffhandItem();
        String offhandPath = itemPath(offhand);
        if ("firedust".equals(offhandPath)) {
            type = RWBYMEntityTypes.BLAKE_FIRE.get();
            consumeOffhandDust(player, offhand);
        } else if ("icedust".equals(offhandPath)) {
            type = RWBYMEntityTypes.BLAKE_ICE.get();
            consumeOffhandDust(player, offhand);
        }
        BlakeSummonEntity shadow = type.create(serverLevel);
        if (shadow != null) {
            float yaw = type == RWBYMEntityTypes.BLAKE_FIRE.get() ? player.getYRot() : 0.0F;
            float pitch = type == RWBYMEntityTypes.BLAKE_FIRE.get() ? player.getXRot() : 0.0F;
            shadow.moveTo(player.getX(), player.getY(), player.getZ(), yaw, pitch);
            shadow.setOwner(player);
            serverLevel.addFreshEntity(shadow);
        }
    }

    /**
     * Consumes the dust item used to specialize Blake's clone.
     */
    private void consumeOffhandDust(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /**
     * Creates Weiss's level 1-3 winter summons.
     */
    private void spawnWinterSummon(ServerLevel level, Player player,
                                   RegistryObject<EntityType<WinterSummonEntity>> type) {
        WinterSummonEntity summon = type.get().create(level);
        if (summon != null) {
            summon.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            level.addFreshEntity(summon);
        }
    }

    /**
     * Creates Weiss's level 4 Armorgeist summon.
     */
    private void spawnWinterArmorgeist(ServerLevel level, Player player) {
        WinterArmorgeistEntity summon = RWBYMEntityTypes.WINTER_ARMORGEIST.get().create(level);
        if (summon != null) {
            summon.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            level.addFreshEntity(summon);
        }
    }

    /**
     * Moves Aura from Jaune to a linked player and removes dead/full targets.
     */
    private boolean transferJauneAura(Player player, UUID targetUuid) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        Entity entity = serverLevel.getEntity(targetUuid);
        if (!(entity instanceof Player other) || !other.isAlive()) {
            return true;
        }
        boolean[] remove = {false};
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(selfAura ->
                other.getCapability(RWBYMCapabilities.AURA).ifPresent(otherAura -> {
                    // Jaune only spends Aura while the target can actually receive it.
                    if (otherAura.getAmount() < otherAura.getMaxAura() && useAura(player, JAUNE_TRANSFER)) {
                        otherAura.addAmount(JAUNE_TRANSFER);
                        if (this.level > 1) {
                            other.heal(0.01F);
                        }
                    } else {
                        remove[0] = true;
                    }
                }));
        return remove[0];
    }

    /**
     * Decrements timed active windows and clears them once expired.
     */
    private void tickTimedActive() {
        if (this.level <= 0) {
            this.active = false;
            return;
        }
        if (this.timer > 0) {
            this.timer--;
        } else {
            this.active = false;
        }
    }

    /**
     * Ticks the generic cooldown slot when it is not owned by Weiss's summon table.
     */
    private void tickSharedCooldown() {
        if (this.cooldown > 0 && !"weiss".equals(this.name)) {
            this.cooldown--;
        }
    }

    /**
     * Consumes Aura with creative-mode bypass and original recharge delay behavior.
     */
    private boolean useAura(Player player, float amount) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return player.getCapability(RWBYMCapabilities.AURA).map(aura -> consumeAura(aura, amount)).orElse(false);
    }

    /**
     * Applies the Aura cost and recharge delay once a Semblance successfully drains Aura.
     */
    private boolean consumeAura(IAura aura, float amount) {
        boolean success = aura.useAura(amount, false) == 0.0F;
        if (success) {
            aura.delayRecharge(AURA_RECHARGE_DELAY);
        }
        return success;
    }

    /**
     * Reads Aura percentage with health fallback for safety if the capability is missing.
     */
    private float auraPercentage(Player player) {
        return player.getCapability(RWBYMCapabilities.AURA)
                .map(IAura::getPercentage)
                .orElseGet(() -> Math.min(player.getHealth() / player.getMaxHealth(), 1.0F));
    }

    /**
     * Reads raw Aura amount for legacy checks that used absolute values instead of percentage.
     */
    private float currentAura(Player player) {
        return player.getCapability(RWBYMCapabilities.AURA).map(IAura::getAmount).orElse(player.getHealth());
    }

    /**
     * Resolves item registry paths for old id-based dust checks.
     */
    private String itemPath(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.getPath();
    }

    /**
     * Re-applies Semblance-specific Dust offhand attribute overrides from the original RWBYAmmoItem.
     *
     * <p>Linked file: {@code RWBYMAmmoItem.java} supplies the base offhand Dust modifiers. Hazel and
     * Nora alter those values per-player, so the 1.20.1 port layers transient player modifiers here.</p>
     */
    private void tickDustSemblanceModifiers(Player player) {
        clearDustSemblanceModifiers(player);
        String element = dustElement(player.getOffhandItem());
        if (element.isEmpty()) {
            return;
        }
        if ("hazel".equals(this.name)) {
            applyHazelDustModifiers(player, element);
        } else if ("nora".equals(this.name)) {
            applyNoraDustModifiers(player, element);
        }
    }

    private void applyHazelDustModifiers(Player player, String element) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original Hazel removes Dust penalties and strengthens the matching beneficial stat.
        switch (element) {
            case "water" -> addDustModifier(player, Attributes.ATTACK_DAMAGE, DUST_ATTACK_MODIFIER,
                    0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            case "impure", "gravity" -> addDustModifier(player, Attributes.MAX_HEALTH, DUST_HEALTH_MODIFIER,
                    0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            case "wind" -> {
                addDustModifier(player, Attributes.MOVEMENT_SPEED, DUST_SPEED_MODIFIER,
                        0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addDustModifier(player, Attributes.MAX_HEALTH, DUST_HEALTH_MODIFIER,
                        0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case "fire" -> {
                addDustModifier(player, Attributes.ATTACK_DAMAGE, DUST_ATTACK_MODIFIER,
                        0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addDustModifier(player, Attributes.MAX_HEALTH, DUST_HEALTH_MODIFIER,
                        0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case "lightning" -> {
                addDustModifier(player, Attributes.MOVEMENT_SPEED, DUST_SPEED_MODIFIER,
                        0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addDustModifier(player, Attributes.MAX_HEALTH, DUST_HEALTH_MODIFIER,
                        0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addDustModifier(player, Attributes.ATTACK_SPEED, DUST_ATTACK_SPEED_MODIFIER,
                        0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case "ice" -> {
                addDustModifier(player, Attributes.MOVEMENT_SPEED, DUST_SPEED_MODIFIER,
                        0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addDustModifier(player, Attributes.MAX_HEALTH, DUST_HEALTH_MODIFIER,
                        0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case "hardlight" -> {
                addDustModifier(player, Attributes.MOVEMENT_SPEED, DUST_SPEED_MODIFIER,
                        0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addDustModifier(player, Attributes.ARMOR, DUST_ARMOR_MODIFIER,
                        0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addDustModifier(player, Attributes.ATTACK_DAMAGE, DUST_ATTACK_MODIFIER,
                        0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            default -> {
            }
        }
    }

    private void applyNoraDustModifiers(Player player, String element) {
        if ("ice".equals(element)) {
            return;
        }
        double healthCompensation = switch (element) {
            case "lightning" -> 0.15D;
            case "impure", "wind", "fire", "gravity" -> 0.25D;
            default -> 0.0D;
        };
        if (healthCompensation > 0.0D) {
            // Original Nora halves non-ice negative health penalties from held Dust.
            addDustModifier(player, Attributes.MAX_HEALTH, DUST_HEALTH_MODIFIER,
                    healthCompensation, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    }

    private void clearDustSemblanceModifiers(Player player) {
        removeDustModifier(player, Attributes.MOVEMENT_SPEED, DUST_SPEED_MODIFIER);
        removeDustModifier(player, Attributes.ARMOR, DUST_ARMOR_MODIFIER);
        removeDustModifier(player, Attributes.MAX_HEALTH, DUST_HEALTH_MODIFIER);
        removeDustModifier(player, Attributes.ATTACK_DAMAGE, DUST_ATTACK_MODIFIER);
        removeDustModifier(player, Attributes.ATTACK_SPEED, DUST_ATTACK_SPEED_MODIFIER);
    }

    private void addDustModifier(Player player, Attribute attribute, UUID uuid, double amount,
            AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && amount != 0.0D) {
            instance.removeModifier(uuid);
            instance.addTransientModifier(new AttributeModifier(uuid, "RWBYM Semblance Dust adjustment",
                    amount, operation));
        }
    }

    private void removeDustModifier(Player player, Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(uuid);
        }
    }

    private String dustElement(ItemStack stack) {
        String path = itemPath(stack);
        if (path.contains("hardlight")) {
            return "hardlight";
        }
        if (path.contains("water")) {
            return "water";
        }
        if (path.equals("dust") || path.equals("dustcrystal") || path.equals("dustrock")) {
            return "impure";
        }
        if (path.contains("wind")) {
            return "wind";
        }
        if (path.contains("fire")) {
            return "fire";
        }
        if (path.contains("gravity") || path.contains("grav")) {
            return "gravity";
        }
        if (path.contains("light") || path.contains("electric") || path.contains("flare")) {
            return "lightning";
        }
        if (path.contains("ice")) {
            return "ice";
        }
        return "";
    }

    /**
     * Maintains Ragora's summon lifecycle, Aura upkeep, and cooldown.
     *
     * <p>Linked file: {@code RagoraEntity.java} implements follow, melee/ranged attack,
     * owner validation, and projectile behavior.</p>
     */
    private void tickRagora(Player player) {
        RagoraEntity ragora = resolveRagoraEntity(player);
        if (ragora != null && !ragora.isAlive()) {
            clearRagoraReference();
            ragora = null;
        }

        if (!this.active) {
            stopRagora(player, false);
            tickRagoraCooldown();
            return;
        }

        // Ragora can only begin summoning while grounded and off cooldown, matching the old setup ritual.
        if (ragora == null && this.ragoraSummonTime <= 0 && this.ragoraCooldown == 0 && player.onGround()) {
            if (useAura(player, RAGORA_INITIAL_AURA_COST)) {
                beginRagoraSummon(player);
            } else {
                this.active = false;
            }
            ragora = resolveRagoraEntity(player);
        }

        if (this.active && ragora != null) {
            // Higher levels reduce upkeep with the original sqrt(level) scaling.
            float upkeep = (float) (RAGORA_UPKEEP_AURA_COST / Math.sqrt(Math.max(1, this.level)));
            if (!useAura(player, upkeep)) {
                stopRagora(player, true);
            }
        }

        if (this.ragoraSummonTime > 0) {
            // Original key handling blocks player movement during Ragora's summon ritual.
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
            player.hasImpulse = true;
            tickRagoraSummon(player, ragora);
        } else if (ragora == null) {
            this.active = false;
        }

        tickRagoraCooldown();
    }

    /**
     * Advances Ragora's pre-spawn ritual particles and inserts the entity halfway through.
     */
    private void tickRagoraSummon(Player player, @Nullable RagoraEntity ragora) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        spawnRagoraSummonParticles(serverLevel, player);
        // The entity becomes real midway through the ritual so the visual build-up is preserved.
        if (this.ragoraSummonTime == 50 && ragora != null && serverLevel.getEntity(ragora.getUUID()) == null) {
            serverLevel.addFreshEntity(ragora);
        }
        if (this.ragoraSummonTime % (player.getRandom().nextInt(5) + 5) == 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (this.ragoraSummonTime > 50 && player.getLastHurtByMob() != null && player.hurtTime > 0) {
            stopRagora(player, true);
            return;
        }

        this.ragoraSummonTime--;
    }

    /**
     * Creates Ragora and stores both a direct reference and UUID for later resolution.
     */
    private void beginRagoraSummon(Player player) {
        RagoraEntity ragora = RWBYMEntityTypes.RAGORA.get().create(player.level());
        if (ragora == null) {
            this.active = false;
            return;
        }
        ragora.bindOwner(player);
        this.ragoraEntity = ragora;
        this.ragoraEntityUuid = ragora.getUUID();
        this.ragoraSummonTime = RAGORA_SUMMON_TIME;
        this.ragoraCooldown = RAGORA_COOLDOWN;
    }

    /**
     * Resolves the Ragora entity across ticking, chunk reloads, and capability clone copies.
     */
    @Nullable
    private RagoraEntity resolveRagoraEntity(Player player) {
        if (this.ragoraEntity != null) {
            return this.ragoraEntity;
        }
        if (this.ragoraEntityUuid == null) {
            if (this.active && this.ragoraSummonTime > 0) {
                beginRagoraSummon(player);
            }
            return this.ragoraEntity;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.ragoraEntityUuid);
            if (entity instanceof RagoraEntity ragora) {
                this.ragoraEntity = ragora;
                return ragora;
            }
        }
        return null;
    }

    /**
     * Removes Ragora and optionally deactivates the player's Semblance state.
     */
    private void stopRagora(Player player, boolean deactivate) {
        RagoraEntity ragora = resolveRagoraEntity(player);
        if (ragora != null && !ragora.isRemoved()) {
            ragora.discard();
        }
        clearRagoraReference();
        if (deactivate) {
            this.active = false;
        }
    }

    /**
     * Clears cached Ragora state without touching generic Semblance fields.
     */
    private void clearRagoraReference() {
        this.ragoraEntity = null;
        this.ragoraEntityUuid = null;
        this.ragoraSummonTime = 0;
    }

    /**
     * Sends server particles for Ragora's summoning cloud.
     */
    private void spawnRagoraSummonParticles(ServerLevel level, Player player) {
        // Progress grows from 0 to 1 so particle density ramps up during the ritual.
        double progress = 1.0D - this.ragoraSummonTime / (double) RAGORA_SUMMON_TIME;
        int count = Math.max(1, (int) Math.round(progress * RAGORA_SUMMON_PARTICLES / 8.0D));
        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                player.getX(),
                player.getY() + player.getBbHeight() + player.getBbWidth(),
                player.getZ(),
                count,
                player.getBbWidth() * 0.5D,
                player.getBbWidth() * 0.5D,
                player.getBbWidth() * 0.5D,
                0.02D);
    }

    /**
     * Counts down Ragora's resummon lockout.
     */
    private void tickRagoraCooldown() {
        if (this.ragoraCooldown > 0) {
            this.ragoraCooldown--;
        }
    }

    /**
     * Detects stale Ragora state after changing to another Semblance.
     */
    private boolean hasRagoraState() {
        return this.ragoraEntity != null || this.ragoraEntityUuid != null || this.ragoraSummonTime > 0;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Changes the active Semblance id and resets state that should not cross character abilities.
     */
    @Override
    public void setName(String name) {
        this.name = name == null || name.isBlank() ? "blank" : name.toLowerCase(Locale.ROOT);
        this.selectedLevel = selectedLevelDefault(this.name, this.level);
        this.active = false;
        this.timer = 0;
        this.cooldown = 0;
        this.blakeShadows = Math.min(this.blakeShadows, this.level);
        this.jauneTargets.clear();
    }

    @Override
    public boolean isActive() {
        return switch (this.name) {
            case "jaune" -> !this.jauneTargets.isEmpty();
            case "blake" -> this.blakeActiveTicks > 0;
            default -> this.active;
        };
    }

    /**
     * Allows commands and coins to force active state while clearing linked transient lists on disable.
     */
    @Override
    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.timer = 0;
            this.blakeActiveTicks = 0;
            this.jauneTargets.clear();
        }
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    /**
     * Clamps level to each original Semblance's max rank.
     */
    @Override
    public void setLevel(int level) {
        this.level = Math.max(0, Math.min(maxLevel(this.name), level));
        if (this.selectedLevel == -1) {
            return;
        }
        if (this.level <= 0) {
            this.selectedLevel = 0;
        } else if (this.selectedLevel <= 0 || this.selectedLevel > this.level) {
            this.selectedLevel = this.level;
        }
        this.blakeShadows = Math.min(this.blakeShadows, this.level);
    }

    @Override
    public int getSelectedLevel() {
        return this.selectedLevel;
    }

    /**
     * Updates the original selected-level value while respecting non-cycling Semblances.
     */
    @Override
    public void setSelectedLevel(int level) {
        if ("jaune".equals(this.name)) {
            this.selectedLevel = -1;
            return;
        }
        if (level <= this.level) {
            this.selectedLevel = Math.max(0, level);
        }
    }

    /**
     * Reports whether the active Semblance should restrict player movement.
     */
    @Override
    public boolean isMovementBlocked() {
        return ("jaune".equals(this.name) && !this.jauneTargets.isEmpty())
                || ("ragora".equals(this.name) && this.ragoraSummonTime > 0);
    }

    /**
     * Copies persisted Semblance state during player clone events.
     *
     * <p>Linked file: {@code RWBYMCapabilityEvents.java} calls this from
     * {@code PlayerEvent.Clone} after reviving old capabilities.</p>
     */
    @Override
    public void copyFrom(ISemblance other) {
        setName(other.getName());
        setLevel(other.getLevel());
        setSelectedLevel(other.getSelectedLevel());
        setActive(other.isActive());
        if (other instanceof Semblance semblance) {
            this.timer = semblance.timer;
            this.cooldown = semblance.cooldown;
            this.blakeShadows = semblance.blakeShadows;
            this.blakeShadowCooldown = semblance.blakeShadowCooldown;
            this.blakeShadowCooldownTime = semblance.blakeShadowCooldownTime;
            this.blakeActiveTicks = semblance.blakeActiveTicks;
            this.blakeAirTime = semblance.blakeAirTime;
            this.jauneTargets.clear();
            this.jauneTargets.addAll(semblance.jauneTargets);
            this.ragoraCooldown = semblance.ragoraCooldown;
            this.ragoraSummonTime = semblance.ragoraSummonTime;
            this.ragoraEntityUuid = semblance.ragoraEntityUuid;
            this.ragoraEntity = null;
        } else {
            clearTransientState();
        }
    }

    /**
     * Writes all Semblance state to capability NBT and network sync packets.
     */
    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", this.name);
        tag.putBoolean("active", this.active);
        tag.putInt("level", this.level);
        tag.putInt("selectedLevel", this.selectedLevel);
        tag.putInt("timer", this.timer);
        tag.putInt("cooldown", this.cooldown);
        tag.putInt("blakeShadows", this.blakeShadows);
        tag.putInt("blakeShadowCooldown", this.blakeShadowCooldown);
        tag.putInt("blakeShadowCooldownTime", this.blakeShadowCooldownTime);
        tag.putInt("blakeActiveTicks", this.blakeActiveTicks);
        tag.putInt("blakeAirTime", this.blakeAirTime);
        // Store Jaune links as UUIDs so they survive packet sync and player clone boundaries.
        for (int i = 0; i < this.jauneTargets.size(); i++) {
            tag.putUUID("jauneTarget" + i, this.jauneTargets.get(i));
        }
        tag.putInt("jauneTargetCount", this.jauneTargets.size());
        tag.putInt("ragoraCooldown", this.ragoraCooldown);
        tag.putInt("ragoraSummonTime", this.ragoraSummonTime);
        if (this.ragoraEntityUuid != null) {
            tag.putUUID("ragoraEntity", this.ragoraEntityUuid);
        }
        return tag;
    }

    /**
     * Restores Semblance state from player capability storage or client sync.
     */
    @Override
    public void deserialize(CompoundTag tag) {
        this.name = tag.contains("name") ? tag.getString("name").toLowerCase(Locale.ROOT) : "blank";
        this.active = tag.getBoolean("active");
        this.level = Math.max(0, Math.min(maxLevel(this.name), tag.contains("level") ? tag.getInt("level") : 1));
        this.selectedLevel = tag.contains("selectedLevel")
                ? tag.getInt("selectedLevel")
                : selectedLevelDefault(this.name, this.level);
        setSelectedLevel(this.selectedLevel);
        this.timer = Math.max(0, tag.getInt("timer"));
        this.cooldown = Math.max(0, tag.getInt("cooldown"));
        this.blakeShadows = Math.max(0, tag.getInt("blakeShadows"));
        this.blakeShadowCooldown = Math.max(0, tag.getInt("blakeShadowCooldown"));
        this.blakeShadowCooldownTime = tag.contains("blakeShadowCooldownTime")
                ? Math.max(1, tag.getInt("blakeShadowCooldownTime"))
                : 200;
        this.blakeActiveTicks = Math.max(0, tag.getInt("blakeActiveTicks"));
        this.blakeAirTime = Math.max(0, tag.getInt("blakeAirTime"));
        this.jauneTargets.clear();
        // Clamp old or malformed data to the original five-target Jaune limit.
        int targetCount = Math.min(5, Math.max(0, tag.getInt("jauneTargetCount")));
        for (int i = 0; i < targetCount; i++) {
            String key = "jauneTarget" + i;
            if (tag.hasUUID(key)) {
                this.jauneTargets.add(tag.getUUID(key));
            }
        }
        this.ragoraCooldown = Math.max(0, tag.getInt("ragoraCooldown"));
        this.ragoraSummonTime = Math.max(0, tag.getInt("ragoraSummonTime"));
        this.ragoraEntityUuid = tag.hasUUID("ragoraEntity") ? tag.getUUID("ragoraEntity") : null;
        this.ragoraEntity = null;
    }

    /**
     * Clears per-ability runtime fields when copying from an unknown implementation.
     */
    private void clearTransientState() {
        this.timer = 0;
        this.cooldown = 0;
        this.blakeShadows = 0;
        this.blakeShadowCooldown = 0;
        this.blakeShadowCooldownTime = 200;
        this.blakeActiveTicks = 0;
        this.blakeAirTime = 0;
        this.jauneTargets.clear();
        this.ragoraCooldown = 0;
        this.ragoraSummonTime = 0;
        this.ragoraEntityUuid = null;
        this.ragoraEntity = null;
    }

    /**
     * Computes the selected-level default used when a Semblance is first assigned.
     */
    private int selectedLevelDefault(String semblanceName, int semblanceLevel) {
        return "jaune".equals(semblanceName) ? -1 : Math.max(0, semblanceLevel);
    }

    /**
     * Returns the original maximum rank for each implemented Semblance.
     */
    private int maxLevel(String semblanceName) {
        return switch (semblanceName) {
            case "ruby", "blake", "yang", "nora", "ren", "jaune", "lysette", "qrow", "clover", "valour", "fall", "hazel", "pyrrha" -> 3;
            case "weiss", "ragora" -> RAGORA_MAX_LEVEL;
            case "harriet" -> 5;
            default -> 10;
        };
    }

    /**
     * Positive/negative random effect pools used by Clover and Qrow.
     */
    private enum MobEffectsHolder {
        POSITIVE(new MobEffect[]{
                MobEffects.NIGHT_VISION, MobEffects.DIG_SPEED, MobEffects.FIRE_RESISTANCE,
                MobEffects.JUMP, MobEffects.WATER_BREATHING, MobEffects.ABSORPTION,
                MobEffects.HEALTH_BOOST, MobEffects.DAMAGE_BOOST, MobEffects.REGENERATION,
                MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_RESISTANCE
        }),
        NEGATIVE(new MobEffect[]{
                MobEffects.BLINDNESS, MobEffects.CONFUSION, MobEffects.WEAKNESS,
                MobEffects.DIG_SLOWDOWN, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.HUNGER,
                MobEffects.GLOWING
        });

        private final MobEffect[] effects;

        MobEffectsHolder(MobEffect[] effects) {
            this.effects = effects;
        }

        private MobEffect first(Player player) {
            return this.effects[player.getRandom().nextInt(this.effects.length)];
        }

        private MobEffect second(Player player) {
            return this.effects[player.getRandom().nextInt(this.effects.length)];
        }
    }
}
