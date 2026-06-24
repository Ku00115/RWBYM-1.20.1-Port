package io.github.blaezdev.rwbym.entity;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class RenSummonEntity extends AbstractGolem {
    public RenSummonEntity(EntityType<? extends AbstractGolem> type, Level level) {
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
        if (!this.level().isClientSide()) {
            for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(60.0D))) {
                if (monster.getTarget() == this) {
                    monster.setTarget(null);
                }
            }
        }
        super.die(source);
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
        // Original Ren summon used the Illager creature attribute for combat classification.
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
}
