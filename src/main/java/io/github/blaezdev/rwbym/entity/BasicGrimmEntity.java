package io.github.blaezdev.rwbym.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class BasicGrimmEntity extends Zombie {
    public BasicGrimmEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean isSunSensitive() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        String kind = this.grimmKind();
        if ("apathy".equals(kind) && this.tickCount % 40 == 0) {
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(6.0D), target -> target != this && !target.isAlliedTo(this))) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));
            }
        } else if ("lancer".equals(kind) && this.tickCount % 20 == 0 && this.getTarget() != null) {
            this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false));
            if (this.getTarget().getY() > this.getY() + 1.0D) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.12D, 0.0D));
                this.hasImpulse = true;
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (!hurt || this.level().isClientSide() || !(target instanceof LivingEntity living)) {
            return hurt;
        }
        String kind = this.grimmKind();
        if ("deathstalker".equals(kind)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        } else if ("boarbatusk".equals(kind) || "sabyr".equals(kind)) {
            living.knockback(1.2D, this.getX() - living.getX(), this.getZ() - living.getZ());
        } else if ("beringle".equals(kind) || "ursa".equals(kind)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        }
        return true;
    }

    private String grimmKind() {
        return EntityType.getKey(this.getType()).getPath();
    }
}
