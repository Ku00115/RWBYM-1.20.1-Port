package io.github.blaezdev.rwbym.entity;

import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class ZweiEntity extends Wolf {
    public ZweiEntity(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            this.heal(0.08F);
        }
    }

    @Override
    public void setTame(boolean tamed) {
        super.setTame(tamed);
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(tamed ? 100.0D : 10.0D);
        }
        AttributeInstance attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(tamed ? 5.0D : 2.0D);
        }
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Original Zwei rolled 4-6 damage per hit instead of using Wolf's fixed attack attribute.
        boolean hurt = target.hurt(this.damageSources().mobAttack(this), 4.0F + this.random.nextInt(3));
        if (hurt) {
            this.doEnchantDamageEffects(this, target);
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        this.setOrderedToSit(false);
        Entity attacker = source.getEntity();
        if (attacker != null && !(attacker instanceof Player) && !(attacker instanceof AbstractArrow)) {
            // Original Zwei softened non-player, non-arrow damage before passing it to EntityTameable.
            amount = (amount + 1.0F) / 2.0F;
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public Wolf getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        ZweiEntity child = RWBYMEntityTypes.ZWEI.get().create(level);
        if (child != null && this.isTame() && this.getOwnerUUID() != null) {
            child.setOwnerUUID(this.getOwnerUUID());
            child.setTame(true);
        }
        return child;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return !this.isAngry() && super.canBeLeashed(player);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper || target instanceof Ghast) {
            return false;
        }
        if (target instanceof ZweiEntity zwei && zwei.isTame() && zwei.getOwner() == owner) {
            return false;
        }
        if (target instanceof Player player && owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(player)) {
            return false;
        }
        return !(target instanceof AbstractHorse horse) || !horse.isTamed();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }
}
