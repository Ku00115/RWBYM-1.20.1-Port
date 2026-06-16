package io.github.blaezdev.rwbym.entity;

import io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RWBYMProjectileEntity extends ThrowableItemProjectile {
    private float damage = 6.0F;
    private String element = "";
    private boolean returning;
    private int life;

    public RWBYMProjectileEntity(EntityType<? extends RWBYMProjectileEntity> type, Level level) {
        super(type, level);
    }

    public RWBYMProjectileEntity(Level level, LivingEntity owner, ItemStack display, float damage,
            String element, boolean returning) {
        super(RWBYMEntityTypes.WEAPON_PROJECTILE.get(), owner, level);
        this.setItem(display.copyWithCount(1));
        this.damage = damage;
        this.element = element == null ? "" : element;
        this.returning = returning;
    }

    @Override
    protected Item getDefaultItem() {
        return RWBYMItems.ICON.get();
    }

    @Override
    public void tick() {
        super.tick();
        this.life++;
        if (this.returning && this.life > 12 && this.getOwner() instanceof LivingEntity owner) {
            Vec3 toOwner = owner.getEyePosition().subtract(this.position());
            if (toOwner.lengthSqr() < 2.25D) {
                this.discard();
                return;
            }
            this.setDeltaMovement(this.getDeltaMovement().scale(0.65D).add(toOwner.normalize().scale(0.35D)));
        }
        if (this.life > 120) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity owner = this.getOwner();
        Entity target = result.getEntity();
        if (!this.level().isClientSide() && target instanceof LivingEntity living) {
            living.hurt(owner instanceof Player player
                    ? this.level().damageSources().playerAttack(player)
                    : this.level().damageSources().thrown(this, owner), this.damage);
            applyElement(living);
            if (!this.returning) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide() && !this.returning) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", this.damage);
        tag.putString("element", this.element);
        tag.putBoolean("returning", this.returning);
        tag.putInt("life", this.life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("damage");
        this.element = tag.getString("element");
        this.returning = tag.getBoolean("returning");
        this.life = tag.getInt("life");
    }

    private void applyElement(LivingEntity target) {
        if (this.element.contains("fire")) {
            target.setSecondsOnFire(5);
        } else if (this.element.contains("ice")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        } else if (this.element.contains("grav")) {
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 45, 0));
        } else if (this.element.contains("wind")) {
            target.knockback(1.0D, target.getRandom().nextDouble() - 0.5D, target.getRandom().nextDouble() - 0.5D);
        } else if (this.element.contains("light") || this.element.contains("electric")) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        } else if (this.element.contains("water")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        } else if (this.element.contains("rocket") || this.element.contains("grenade")) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.5F, Level.ExplosionInteraction.NONE);
        }
    }

    public static boolean shouldUseProjectile(RWBYMWeaponProfiles.WeaponProfile profile) {
        return profile.hasType(RWBYMWeaponProfiles.THROWN)
                || profile.hasType(RWBYMWeaponProfiles.BOOMERANG)
                || profile.hasType(RWBYMWeaponProfiles.ROCKET)
                || profile.hasType(RWBYMWeaponProfiles.BOW)
                || profile.name().contains("boomerang")
                || profile.name().contains("rocket");
    }
}
