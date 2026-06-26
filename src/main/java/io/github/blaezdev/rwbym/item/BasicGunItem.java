package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.registry.RWBYMSounds;
import io.github.blaezdev.rwbym.entity.RWBYMProjectileEntity;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BasicGunItem extends BasicWeaponItem {
    private static final TagKey<net.minecraft.world.item.Item> AMMO_TAG =
            ItemTags.create(new ResourceLocation("rwbym", "ammo"));

    private final RegistryObject<SoundEvent> shootSound;
    private final float damage;

    public BasicGunItem(Properties properties, RegistryObject<SoundEvent> shootSound) {
        super(properties, 3.0D, -2.2D);
        this.shootSound = shootSound;
        this.damage = 6.0F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 8);
        if (!level.isClientSide()) {
            ItemStack ammo = player.getAbilities().instabuild ? defaultAmmo(player) : consumeAmmo(player);
            if (ammo.isEmpty()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_DRY.get(),
                        SoundSource.PLAYERS, 0.6F, 1.0F);
                return InteractionResultHolder.fail(stack);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), this.shootSound.get(),
                    SoundSource.PLAYERS, 0.75F, 0.9F + level.random.nextFloat() * 0.2F);
            shootProjectile(level, player, stack, ammo);
            if (player instanceof ServerPlayer serverPlayer) {
                RWBYMNetwork.sendCameraRecoil(serverPlayer, -4.0F, (player.getRandom().nextFloat() - 0.5F) * 1.2F);
            }
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void shootProjectile(Level level, Player player, ItemStack weapon, ItemStack ammo) {
        float shotDamage = RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.HandofBullets)
                ? this.damage * 2.0F
                : this.damage;
        RWBYMProjectileEntity projectile = new RWBYMProjectileEntity(level, player, ammo, weapon,
                shotDamage, ammoElement(ammo), false, false, false, true);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.5F, 0.6F);
        level.addFreshEntity(projectile);
    }

    static boolean restoreScavengerAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate) && candidate.getCount() < candidate.getMaxStackSize()) {
                candidate.grow(1);
                return true;
            }
        }
        ItemStack ammo = defaultAmmo(player);
        return !ammo.isEmpty() && player.getInventory().add(ammo);
    }

    private static ItemStack consumeAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate)) {
                ItemStack ammo = candidate.copyWithCount(1);
                candidate.shrink(1);
                return ammo;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack defaultAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate)) {
                return candidate.copyWithCount(1);
            }
        }
        Item ammo = ForgeRegistries.ITEMS.getValue(new ResourceLocation("rwbym", "ammo"));
        return ammo == null ? ItemStack.EMPTY : new ItemStack(ammo);
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

    private static String ammoElement(ItemStack ammo) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(ammo.getItem());
        String path = id == null ? "" : id.getPath();
        return RWBYMWeaponItem.legacyAmmoElementKey(path);
    }
}
