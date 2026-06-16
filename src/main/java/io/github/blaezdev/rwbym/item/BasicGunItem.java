package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.registry.RWBYMSounds;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BasicGunItem extends BasicWeaponItem {
    private static final TagKey<net.minecraft.world.item.Item> AMMO_TAG =
            ItemTags.create(new ResourceLocation("rwbym", "ammo"));

    private final RegistryObject<SoundEvent> shootSound;
    private final float damage;
    private final double range;

    public BasicGunItem(Properties properties, RegistryObject<SoundEvent> shootSound) {
        super(properties, 3.0D, -2.2D);
        this.shootSound = shootSound;
        this.damage = 6.0F;
        this.range = 64.0D;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 8);
        if (!level.isClientSide()) {
            if (!player.getAbilities().instabuild && !consumeAmmo(player)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_DRY.get(),
                        SoundSource.PLAYERS, 0.6F, 1.0F);
                return InteractionResultHolder.fail(stack);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), this.shootSound.get(),
                    SoundSource.PLAYERS, 0.75F, 0.9F + level.random.nextFloat() * 0.2F);
            hitScan(level, player);
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void hitScan(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(this.range));
        AABB bounds = player.getBoundingBox().expandTowards(player.getLookAngle().scale(this.range)).inflate(1.0D);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, start, end, bounds,
                entity -> isValidTarget(player, entity));
        if (result != null && result.getEntity() instanceof LivingEntity target) {
            float shotDamage = this.damage;
            if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.HandofBullets)) {
                shotDamage *= 2.0F;
            }
            target.hurt(level.damageSources().playerAttack(player), shotDamage);
            if (target instanceof BasicGrimmEntity && target.isDeadOrDying()) {
                player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(3.0F));
            }
        }
    }

    private static boolean isValidTarget(Player player, Entity entity) {
        return entity != player && !entity.isSpectator() && entity.isPickable();
    }

    private static boolean consumeAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate)) {
                candidate.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static boolean isAmmo(ItemStack stack) {
        if (stack.is(AMMO_TAG)) {
            return true;
        }
        String path = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();
        return path.contains("ammo")
                || path.contains("ammmo")
                || path.contains("bullet")
                || path.contains("shell")
                || path.equals("bolt")
                || path.startsWith("bolt");
    }
}
