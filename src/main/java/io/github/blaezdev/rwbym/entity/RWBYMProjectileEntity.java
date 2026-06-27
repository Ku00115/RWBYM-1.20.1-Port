package io.github.blaezdev.rwbym.entity;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.item.RWBYMWeaponModifierHelper;
import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.registry.RWBYMMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class RWBYMProjectileEntity extends ThrowableItemProjectile {
    private float damage = 6.0F;
    private String element = "";
    private boolean returning;
    private boolean pierce;
    private boolean recoverable;
    private boolean fast;
    private boolean pyrrhaReturning;
    private ItemStack weaponStack = ItemStack.EMPTY;
    private int life;

    public RWBYMProjectileEntity(EntityType<? extends RWBYMProjectileEntity> type, Level level) {
        super(type, level);
    }

    public RWBYMProjectileEntity(Level level, LivingEntity owner, ItemStack display, float damage,
            String element, boolean returning) {
        this(level, owner, display, damage, element, returning, false);
    }

    public RWBYMProjectileEntity(Level level, LivingEntity owner, ItemStack display, float damage,
            String element, boolean returning, boolean pierce) {
        this(level, owner, display, damage, element, returning, pierce, false);
    }

    public RWBYMProjectileEntity(Level level, LivingEntity owner, ItemStack display, float damage,
            String element, boolean returning, boolean pierce, boolean recoverable) {
        this(level, owner, display, ItemStack.EMPTY, damage, element, returning, pierce, recoverable);
    }

    public RWBYMProjectileEntity(Level level, LivingEntity owner, ItemStack display, ItemStack weaponStack,
            float damage, String element, boolean returning, boolean pierce, boolean recoverable) {
        this(level, owner, display, weaponStack, damage, element, returning, pierce, recoverable, false);
    }

    public RWBYMProjectileEntity(Level level, LivingEntity owner, ItemStack display, ItemStack weaponStack,
            float damage, String element, boolean returning, boolean pierce, boolean recoverable, boolean fast) {
        super(RWBYMEntityTypes.WEAPON_PROJECTILE.get(), owner, level);
        this.setItem(display.copyWithCount(1));
        this.weaponStack = weaponStack.copyWithCount(1);
        this.damage = damage;
        this.element = element == null ? "" : element;
        this.returning = returning;
        this.pierce = pierce;
        this.recoverable = recoverable;
        this.fast = fast;
    }

    @Override
    protected Item getDefaultItem() {
        return RWBYMItems.ICON.get();
    }

    @Override
    public void tick() {
        super.tick();
        this.life++;
        if (this.level().isClientSide()) {
            spawnLegacyCritTrail();
        }
        if (this.returning && this.life > 12 && this.getOwner() instanceof LivingEntity owner) {
            Vec3 toOwner = owner.getEyePosition().subtract(this.position());
            if (toOwner.lengthSqr() < 2.25D) {
                if (this.pyrrhaReturning && owner instanceof Player player) {
                    giveRecoveredItem(player);
                }
                this.discard();
                return;
            }
            this.setDeltaMovement(this.getDeltaMovement().scale(0.65D).add(toOwner.normalize().scale(returnPullStrength())));
        }
        if (this.fast) {
            this.setDeltaMovement(this.getDeltaMovement().scale(1.02D));
        }
        if (this.life > (this.fast ? 35 : 120)) {
            dropRecoveredItem();
            this.discard();
        }
    }

    private void spawnLegacyCritTrail() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-7D) {
            return;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // EntityBullet always marked shots critical, so client-side CRIT particles provide the old bullet trail.
        for (int i = 0; i < 5; i++) {
            double progress = i / 32.0D;
            this.level().addParticle(ParticleTypes.CRIT,
                    this.getX() + motion.x * progress,
                    this.getY() + motion.y * progress,
                    this.getZ() + motion.z * progress,
                    this.random.nextGaussian() * 0.1D,
                    this.random.nextGaussian() * 0.1D,
                    this.random.nextGaussian() * 0.1D);
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
            spawnRagoraShadowBallIfNeeded(living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ());
            applyWeaponModifierEffects(living, owner);
            teleportOwnerIfNeeded();
            if (!this.returning && !this.pierce) {
                dropRecoveredItem();
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            applyBlockImpact(result);
            spawnRagoraShadowBallIfNeeded(result.getLocation().x, result.getLocation().y, result.getLocation().z);
            teleportOwnerIfNeeded();
            if (!this.returning && startPyrrhaReturnIfPossible()) {
                return;
            }
            if (!this.returning) {
                dropRecoveredItem();
                this.discard();
            }
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
        tag.putBoolean("pierce", this.pierce);
        tag.putBoolean("recoverable", this.recoverable);
        tag.putBoolean("fast", this.fast);
        tag.putBoolean("pyrrhaReturning", this.pyrrhaReturning);
        tag.putInt("life", this.life);
        if (!this.weaponStack.isEmpty()) {
            tag.put("weaponStack", this.weaponStack.save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("damage");
        this.element = tag.getString("element");
        this.returning = tag.getBoolean("returning");
        this.pierce = tag.getBoolean("pierce");
        this.recoverable = tag.getBoolean("recoverable");
        this.fast = tag.getBoolean("fast");
        this.pyrrhaReturning = tag.getBoolean("pyrrhaReturning");
        this.life = tag.getInt("life");
        this.weaponStack = tag.contains("weaponStack") ? ItemStack.of(tag.getCompound("weaponStack")) : ItemStack.EMPTY;
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
        }
        if (this.element.contains("absorption")) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 2));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 2));
            }
        }
        if (this.element.contains("poison")) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        }
        if (this.element.contains("flarefrost")) {
            target.setSecondsOnFire(4);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
        explodeIfNeeded(this.getX(), this.getY(), this.getZ(), Level.ExplosionInteraction.NONE);
        RWBYMWeaponItem.applyLegacyExplosionTargetEffects(target, this.element);
        spawnPotionCloudIfNeeded(target.getX(), target.getY(), target.getZ());
    }

    private void applyWeaponModifierEffects(LivingEntity target, Entity owner) {
        ItemStack stack = this.weaponStack.isEmpty() ? this.getItem() : this.weaponStack;
        int knockShot = enchantLevel(stack, "knock_shot");
        if (knockShot > 0 && owner != null) {
            target.knockback(knockShot * 0.8D, owner.getX() - target.getX(), owner.getZ() - target.getZ());
        }
        if (!(owner instanceof Player player)) {
            return;
        }
        if (target instanceof BasicGrimmEntity) {
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(3.0F));
        }
        RWBYMWeaponModifierHelper.applyKillModifierEffects(stack, player, target);
    }

    private static int enchantLevel(ItemStack stack, String idPart) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey());
            if (id != null && id.getPath().contains(idPart)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    private static boolean hasEnchant(ItemStack stack, String idPart) {
        return enchantLevel(stack, idPart) > 0;
    }

    private void applyBlockImpact(BlockHitResult result) {
        // Original ExplosionAmmoHit respected mobGriefing only for block impacts, not entity impacts.
        explodeIfNeeded(result.getLocation().x, result.getLocation().y, result.getLocation().z,
                Level.ExplosionInteraction.MOB);
        if (this.element.contains("fire")) {
            net.minecraft.core.BlockPos firePos = result.getBlockPos().relative(result.getDirection());
            BlockState fire = Blocks.FIRE.defaultBlockState();
            if (fire.canSurvive(this.level(), firePos)) {
                this.level().setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
            }
        }
        spawnPotionCloudIfNeeded(result.getLocation().x, result.getLocation().y, result.getLocation().z);
    }

    private void teleportOwnerIfNeeded() {
        if (!this.element.contains("flyingthundergod") || !(this.getOwner() instanceof Player player)) {
            return;
        }
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
            if (aura.getPercentage() > 0.06F) {
                aura.useAura(2.0F, false);
                player.teleportTo(this.getX(), this.getY(), this.getZ());
                player.fallDistance = 0.0F;
            }
        });
    }

    private void spawnPotionCloudIfNeeded(double x, double y, double z) {
        if (!this.element.contains("cloud")) {
            return;
        }
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), x, y, z);
        cloud.setRadius(2.0F);
        cloud.setDuration(100);
        cloud.setWaitTime(0);
        if (this.element.contains("fire")) {
            cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0));
        }
        if (this.element.contains("ice")) {
            cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
        }
        if (this.element.contains("poison")) {
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        }
        if (this.element.contains("light")) {
            cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
        }
        if (this.element.contains("grav")) {
            cloud.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
        }
        this.level().addFreshEntity(cloud);
    }

    private void spawnRagoraShadowBallIfNeeded(double x, double y, double z) {
        if (!isRagoraFireball()) {
            return;
        }
        float radius = 4.5F;
        float step = 1.0F;
        this.level().playSound(null, x, y, z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                this.getSoundSource(), 3.0F, 1.0F);
        for (float offset = Math.round(-radius / step) * step; offset <= Math.round(radius / step) * step; offset += step) {
            float layerRadiusSquared = radius * radius - offset * offset;
            if (layerRadiusSquared < 0.0F) {
                continue;
            }
            AreaEffectCloud cloud = new AreaEffectCloud(this.level(), x, y + offset, z);
            cloud.setRadius((float) Math.sqrt(layerRadiusSquared));
            cloud.setRadiusOnUse(0.0F);
            cloud.setWaitTime(0);
            cloud.setDuration(20);
            cloud.setRadiusPerTick(0.0F);
            cloud.setParticle(ParticleTypes.DRAGON_BREATH);
            cloud.addEffect(new MobEffectInstance(RWBYMMobEffects.INSTANT_DAMAGE.get(), 1, 30));
            if (this.getOwner() instanceof LivingEntity livingOwner) {
                cloud.setOwner(livingOwner);
            }
            this.level().addFreshEntity(cloud);
        }
    }

    private void dropRecoveredItem() {
        if (this.level().isClientSide() || !this.recoverable || this.returning || this.getItem().isEmpty()) {
            return;
        }
        this.spawnAtLocation(this.getItem().copyWithCount(1), 0.1F);
        this.recoverable = false;
    }

    private boolean startPyrrhaReturnIfPossible() {
        if (!this.recoverable || !(this.getOwner() instanceof Player player) || player.isSpectator()) {
            return false;
        }
        int level = pyrrhaLevel(player);
        if (level <= 0 || player.getCapability(RWBYMCapabilities.AURA)
                .map(aura -> aura.useAura(1.0F, false) > 0.0F)
                .orElse(true)) {
            return false;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original EntityBullet made Pyrrha-owned lodged projectiles no-clip back to the shooter.
        this.returning = true;
        this.pyrrhaReturning = true;
        this.life = Math.max(this.life, 13);
        this.setNoGravity(true);
        return true;
    }

    private int pyrrhaLevel(Player player) {
        return player.getCapability(RWBYMCapabilities.SEMBLANCE)
                .map(semblance -> "pyrrha".equals(semblance.getName()) ? Math.min(3, semblance.getLevel()) : 0)
                .orElse(0);
    }

    private double returnPullStrength() {
        if (!this.pyrrhaReturning || !(this.getOwner() instanceof Player player)) {
            return 0.35D;
        }
        return switch (pyrrhaLevel(player)) {
            case 1 -> 0.25D;
            case 2 -> 0.50D;
            default -> 1.0D;
        };
    }

    private void giveRecoveredItem(Player player) {
        if (!this.recoverable || this.getItem().isEmpty()) {
            return;
        }
        ItemStack recovered = this.getItem().copyWithCount(1);
        if (!player.getInventory().add(recovered)) {
            player.drop(recovered, false);
        }
        this.recoverable = false;
    }

    private void explodeIfNeeded(double x, double y, double z, Level.ExplosionInteraction interaction) {
        String explosionElement = this.element;
        if (this.element.contains("rocket") || this.element.contains("grenade")) {
            explosionElement += " explosion";
        }
        Float power = RWBYMWeaponItem.explosionPower(explosionElement);
        if (power == null) {
            return;
        }
        RWBYMWeaponItem.createLegacyExplosion(this.level(), this, x, y, z, power, interaction);
    }

    private boolean isRagoraFireball() {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(this.getItem().getItem());
        return itemId != null && itemId.getNamespace().equals("rwbym") && itemId.getPath().equals("ragorafireball");
    }

    public static boolean shouldUseProjectile(RWBYMWeaponProfiles.WeaponProfile profile) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The 1.12 RWBYGun path spawned EntityBullet for regular ammo; only modern special guns stay hitscan.
        return profile.hasType(RWBYMWeaponProfiles.THROWN)
                || profile.hasType(RWBYMWeaponProfiles.BOOMERANG)
                || profile.hasType(RWBYMWeaponProfiles.ROCKET)
                || profile.hasType(RWBYMWeaponProfiles.BOW)
                || profile.name().contains("boomerang")
                || profile.name().contains("rocket")
                || (profile.ammo() != null && !profile.ammo().isBlank());
    }
}
