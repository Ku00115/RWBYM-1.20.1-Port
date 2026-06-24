package io.github.blaezdev.rwbym.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class BlakeSummonEntity extends AbstractGolem {
    @Nullable
    private LivingEntity owner;
    private String cachedKind;

    public BlakeSummonEntity(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        attractNearbyMonsters();
        if (this.tickCount >= 1200) {
            this.discard();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.level().broadcastEntityEvent(this, (byte) 4);
        boolean hurt = target.hurt(this.damageSources().mobAttack(this), 7.0F + this.random.nextInt(15));
        if (hurt) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
            this.doEnchantDamageEffects(this, target);
        }
        this.playSound(SoundEvents.PLAYER_ATTACK_WEAK, 1.0F, 1.0F);
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        boolean serverSide = !this.level().isClientSide();
        if (serverSide) {
            clearMonsterTargets();
        }
        super.die(source);
        if (!serverSide) {
            return;
        }
        switch (summonKind()) {
            case "blakefire" -> explodeOnDeath();
            case "blakeice" -> spawnSlowCloud();
            default -> {
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed(net.minecraft.world.entity.player.Player player) {
        return false;
    }

    @Override
    public MobType getMobType() {
        // Original Blake shadows used the Illager creature attribute for combat classification.
        return MobType.ILLAGER;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
    }

    private void attractNearbyMonsters() {
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(20.0D))) {
            monster.setTarget(this);
        }
    }

    private void clearMonsterTargets() {
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(20.0D))) {
            if (monster.getTarget() == this) {
                monster.setTarget(null);
            }
        }
    }

    private void explodeOnDeath() {
        double radius = 3.0D;
        double effectiveRadius = radius * 2.0D;
        Vec3 origin = this.position();
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(effectiveRadius))) {
            if (entity == this || entity == this.owner || entity.isInvulnerable()) {
                continue;
            }
            double dx = entity.getX() - this.getX();
            double dy = entity.getEyeY() - this.getY();
            double dz = entity.getZ() - this.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance <= 0.0D || distance > effectiveRadius) {
                continue;
            }
            // Original BlakeFire explosion was non-destructive but still used vanilla-style exposure falloff.
            double strength = (1.0D - distance / effectiveRadius) * Explosion.getSeenPercent(origin, entity);
            float damage = (float) ((int) (((strength * strength + strength) / 2.0D) * 7.0D * 20.0D + 1.0D));
            entity.hurt(this.damageSources().explosion(this, null), damage);
            double knockback = ProtectionEnchantment.getExplosionKnockbackAfterDampener(entity, strength);
            entity.push(dx / distance * knockback, dy / distance * knockback, dz / distance * knockback);
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY(), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 4.0F,
                (1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F) * 0.7F);
    }

    private void spawnSlowCloud() {
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 55));
        // Keep the original pale-cyan Blake Ice cloud color instead of the potion-derived default.
        cloud.setFixedColor(0xccffff);
        cloud.setDuration(60);
        cloud.setRadius(3.0F);
        cloud.setWaitTime(10);
        this.level().addFreshEntity(cloud);
    }

    private String summonKind() {
        if (this.cachedKind == null) {
            this.cachedKind = EntityType.getKey(this.getType()).getPath();
        }
        return this.cachedKind;
    }
}
