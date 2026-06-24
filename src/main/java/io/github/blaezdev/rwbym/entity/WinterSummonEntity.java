package io.github.blaezdev.rwbym.entity;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WinterSummonEntity extends AbstractGolem {
    private String cachedKind;

    public WinterSummonEntity(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new RangedMeleeAttackGoal(this, 1.0D, false, 0.5F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(20.0D))) {
            monster.setTarget(this);
        }
        if (this.tickCount >= 1200) {
            this.discard();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.level().broadcastEntityEvent(this, (byte) 4);
        boolean hurt = target.hurt(this.damageSources().mobAttack(this), 8.0F + this.random.nextInt(15));
        if (hurt) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
            this.doEnchantDamageEffects(this, target);
        }
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return hurt;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public MobType getMobType() {
        // Original Weiss summons used the Illager creature attribute to avoid vanilla golem behavior assumptions.
        return MobType.ILLAGER;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return switch (kind()) {
            case "winter_boarbatusk" -> SoundEvents.ZOMBIFIED_PIGLIN_HURT;
            case "winter_ursa" -> SoundEvents.POLAR_BEAR_HURT;
            default -> SoundEvents.SQUID_DEATH;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (kind()) {
            case "winter_boarbatusk" -> SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
            case "winter_ursa" -> SoundEvents.POLAR_BEAR_DEATH;
            default -> SoundEvents.WOLF_DEATH;
        };
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    private String kind() {
        if (this.cachedKind == null) {
            this.cachedKind = EntityType.getKey(this.getType()).getPath();
        }
        return this.cachedKind;
    }
}
