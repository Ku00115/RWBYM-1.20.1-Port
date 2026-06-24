package io.github.blaezdev.rwbym.entity;

import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;

public class BasicRWBYMNpcEntity extends Zombie {
    private String cachedKind;

    public BasicRWBYMNpcEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        String kind = npcKind();
        if (isTemporarySummon(kind)) {
            attractNearbyMonsters(kind);
            if (this.tickCount >= 1200) {
                this.discard();
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (!hurt || this.level().isClientSide() || !(target instanceof LivingEntity living)) {
            return hurt;
        }
        living.setDeltaMovement(living.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
        String kind = npcKind();
        if ("winter_beowolf".equals(kind) || "winter_ursa".equals(kind)
                || "winter_boarbatusk".equals(kind) || "winterarmorgeist".equals(kind)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
        }
        return true;
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        String kind = npcKind();
        if (!this.level().isClientSide()) {
            clearMonsterTargets(kind);
        }
        super.die(source);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
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

    private boolean isTemporarySummon(String kind) {
        return "winter_beowolf".equals(kind)
                || "winter_ursa".equals(kind)
                || "winter_boarbatusk".equals(kind)
                || "winterarmorgeist".equals(kind);
    }

    private void attractNearbyMonsters(String kind) {
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(20.0D))) {
            if (monster != this && monster.getTarget() == null) {
                monster.setTarget(this);
            }
        }
    }

    private void clearMonsterTargets(String kind) {
        if (!isTemporarySummon(kind)) {
            return;
        }
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(20.0D))) {
            if (monster.getTarget() == this) {
                monster.setTarget(null);
            }
        }
    }

    private void freezeNearby(double radius, int duration, int amplifier) {
        AABB area = this.getBoundingBox().inflate(radius);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && !this.isAlliedTo(entity))) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        }
    }

    private String npcKind() {
        if (this.cachedKind == null) {
            this.cachedKind = EntityType.getKey(this.getType()).getPath();
        }
        return this.cachedKind;
    }
}
