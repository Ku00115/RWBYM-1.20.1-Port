package io.github.blaezdev.rwbym.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.entity.RWBYMProjectileEntity;
import io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles.WeaponProfile;
import io.github.blaezdev.rwbym.registry.RWBYMSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class RWBYMWeaponItem extends Item {
    private static final TagKey<Item> AMMO_TAG = ItemTags.create(new ResourceLocation(RWBYM.MOD_ID, "ammo"));
    private final WeaponProfile profile;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private final RegistryObject<SoundEvent> shootSound;

    public RWBYMWeaponItem(WeaponProfile profile, Properties properties, RegistryObject<SoundEvent> shootSound) {
        super(properties);
        this.profile = profile;
        this.shootSound = shootSound;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                Math.max(1.0D, profile.damage() - 1.0D), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                attackSpeed(profile), AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.profile.hasType(RWBYMWeaponProfiles.SCARLET) && hand == InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }
        if (player.isShiftKeyDown() && this.profile.element() != null) {
            return morph(level, player, hand, stack, this.profile.element());
        }
        if (player.isShiftKeyDown() && this.profile.hasMorph()) {
            return morph(level, player, hand, stack);
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.STAFF) || this.profile.elementMelee() != null) {
            applyStaffAbility(level, player);
        }
        if (isRanged()) {
            if (this.profile.charges() && shouldChargeShot()) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            }
            return shoot(level, player, hand, stack);
        }
        if (canBlockOrChannel()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player && isHeld(player, stack) && player.tickCount % 20 == 0) {
            applyHeldPassives(player);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.FLIGHT)) {
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
                if (!player.onGround() && aura.getPercentage() > 0.05F) {
                    Vec3 look = player.getLookAngle();
                    player.push(look.x / 4.0D, look.y / 2.0D, look.z / 4.0D);
                    player.fallDistance = 0.0F;
                    player.hurtMarked = true;
                    aura.useAura(0.15F, false);
                    aura.delayRecharge(30);
                }
            });
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.WALLCLIMB) && player.horizontalCollision) {
            Vec3 look = player.getLookAngle();
            player.push(look.x / 2.0D, Math.max(0.12D, look.y / 2.0D), look.z / 2.0D);
            player.fallDistance = 0.0F;
            player.hurtMarked = true;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.UMBRELLA) && !player.onGround()) {
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(look.x / 2.0D, -0.1D, look.z / 2.0D);
            player.fallDistance = 0.0F;
            player.hurtMarked = true;
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof Player player) || !isRanged()) {
            return;
        }
        int used = this.getUseDuration(stack) - timeLeft;
        float charge = Math.min(1.0F, used / 20.0F);
        if (charge < 0.12F) {
            return;
        }
        shoot(level, player, player.getUsedItemHand(), stack, 0.55F + charge);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (this.profile.hasType(RWBYMWeaponProfiles.BOW) || this.profile.hasType(RWBYMWeaponProfiles.ROCKET)
                || this.profile.hasType(RWBYMWeaponProfiles.SANREI) || this.profile.hasType(RWBYMWeaponProfiles.LETZT)) {
            return UseAnim.BOW;
        }
        return canBlockOrChannel() ? UseAnim.BLOCK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return canBlockOrChannel() || isRanged() ? 72000 : 0;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (this.profile.hasType(RWBYMWeaponProfiles.AXE) && state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return 6.0F;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.PICKAXE) && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return 6.0F;
        }
        if (this.profile.name().equals("leafshield") && state.is(BlockTags.LEAVES)) {
            return 6.0F;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(repair.getItem());
        return id != null && id.getNamespace().equals(RWBYM.MOD_ID) && id.getPath().equals("scrap")
                || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
            int damage = this.profile.hasType(RWBYMWeaponProfiles.TOOL) ? 20 : 1;
            stack.hurtAndBreak(damage, entity, owner -> owner.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide()) {
            applyMeleeEffects(stack, target, attacker);
            if (target instanceof BasicGrimmEntity && target.isDeadOrDying()) {
                attacker.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(4.0F));
            }
            stack.hurtAndBreak(1, attacker, owner -> owner.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        List<String> typeLabels = weaponTypeLabels();
        tooltip.add(Component.literal("Type: " + String.join(", ", typeLabels)).withStyle(ChatFormatting.DARK_GRAY));
        if (this.profile.hasMorph()) {
            tooltip.add(Component.translatable("tooltip.rwbym.weapon.morph").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(" -> " + this.profile.morph()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (isRanged()) {
            tooltip.add(Component.translatable("tooltip.rwbym.weapon.shoot").withStyle(ChatFormatting.GRAY));
            if (this.profile.ammo() != null) {
                tooltip.add(Component.literal("Ammo: " + this.profile.ammo()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.TOME)) {
            tooltip.add(Component.literal("Sneak while held to channel the tome").withStyle(ChatFormatting.BLUE));
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.UMBRELLA)) {
            tooltip.add(Component.literal("Hold use while airborne to glide").withStyle(ChatFormatting.BLUE));
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.WALLCLIMB)) {
            tooltip.add(Component.literal("Hold use against a wall to climb").withStyle(ChatFormatting.BLUE));
        }
        if (this.profile.element() != null || this.profile.elementMelee() != null) {
            tooltip.add(Component.translatable("tooltip.rwbym.weapon.element").withStyle(ChatFormatting.BLUE));
        }
    }

    private InteractionResultHolder<ItemStack> morph(Level level, Player player, InteractionHand hand, ItemStack stack) {
        return morph(level, player, hand, stack, this.profile.morph());
    }

    private InteractionResultHolder<ItemStack> morph(Level level, Player player, InteractionHand hand, ItemStack stack,
            String targetId) {
        if (!level.isClientSide()) {
            Item target = BuiltInRegistries.ITEM.get(new ResourceLocation(targetId));
            if (target != null && target != stack.getItem()) {
                ItemStack morphed = new ItemStack(target, stack.getCount());
                morphed.setTag(stack.getTag() == null ? null : stack.getTag().copy());
                morphed.setDamageValue(Math.min(stack.getDamageValue(), morphed.getMaxDamage()));
                player.setItemInHand(hand, morphed);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_IRON,
                        SoundSource.PLAYERS, 0.65F, 1.25F);
                player.getCooldowns().addCooldown(target, 8);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private void applyHeldPassives(Player player) {
        if (this.profile.hasType(RWBYMWeaponProfiles.TOME) && player.isShiftKeyDown()
                && player.getMainHandItem().getItem() == this) {
            applyTomePassive(player);
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.SANREI) || this.profile.hasType(RWBYMWeaponProfiles.AURAWEAP)
                || this.profile.hasType(RWBYMWeaponProfiles.LETZT)) {
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
                float cost = this.profile.hasType(RWBYMWeaponProfiles.LETZT) ? 0.3F : 0.15F;
                aura.useAura(cost, false);
                if (aura.getAmount() < 1.0F && this.profile.hasMorph()) {
                    ItemStack held = player.getMainHandItem().getItem() == this
                            ? player.getMainHandItem()
                            : player.getOffhandItem();
                    InteractionHand hand = player.getMainHandItem().getItem() == this
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND;
                    morph(player.level(), player, hand, held);
                }
            });
        }
        switch (this.profile.name()) {
            case "pickaxeshield" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, true, false));
            }
            case "rageshield" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
                player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.useAura(0.1F, false));
            }
            case "elucidator", "darkrepulser" ->
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, true, false));
            default -> {
            }
        }
    }

    private InteractionResultHolder<ItemStack> shoot(Level level, Player player, InteractionHand hand, ItemStack stack) {
        return shoot(level, player, hand, stack, 1.0F);
    }

    private InteractionResultHolder<ItemStack> shoot(Level level, Player player, InteractionHand hand, ItemStack stack,
            float power) {
        player.getCooldowns().addCooldown(this, cooldown());
        if (!level.isClientSide()) {
            int shots = Math.max(1, this.profile.bulletCount());
            if (!hasEnoughAura(player)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_DRY.get(),
                        SoundSource.PLAYERS, 0.6F, 0.8F);
                return InteractionResultHolder.fail(stack);
            }
            if (!player.getAbilities().instabuild && !consumeAmmo(player, shots)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_DRY.get(),
                        SoundSource.PLAYERS, 0.6F, 1.0F);
                return InteractionResultHolder.fail(stack);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), this.shootSound.get(),
                    SoundSource.PLAYERS, 0.8F, 0.9F + level.random.nextFloat() * 0.25F);
            if (RWBYMProjectileEntity.shouldUseProjectile(this.profile)) {
                shootProjectile(level, player, stack, shots, power);
            } else {
                for (int i = 0; i < shots; i++) {
                    hitScan(level, player, i, power);
                }
            }
            applyRecoil(player);
            spendShotAura(player);
            if (this.profile.hasType(RWBYMWeaponProfiles.THROWN)) {
                stack.shrink(1);
            } else {
                stack.hurtAndBreak(shotDurabilityCost(), player, owner -> owner.broadcastBreakEvent(hand));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void shootProjectile(Level level, Player player, ItemStack stack, int shots, float power) {
        for (int i = 0; i < shots; i++) {
            float projectileDamage = Math.max(4.0F, this.profile.damage() * 0.7F * power);
            if (this.profile.hasType(RWBYMWeaponProfiles.THROWN)) {
                if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.JAVELIN2)) {
                    projectileDamage *= 3.0F;
                } else if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.JAVELIN1)) {
                    projectileDamage *= 2.0F;
                }
            }
            RWBYMProjectileEntity projectile = new RWBYMProjectileEntity(level, player, stack,
                    projectileDamage, elementKey(),
                    this.profile.hasType(RWBYMWeaponProfiles.BOOMERANG) || this.profile.name().contains("boomerang"));
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, projectileSpeed() * power, 1.0F + i * 0.08F);
            level.addFreshEntity(projectile);
        }
    }

    private void hitScan(Level level, Player player, int shotIndex, float power) {
        double range = Math.max(24.0D, 28.0D + this.profile.projectileSpeed() * 16.0D) * power;
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        if (shotIndex > 0) {
            double spread = (shotIndex - (this.profile.bulletCount() - 1) / 2.0D) * 0.015D;
            direction = direction.add(player.getUpVector(1.0F).scale(spread)).normalize();
        }
        Vec3 end = start.add(direction.scale(range));
        AABB bounds = player.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0D);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, start, end, bounds,
                entity -> isValidTarget(player, entity));
        if (result != null && result.getEntity() instanceof LivingEntity target) {
            float damage = Math.max(4.0F, this.profile.damage() * 0.55F * power);
            if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.HandofBullets)) {
                damage *= 2.0F;
            }
            target.hurt(level.damageSources().playerAttack(player), damage);
            applyElement(target);
            if (target instanceof BasicGrimmEntity && target.isDeadOrDying()) {
                player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(3.0F));
            }
        }
    }

    private void applyMeleeEffects(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyElement(target);
        if (this.profile.name().equals("rageshield") && attacker.isShiftKeyDown()) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2));
            target.hurt(attacker.damageSources().magic(), 16.0F);
            if (attacker instanceof Player player) {
                player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
                    aura.useAura(25.0F, false);
                    aura.delayRecharge(60);
                });
            }
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.SCYTHE) || this.profile.hasType(RWBYMWeaponProfiles.SWORD)) {
            sweep(attacker, target, sweepRadius(attacker), sweepKnockback(attacker));
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.HAMMER) || this.profile.hasType(RWBYMWeaponProfiles.FIST)) {
            int dazeChance = this.profile.hasType(RWBYMWeaponProfiles.HAMMER) ? 25 : 15;
            if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.K02)) {
                dazeChance += 10;
            } else if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.K01)) {
                dazeChance += 5;
            }
            if (attacker.getRandom().nextInt(100) < dazeChance) {
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION,
                        this.profile.hasType(RWBYMWeaponProfiles.HAMMER) ? 120 : 60, 0));
            }
            target.knockback(0.8D, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.WHIP)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            target.knockback(0.45D, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        }
        if (this.profile.name().startsWith("grimm")) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            target.hurt(attacker.damageSources().magic(), 2.0F);
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.DAGGER) && attacker.getRandom().nextInt(100) < 15) {
            target.hurt(attacker.damageSources().mobAttack(attacker), this.profile.damage());
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.DAGGER) && attacker.getRandom().nextInt(100) < 30) {
            if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.CRITICALSTRIKE2) && target.getHealth() >= 50.0F) {
                target.hurt(attacker.damageSources().magic(), 100.0F);
            } else if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.CRITICALSTRIKE1)
                    && target.getHealth() >= 75.0F) {
                target.hurt(attacker.damageSources().magic(), 100.0F);
            } else if (target.getHealth() >= 100.0F) {
                target.hurt(attacker.damageSources().magic(), 100.0F);
            }
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.RAPIER) || this.profile.hasType(RWBYMWeaponProfiles.WINTER)) {
            target.hurt(attacker.damageSources().mobAttack(attacker), Math.max(2.0F, this.profile.damage() * 0.35F));
            if (target.getArmorValue() > 0) {
                if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.PUNCTURE2)) {
                    target.hurt(attacker.damageSources().magic(), 45.0F);
                } else if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.PUNCTURE1)) {
                    target.hurt(attacker.damageSources().magic(), 38.0F);
                }
            }
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.AURAWEAP) && attacker instanceof Player player) {
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
                aura.useAura(2.0F, false);
                aura.delayRecharge(20);
            });
        }
    }

    private void sweep(LivingEntity attacker, LivingEntity target, double radius, double knockback) {
        for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(radius, 0.35D, radius))) {
            if (nearby != attacker && nearby != target && !attacker.isAlliedTo(nearby)) {
                nearby.knockback(knockback, attacker.getX() - nearby.getX(), attacker.getZ() - nearby.getZ());
                nearby.hurt(attacker.damageSources().mobAttack(attacker), Math.max(2.0F, this.profile.damage() * 0.35F));
            }
        }
        attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7F, 1.0F);
    }

    private double sweepRadius(LivingEntity attacker) {
        if (this.profile.hasType(RWBYMWeaponProfiles.SCYTHE)) {
            if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.REACH2)) {
                return 5.0D;
            }
            if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.REACH1)) {
                return 4.0D;
            }
            return 3.0D;
        }
        return 2.25D;
    }

    private double sweepKnockback(LivingEntity attacker) {
        if (this.profile.hasType(RWBYMWeaponProfiles.SWORD)) {
            if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.GLADIATOR2)) {
                return 1.2D;
            }
            if (RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.GLADIATOR1)) {
                return 0.6D;
            }
        }
        return 0.4D;
    }

    private void applyStaffAbility(Level level, Player player) {
        String element = this.profile.elementMelee();
        if ("grav".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 0));
        } else if ("fire".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0));
        } else if ("ice".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
        } else if ("water".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        } else if ("wind".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
        } else if ("light".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 1));
        } else if (this.profile.hasType(RWBYMWeaponProfiles.STAFF)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0));
        }
        if (!level.isClientSide()) {
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
                aura.useAura(4.0F, false);
                aura.delayRecharge(30);
            });
        }
    }

    private void applyTomePassive(Player player) {
        String element = this.profile.elementMelee();
        if ("fire".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 5, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 5, true, false));
        } else if ("water".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3, true, false));
        } else if ("light".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 5, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 5, true, false));
        } else if ("wind".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 7, true, false));
        } else if ("ice".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, true, false));
        } else if ("grav".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false));
        }
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
            aura.useAura(0.15F, false);
            aura.delayRecharge(20);
        });
    }

    private void applyElement(LivingEntity target) {
        String element = elementKey();
        if (element == null) {
            element = "";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.ROCKET)) {
            element = element + " rocket";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.BOW) && this.profile.name().contains("cinder")) {
            element = element + " fire";
        }
        if (this.profile.name().contains("grenade")) {
            element = element + " grenade";
        }
        if (this.profile.name().contains("frost")) {
            element = element + " ice";
        }
        if (element == null) {
            element = this.profile.name();
        }
        if (element.contains("fire")) {
            target.setSecondsOnFire(5);
        } else if (element.contains("ice")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        } else if (element.contains("grav")) {
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 45, 0));
        } else if (element.contains("wind")) {
            target.knockback(1.0D, target.getRandom().nextDouble() - 0.5D, target.getRandom().nextDouble() - 0.5D);
        } else if (element.contains("light") || element.contains("electric")) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        } else if (element.contains("water")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        }
    }

    private String elementKey() {
        String element = this.profile.element() != null ? this.profile.element() : this.profile.elementMelee();
        return element == null ? this.profile.name() : element;
    }

    private boolean hasEnoughAura(Player player) {
        if (this.profile.hasType(RWBYMWeaponProfiles.SANREI) || this.profile.hasType(RWBYMWeaponProfiles.LETZT)) {
            final float required = this.profile.hasType(RWBYMWeaponProfiles.LETZT) ? 20.0F : 10.0F;
            return player.getCapability(RWBYMCapabilities.AURA)
                    .map(aura -> aura.getAmount() >= required)
                    .orElse(false);
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.AURAWEAP) || this.profile.hasType(RWBYMWeaponProfiles.FLIGHT)) {
            return player.getCapability(RWBYMCapabilities.AURA)
                    .map(aura -> aura.getAmount() >= 5.0F)
                    .orElse(false);
        }
        return true;
    }

    private void spendShotAura(Player player) {
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
            if (this.profile.hasType(RWBYMWeaponProfiles.LETZT)) {
                aura.useAura(20.0F, false);
                aura.delayRecharge(60);
            } else if (this.profile.hasType(RWBYMWeaponProfiles.SANREI)) {
                aura.useAura(10.0F, false);
                aura.delayRecharge(60);
            } else if (this.profile.hasType(RWBYMWeaponProfiles.AURAWEAP)
                    || this.profile.hasType(RWBYMWeaponProfiles.FLIGHT)) {
                aura.useAura(5.0F, false);
                aura.delayRecharge(40);
            }
        });
    }

    private void applyRecoil(Player player) {
        int recoil = this.profile.shotRecoil() > 0 ? this.profile.shotRecoil() : this.profile.recoilType() * 4;
        if (recoil <= 0) {
            return;
        }
        Vec3 push = player.getLookAngle().scale(-0.02D * Math.min(recoil, 20));
        if (this.profile.recoilType() == 3) {
            push = push.reverse();
        }
        player.push(push.x, Math.max(0.0D, push.y * 0.2D), push.z);
        player.hurtMarked = true;
    }

    private boolean consumeAmmo(Player player, int amount) {
        if (countAmmo(player) < amount) {
            return false;
        }
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate)) {
                candidate.shrink(1);
                remaining--;
            }
        }
        return remaining == 0;
    }

    private int countAmmo(Player player) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate)) {
                total += candidate.getCount();
            }
        }
        return total;
    }

    private boolean isAmmo(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            return false;
        }
        if (hasProfileAmmo() && profileAmmoMatches(id.toString())) {
            return true;
        }
        if (hasProfileAmmo() && hasAnyRegisteredProfileAmmo()) {
            return false;
        }
        if (stack.is(AMMO_TAG)) {
            return true;
        }
        String path = id.getPath();
        return path.contains("ammo") || path.contains("ammmo") || path.contains("bullet") || path.contains("shell")
                || path.equals("bolt") || path.startsWith("bolt") || path.endsWith("mag");
    }

    private boolean profileAmmoMatches(String id) {
        if (!hasProfileAmmo()) {
            return false;
        }
        for (String profileAmmo : this.profile.ammo().split(",")) {
            if (profileAmmo.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasProfileAmmo() {
        return this.profile.ammo() != null && !this.profile.ammo().isBlank() && !isPlaceholderAmmo(this.profile.ammo());
    }

    private static boolean isPlaceholderAmmo(String ammo) {
        return ammo.equals("none")
                || ammo.equals("rwbym:nuller")
                || ammo.equals("rwbym:nullest")
                || ammo.equals("rwbym:nulls")
                || ammo.equals("rwbym:nuller,rwbym:nullest")
                || ammo.equals("rwbym:nullest,rwbym:nuller")
                || ammo.equals("rwbym:nuller,rwbym:nulls");
    }

    private boolean hasAnyRegisteredProfileAmmo() {
        if (!hasProfileAmmo()) {
            return false;
        }
        for (String id : this.profile.ammo().split(",")) {
            if (isPlaceholderAmmo(id)) {
                continue;
            }
            if (BuiltInRegistries.ITEM.containsKey(new ResourceLocation(id))) {
                return true;
            }
        }
        return false;
    }

    private boolean isRanged() {
        return hasProfileAmmo()
                || this.profile.hasType(RWBYMWeaponProfiles.BOW)
                || this.profile.hasType(RWBYMWeaponProfiles.ROCKET)
                || this.profile.hasType(RWBYMWeaponProfiles.INT_MAG)
                || this.profile.name().endsWith("gun")
                || this.profile.name().endsWith("rifle")
                || this.profile.name().endsWith("pistol")
                || this.profile.name().endsWith("bow")
                || this.profile.name().contains("crossbow");
    }

    private boolean canBlockOrChannel() {
        return this.profile.hasType(RWBYMWeaponProfiles.UMBRELLA)
                || this.profile.hasType(RWBYMWeaponProfiles.AURAWEAP)
                || this.profile.shield()
                || this.profile.canBlock()
                || this.profile.name().endsWith("shield")
                || this.profile.hasType(RWBYMWeaponProfiles.FLIGHT)
                || this.profile.hasType(RWBYMWeaponProfiles.WALLCLIMB);
    }

    private static boolean isHeld(Player player, ItemStack stack) {
        return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }

    private boolean shouldChargeShot() {
        return this.profile.hasType(RWBYMWeaponProfiles.BOW)
                || this.profile.hasType(RWBYMWeaponProfiles.ROCKET)
                || this.profile.hasType(RWBYMWeaponProfiles.SANREI)
                || this.profile.hasType(RWBYMWeaponProfiles.LETZT)
                || this.profile.name().endsWith("bow")
                || this.profile.name().contains("crossbow");
    }

    private List<String> weaponTypeLabels() {
        List<String> labels = new java.util.ArrayList<>();
        addTypeLabel(labels, RWBYMWeaponProfiles.OFFHAND, "Offhand");
        addTypeLabel(labels, RWBYMWeaponProfiles.SWORD, "Sword");
        addTypeLabel(labels, RWBYMWeaponProfiles.LION_HEART, "Shield");
        addTypeLabel(labels, RWBYMWeaponProfiles.RAPIER, "Rapier");
        addTypeLabel(labels, RWBYMWeaponProfiles.SCARLET, "Offhand gun");
        addTypeLabel(labels, RWBYMWeaponProfiles.WINTER, "Rapier");
        addTypeLabel(labels, RWBYMWeaponProfiles.WHIP, "Whip");
        addTypeLabel(labels, RWBYMWeaponProfiles.SCYTHE, "Polearm");
        addTypeLabel(labels, RWBYMWeaponProfiles.DAGGER, "Dagger");
        addTypeLabel(labels, RWBYMWeaponProfiles.INT_MAG, "Internal magazine");
        addTypeLabel(labels, RWBYMWeaponProfiles.JUNIOR, "Internal magazine");
        addTypeLabel(labels, RWBYMWeaponProfiles.ROCKET, "Rocket launcher");
        addTypeLabel(labels, RWBYMWeaponProfiles.TOOL, "Multitool");
        addTypeLabel(labels, RWBYMWeaponProfiles.BOW, "Bow");
        addTypeLabel(labels, RWBYMWeaponProfiles.STAFF, "Staff");
        addTypeLabel(labels, RWBYMWeaponProfiles.UMBRELLA, "Glider");
        addTypeLabel(labels, RWBYMWeaponProfiles.AXE, "Axe");
        addTypeLabel(labels, RWBYMWeaponProfiles.PICKAXE, "Pickaxe");
        addTypeLabel(labels, RWBYMWeaponProfiles.TOME, "Tome");
        addTypeLabel(labels, RWBYMWeaponProfiles.FIST, "Gauntlet");
        addTypeLabel(labels, RWBYMWeaponProfiles.HAMMER, "Blunt");
        addTypeLabel(labels, RWBYMWeaponProfiles.THROWN, "Throwable");
        addTypeLabel(labels, RWBYMWeaponProfiles.WALLCLIMB, "Wall climb");
        addTypeLabel(labels, RWBYMWeaponProfiles.FLIGHT, "Flight");
        addTypeLabel(labels, RWBYMWeaponProfiles.BOOMERANG, "Returning");
        if (this.profile.hasType(RWBYMWeaponProfiles.AURAWEAP) || this.profile.hasType(RWBYMWeaponProfiles.LETZT)
                || this.profile.hasType(RWBYMWeaponProfiles.SANREI)) {
            labels.add("Aura based");
        }
        if (this.profile.name().startsWith("grimm")) {
            labels.add("Grimm");
        }
        if (isRanged() && labels.stream().noneMatch(label -> label.contains("gun") || label.equals("Bow")
                || label.equals("Rocket launcher") || label.equals("Internal magazine"))) {
            labels.add("Gun");
        }
        if (labels.isEmpty()) {
            labels.add("Weapon");
        }
        return labels;
    }

    private void addTypeLabel(List<String> labels, int type, String label) {
        if (this.profile.hasType(type)) {
            labels.add(label);
        }
    }

    private String weaponTypeLabel() {
        if (this.profile.hasType(RWBYMWeaponProfiles.SCYTHE)) {
            return "Scythe";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.RAPIER)) {
            return "Rapier";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.SWORD)) {
            return "Sword";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.BOW)) {
            return "Bow";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.ROCKET)) {
            return "Rocket";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.STAFF)) {
            return "Staff";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.HAMMER)) {
            return "Hammer";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.FIST)) {
            return "Fist";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.THROWN)) {
            return "Thrown";
        }
        if (isRanged()) {
            return "Gun";
        }
        return "Weapon";
    }

    private int shotDurabilityCost() {
        if (this.profile.hasType(RWBYMWeaponProfiles.JUNIOR)) {
            return 30;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.INT_MAG)) {
            return 4;
        }
        return 1;
    }

    private int cooldown() {
        if (this.profile.hasType(RWBYMWeaponProfiles.BOW) || this.profile.hasType(RWBYMWeaponProfiles.ROCKET)) {
            return 18;
        }
        return this.profile.bulletCount() > 1 ? 12 : 8;
    }

    private float projectileSpeed() {
        float speed = Math.max(0.75F, this.profile.projectileSpeed());
        if (this.profile.hasType(RWBYMWeaponProfiles.ROCKET)) {
            return Math.max(1.2F, speed);
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.BOW)) {
            return Math.max(1.5F, speed);
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.BOOMERANG)) {
            return Math.max(1.0F, speed * 0.8F);
        }
        return speed;
    }

    private static boolean isValidTarget(Player player, Entity entity) {
        return entity != player && !entity.isSpectator() && entity.isPickable();
    }

    private static double attackSpeed(WeaponProfile profile) {
        if (profile.hasType(RWBYMWeaponProfiles.DAGGER)) {
            return 1.0D;
        }
        if (profile.hasType(RWBYMWeaponProfiles.RAPIER) || profile.hasType(RWBYMWeaponProfiles.WHIP)
                || profile.hasType(RWBYMWeaponProfiles.WINTER)) {
            return -1.0D;
        }
        if (profile.hasType(RWBYMWeaponProfiles.SWORD) || profile.hasType(RWBYMWeaponProfiles.LION_HEART)
                || profile.hasType(RWBYMWeaponProfiles.AURAWEAP)) {
            return -2.4D;
        }
        return -3.0D;
    }
}
