package io.github.blaezdev.rwbym.capability.semblance;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Semblance implements ISemblance {
    private String name = "blank";
    private boolean active;
    private int level = 1;

    @Override
    public void tick(Player player) {
        if (!this.active || "blank".equals(this.name)) {
            return;
        }
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
            float cost = Math.max(0.1F, this.level * 0.12F);
            if (aura.getAmount() < cost) {
                this.active = false;
                return;
            }
            aura.useAura(cost, false);
            aura.delayRecharge(20);
            apply(player);
        });
    }

    private void apply(Player player) {
        switch (this.name) {
            case "ruby" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, Math.min(3, this.level), true, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));
            }
            case "blake" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, true, false));
            }
            case "yang" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, Math.min(3, this.level), true, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, true, false));
            }
            case "weiss" -> {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 30, Math.min(2, this.level), true, false));
                if (player.tickCount % 40 == 0) {
                    slowNearbyGrimm(player);
                }
            }
            case "nora" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 1, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30, 1, true, false));
            }
            case "harriet" -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30,
                    Math.min(5, this.level + 2), true, false));
            case "ren" -> player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false));
            case "jaune" -> player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(0.35F * this.level));
            default -> player.addEffect(new MobEffectInstance(MobEffects.LUCK, 30, 0, true, false));
        }
    }

    private void slowNearbyGrimm(Player player) {
        AABB area = player.getBoundingBox().inflate(5.0D + this.level);
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, area,
                target -> target instanceof BasicGrimmEntity)) {
            Vec3 away = entity.position().subtract(player.position()).normalize();
            entity.setDeltaMovement(entity.getDeltaMovement().add(away.x * 0.2D, 0.08D, away.z * 0.2D));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            entity.hurt(player.damageSources().magic(), 1.5F * this.level);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name == null || name.isBlank() ? "blank" : name.toLowerCase();
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(10, level));
    }

    @Override
    public void copyFrom(ISemblance other) {
        setName(other.getName());
        setActive(other.isActive());
        setLevel(other.getLevel());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", this.name);
        tag.putBoolean("active", this.active);
        tag.putInt("level", this.level);
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        setName(tag.getString("name"));
        setActive(tag.getBoolean("active"));
        setLevel(tag.contains("level") ? tag.getInt("level") : 1);
    }
}
