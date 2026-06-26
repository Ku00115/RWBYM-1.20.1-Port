package io.github.blaezdev.rwbym.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.entity.RWBYMProjectileEntity;
import io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles.WeaponProfile;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import io.github.blaezdev.rwbym.network.SpecialGunActionPacket.Action;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.registry.RWBYMSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RWBYMWeaponItem extends Item {
    private record AmmoShot(String id, String element, float baseDamage, boolean pierce, boolean recoverable) {
        private static final AmmoShot EMPTY = new AmmoShot("", "", -1.0F, false, false);
    }

    private static final TagKey<Item> AMMO_TAG = ItemTags.create(new ResourceLocation(RWBYM.MOD_ID, "ammo"));
    private static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("24806a06-46d6-11ea-b77f-2e728ce88125");
    private static final String SPECIAL_GUN_MAGAZINE_AMMO = "MagazineAmmo";
    private static final String SPECIAL_GUN_CHAMBERED = "BulletChambered";
    private static final String SPECIAL_GUN_HAS_MAGAZINE = "HasMagazine";
    private static final String SPECIAL_GUN_NEEDS_BOLT = "NeedsBolt";
    private static final String SPECIAL_GUN_RELOAD_TICKS = "ReloadTicks";
    private static final String SPECIAL_GUN_BOLT_TICKS = "BoltTicks";
    private static final String SPECIAL_GUN_SLIDE_TICKS = "SlideTicks";
    private static final String SPECIAL_GUN_HAMMER_TICKS = "HammerTicks";
    private static final String SPECIAL_GUN_AUTO_MODE = "AutoMode";
    private static final String SPECIAL_GUN_ADS = "ads";
    private static final String SPECIAL_GUN_TRIGGER_HELD = "held";
    private static final String SPECIAL_GUN_HAMMER_COCKED = "hammer";
    private static final String SPECIAL_GUN_FIRED = "fired";
    private static final String SPECIAL_GUN_MODE_INDEX = "modeindex";
    private static final String SPECIAL_GUN_BURST_COUNT = "burstcount";
    private static final String SPECIAL_GUN_BOLT_UP = "boltup";
    private static final String SPECIAL_GUN_BOLT_BACK = "boltback";
    private static final String SPECIAL_GUN_AUTO_SLIDE_LOCK = "AutoSlideLock";
    private static final String SPECIAL_GUN_LAST_AUTO_SHOT_TICK = "LastAutoShotTick";
    private static final Set<String> INFINITE_AMMO_IDS = Set.of(
            "rwbym:whisperammo",
            "rwbym:noctu",
            "rwbym:noctufire",
            "rwbym:noctuice",
            "rwbym:noctugrav",
            "rwbym:noctulight",
            "rwbym:fetchammo",
            "rwbym:arslanammo",
            "rwbym:pennyswdammo",
            "rwbym:carminesaiammo",
            "rwbym:carminestaffammo",
            "rwbym:pyrrhaspearvammo",
            "rwbym:pyrrhaspearammo",
            "rwbym:chastifolammo",
            "rwbym:thornammo",
            "rwbym:chastifolincreaseammo",
            "rwbym:jnrammo",
            "rwbym:ammov",
            "rwbym:sanreiammo",
            "rwbym:letztammo",
            "rwbym:ammmo",
            "rwbym:ammmmo",
            "rwbym:ammmmmmo",
            "rwbym:rzrbolt",
            "rwbym:gwen",
            "rwbym:thundergodammo",
            "rwbym:sawblade",
            "rwbym:gravitydustcut",
            "rwbym:winddustcut",
            "rwbym:waterdustcut",
            "rwbym:firedustcut",
            "rwbym:dustcut",
            "rwbym:lightdustcut",
            "rwbym:icedustcut",
            "rwbym:ragorafireball");
    private final WeaponProfile profile;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private final RegistryObject<SoundEvent> shootSound;
    private final ResourceLocation morphTargetId;
    private final ResourceLocation elementTargetId;
    private final List<String> profileAmmoIds;

    public RWBYMWeaponItem(WeaponProfile profile, Properties properties, RegistryObject<SoundEvent> shootSound) {
        super(properties);
        this.profile = profile;
        this.shootSound = shootSound;
        this.morphTargetId = resourceLocationOrNull(profile.morph());
        this.elementTargetId = resourceLocationOrNull(profile.element());
        this.profileAmmoIds = parseProfileAmmo(profile.ammo());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                attackDamage(profile) - 1.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                attackSpeed(profile), AttributeModifier.Operation.ADDITION));
        double movementModifier = movementSpeedModifier(profile);
        if (movementModifier != 0.0D) {
            AttributeModifier.Operation operation = "flash".equals(profile.elementMelee()) || "wind".equals(profile.elementMelee())
                    ? AttributeModifier.Operation.ADDITION
                    : AttributeModifier.Operation.MULTIPLY_BASE;
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "Weapon modifier",
                    movementModifier, operation));
        }
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
        if (isSpecialMagazineGun() && player.isShiftKeyDown()) {
            return cycleSpecialGunAction(level, player, hand, stack);
        }
        if (player.isShiftKeyDown() && this.elementTargetId != null) {
            return morph(level, player, hand, stack, this.elementTargetId);
        }
        if (player.isShiftKeyDown() && this.profile.hasMorph()) {
            return morph(level, player, hand, stack);
        }
        if (!player.isShiftKeyDown() && isAuraStorageWeapon()) {
            toggleAuraStorage(stack, player, level);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.STAFF) || this.profile.elementMelee() != null) {
            applyStaffAbility(level, player);
        }
        if (isSpecialMagazineGun()) {
            return InteractionResultHolder.pass(stack);
        }
        if (isAutomaticWeapon()) {
            if (!hasUsableAmmo(player)) {
                playDryFire(level, player);
                if (!level.isClientSide()) {
                    applyNoAmmoSpecial(player);
                }
                return InteractionResultHolder.fail(stack);
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (hasUseReleaseMelee() && hand == meleeUseHand()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (shouldStartUseChannel(hand)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (isRanged()) {
            if (this.profile.charges()) {
                if (!hasUsableAmmo(player)) {
                    playDryFire(level, player);
                    if (!level.isClientSide()) {
                        applyNoAmmoSpecial(player);
                    }
                    return InteractionResultHolder.fail(stack);
                }
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
        if (level.isClientSide() || !(entity instanceof Player player) || !isHeld(player, stack)) {
            return;
        }
        if (player.tickCount % 20 == 0) {
            applyHeldPassives(player);
            syncKoreKosmouWeapon(stack, player);
        }
        tickSpecialGunAnimations(stack);
        tickSpecialGunTrigger(stack, player);
        applyAuraStorage(stack, player);
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
        if (this.profile.name().equals("lucidroseboard")) {
            applyBoardRide(player, stack, 0.5D, 0.3D, 30.0F);
        }
        if (this.profile.name().equals("reese")) {
            applyBoardRide(player, stack, 0.5D, 2.0D, 10.0F);
        }
        if (isAutomaticWeapon() && !isSpecialMagazineGun()) {
            int used = this.getUseDuration(stack) - remainingUseDuration;
            if (used == 1 || used % 3 == 0) {
                shoot(level, player, player.getUsedItemHand(), stack);
            }
        }
        if (isChannelStrengthSword()) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 2, true, false));
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (isAutomaticWeapon()) {
            return;
        }
        if (isChannelStrengthSword()) {
            int used = this.getUseDuration(stack) - timeLeft;
            if (used > 0) {
                stack.hurtAndBreak(20, player,
                        owner -> owner.broadcastBreakEvent(player.getUsedItemHand()));
            }
            return;
        }
        if (hasUseReleaseMelee() && player.getUsedItemHand() == meleeUseHand()) {
            performUseReleaseMelee(level, player, stack);
            return;
        }
        if (isPyrrhaGuardWeapon() && player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            return;
        }
        if (!isRanged()) {
            return;
        }
        if ((this.profile.hasType(RWBYMWeaponProfiles.LION_HEART)
                || this.profile.hasType(RWBYMWeaponProfiles.FLIGHT))
                && this.getUseDuration(stack) - timeLeft < 60) {
            return;
        }
        int used = this.getUseDuration(stack) - timeLeft;
        if (used < minimumChargeTicks()) {
            return;
        }
        float power = releaseShotPower(used);
        if (power < 0.1F) {
            return;
        }
        shoot(level, player, player.getUsedItemHand(), stack, power);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (isSpecialMagazineGun() || isAutomaticWeapon()) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Legacy guns used custom first-person renderers; BOW is the closest vanilla held-use pose for gun fire.
            return UseAnim.BOW;
        }
        if (isKineticBoard()) {
            return UseAnim.BOW;
        }
        if (isRanged() && this.profile.charges() && !canBlockOrChannel()) {
            // Legacy charged guns used custom renderers; BOW avoids the vanilla throw-style first-person pose.
            return UseAnim.BOW;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.BOW) || this.profile.hasType(RWBYMWeaponProfiles.ROCKET)
                || this.profile.hasType(RWBYMWeaponProfiles.SANREI)
                || this.profile.hasType(RWBYMWeaponProfiles.LETZT)
                || this.profile.name().equals("chatareusgun")) {
            return UseAnim.BOW;
        }
        return canBlockOrChannel() ? UseAnim.BLOCK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return canBlockOrChannel() || isRanged() || hasUseReleaseMelee() ? 72000 : 0;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (this.profile.hasType(RWBYMWeaponProfiles.AXE)) {
            return state.is(BlockTags.MINEABLE_WITH_AXE) ? 6.0F : 0.0F;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.PICKAXE)) {
            return state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? 6.0F : 0.0F;
        }
        if (this.profile.name().equals("leafshield")) {
            return state.is(BlockTags.LEAVES) ? 6.0F : 0.0F;
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
            int damage = this.profile.hasType(RWBYMWeaponProfiles.TOOL) ? 21 : 1;
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
        if (isCrescentRose() && this.profile.recoilType() == 1) {
            tooltip.add(Component.literal("Shots kick the wielder backward for burst movement")
                    .withStyle(ChatFormatting.BLUE));
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
        if (isAuraStorageWeapon()) {
            CompoundTag tag = stack.getTag();
            int storedAura = tag == null ? 0 : tag.getInt("Aura");
            boolean auraOn = tag != null && tag.getBoolean("AuraOn");
            tooltip.add(Component.literal("Stored Aura: " + storedAura + " / " + maxStoredAura())
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal("Aura mode: " + (auraOn ? "release" : "store"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (isSpecialMagazineGun()) {
            CompoundTag tag = stack.getOrCreateTag();
            int chambered = tag.getBoolean(SPECIAL_GUN_CHAMBERED) ? 1 : 0;
            tooltip.add(Component.literal("Magazine: " + (tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO) + chambered)
                    + " / " + specialGunMagazineSize()).withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal("Mode: " + specialGunModeLabel(tag) + ", Chamber: "
                    + (tag.getBoolean(SPECIAL_GUN_CHAMBERED) ? "loaded" : "empty")
                    + (tag.getBoolean(SPECIAL_GUN_ADS) ? ", ADS" : "")).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private InteractionResultHolder<ItemStack> morph(Level level, Player player, InteractionHand hand, ItemStack stack) {
        return morph(level, player, hand, stack, this.morphTargetId);
    }

    private InteractionResultHolder<ItemStack> morph(Level level, Player player, InteractionHand hand, ItemStack stack,
            ResourceLocation targetId) {
        if (!level.isClientSide() && targetId != null) {
            Item target = BuiltInRegistries.ITEM.get(targetId);
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
                float cost = this.profile.hasType(RWBYMWeaponProfiles.LETZT) ? 6.0F : 3.0F;
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
            case "leafshield" ->
                    player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.useAura(1.0F, false));
            case "pickaxeshield" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, true, false));
            }
            case "rageshield" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
                player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.useAura(2.0F, false));
            }
            default -> {
            }
        }
    }

    private void syncKoreKosmouWeapon(ItemStack stack, Player player) {
        String requiredChest = switch (this.profile.name()) {
            case "kkfire" -> "korekosmoufire";
            case "kkice" -> "korekosmouice";
            case "kkwind" -> "korekosmouwind";
            default -> null;
        };
        if (requiredChest == null) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ResourceLocation chestId = ForgeRegistries.ITEMS.getKey(chest.getItem());
        if (chestId != null && chestId.getNamespace().equals(RWBYM.MOD_ID) && chestId.getPath().equals(requiredChest)) {
            stack.setDamageValue(Math.min(chest.getDamageValue(), stack.getMaxDamage()));
            return;
        }
        stack.hurtAndBreak(365, player, owner -> owner.broadcastBreakEvent(
                player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
    }

    private InteractionResultHolder<ItemStack> shoot(Level level, Player player, InteractionHand hand, ItemStack stack) {
        return shoot(level, player, hand, stack, 1.0F);
    }

    private InteractionResultHolder<ItemStack> shoot(Level level, Player player, InteractionHand hand, ItemStack stack,
            float power) {
        if (!level.isClientSide()) {
            int shotCount = shotMultiplier(player);
            if (!this.profile.hasType(RWBYMWeaponProfiles.THROWN) && enchantLevel(stack, "double_shot") > 0) {
                shotCount *= 2;
            }
            int shots = Math.max(1, this.profile.bulletCount()) * shotCount;
            if (!hasEnoughAura(player)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_DRY.get(),
                        SoundSource.PLAYERS, 0.6F, 0.8F);
                return InteractionResultHolder.fail(stack);
            }
            List<AmmoShot> ammoShots = isSpecialMagazineGun()
                    ? consumeSpecialGunAmmo(stack, player, shots)
                    : player.getAbilities().instabuild ? creativeAmmo(shots) : consumeAmmo(player, shots);
            if (ammoShots.size() < shots) {
                playDryFire(level, player);
                applyNoAmmoSpecial(player);
                return InteractionResultHolder.fail(stack);
            }
            int cooldown = cooldown(stack);
            if (cooldown > 0) {
                player.getCooldowns().addCooldown(this, cooldown);
            }
            if (this.shootSound != null) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), this.shootSound.get(),
                        SoundSource.PLAYERS, 1.0F, shotSoundPitch(level, isSpecialMagazineGun() ? 1.0F : power));
            }
            if (shouldFireVisibleProjectile()) {
                shootProjectile(level, player, stack, ammoShots, power, shotCount);
            } else {
                for (int i = 0; i < shots; i++) {
                    hitScan(level, player, stack, i, power, ammoShots.get(i));
                }
            }
            applyRecoil(player, stack);
            spendShotAura(player);
            if (this.profile.hasType(RWBYMWeaponProfiles.THROWN)
                    && !this.profile.hasType(RWBYMWeaponProfiles.BOOMERANG)
                    && !this.profile.name().contains("boomerang")) {
                stack.shrink(1);
            } else {
                int durabilityCost = weaponShotDurabilityCost();
                if (durabilityCost > 0) {
                    stack.hurtAndBreak(durabilityCost, player, owner -> owner.broadcastBreakEvent(hand));
                    damagePairedOffhand(player, stack, hand, durabilityCost);
                }
            }
            markSpecialGunFired(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private InteractionResultHolder<ItemStack> cycleSpecialGunAction(Level level, Player player, InteractionHand hand,
            ItemStack stack) {
        if (!level.isClientSide()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tryLoadSpecialGun(stack, player)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_MAG_IN.get(),
                        SoundSource.PLAYERS, 0.8F, 1.0F);
                tag.putInt(SPECIAL_GUN_RELOAD_TICKS, 12);
                player.getCooldowns().addCooldown(this, 12);
            } else if (shouldCycleSpecialGunAction(stack) && tryCycleSpecialGunAction(stack)) {
                playSpecialGunCycleSound(level, player, stack);
                tag.putInt(isBoltActionSpecialGun() ? SPECIAL_GUN_BOLT_TICKS : SPECIAL_GUN_SLIDE_TICKS, 10);
                player.getCooldowns().addCooldown(this, 10);
            } else if (canEjectSpecialGunMagazine(stack) && ejectSpecialGunMagazine(stack, player)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_MAG_OUT.get(),
                        SoundSource.PLAYERS, 0.8F, 1.0F);
                tag.putInt(SPECIAL_GUN_RELOAD_TICKS, 8);
                player.getCooldowns().addCooldown(this, 8);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_DRY.get(),
                        SoundSource.PLAYERS, 0.5F, 1.0F);
                player.getCooldowns().addCooldown(this, 6);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void shootProjectile(Level level, Player player, ItemStack stack, List<AmmoShot> ammoShots, float power,
            int shotCount) {
        for (int i = 0; i < ammoShots.size(); i++) {
            AmmoShot ammoShot = ammoShots.get(i);
            float projectileDamage = ammoShot.baseDamage() >= 0.0F
                    ? ammoShot.baseDamage() * power
                    : Math.max(4.0F, this.profile.damage() * 0.7F * power);
            projectileDamage *= ammoDamageMultiplier(ammoShot);
            if (this.profile.hasType(RWBYMWeaponProfiles.THROWN)) {
                if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.JAVELIN2)) {
                    projectileDamage *= 3.0F;
                } else if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.JAVELIN1)) {
                    projectileDamage *= 2.0F;
                }
            } else if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.HandofBullets)) {
                projectileDamage *= 2.0F;
            }
            ItemStack display = projectileDisplay(stack, ammoShot);
            boolean fastProjectile = isFastProjectileWeapon();
            boolean recoverableProjectile = ammoShot.recoverable()
                    || (this.profile.hasType(RWBYMWeaponProfiles.THROWN)
                            && !this.profile.hasType(RWBYMWeaponProfiles.BOOMERANG)
                            && !this.profile.name().contains("boomerang"));
            RWBYMProjectileEntity projectile = new RWBYMProjectileEntity(level, player, display, stack,
                    projectileDamage, projectileElementKey(ammoShot, stack),
                    this.profile.hasType(RWBYMWeaponProfiles.BOOMERANG) || this.profile.name().contains("boomerang"),
                    ammoShot.pierce(), recoverableProjectile, fastProjectile);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, projectileSpeed(stack) * power, projectileSpread(level, stack, shotCount));
            level.addFreshEntity(projectile);
        }
    }

    private void hitScan(Level level, Player player, ItemStack stack, int shotIndex, float power, AmmoShot ammoShot) {
        float effectivePower = isSpecialMagazineGun() ? 1.0F : power;
        double range = hitScanRange(stack, ammoShot) * effectivePower;
        Vec3 start = player.getEyePosition();
        Vec3 direction = hitScanDirection(level, player, stack, shotIndex);
        if (!isSpecialMagazineGun() && shotIndex > 0) {
            double spread = (shotIndex - (this.profile.bulletCount() - 1) / 2.0D)
                    * 0.015D * barrelAccuracy(stack);
            direction = direction.add(player.getUpVector(1.0F).scale(spread)).normalize();
        }
        Vec3 intendedEnd = start.add(direction.scale(range));
        BlockHitResult blockHit = level.clip(new ClipContext(start, intendedEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? intendedEnd : blockHit.getLocation();
        AABB bounds = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, start, end, bounds,
                entity -> isValidTarget(player, entity));
        Vec3 trailEnd = result == null ? end : result.getLocation();
        spawnBulletTrail(level, start, trailEnd);
        if (result != null && result.getEntity() instanceof LivingEntity target) {
            float damage = ammoShot.baseDamage() >= 0.0F
                    ? ammoShot.baseDamage() * effectivePower
                    : Math.max(4.0F, this.profile.damage() * 0.55F * effectivePower);
            damage *= ammoDamageMultiplier(ammoShot);
            if (RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.HandofBullets)) {
                damage *= 2.0F;
            }
            target.hurt(level.damageSources().playerAttack(player), damage);
            applyElement(target, ammoShot, stack);
            int knockShot = enchantLevel(stack, "knock_shot");
            if (knockShot > 0) {
                target.knockback(knockShot * 0.8D, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
            if (target instanceof BasicGrimmEntity && target.isDeadOrDying()) {
                player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(3.0F));
            }
            applyWeaponModifierKillEffects(stack, player, target);
        }
    }

    private void spawnBulletTrail(Level level, Vec3 start, Vec3 end) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 path = end.subtract(start);
        double length = path.length();
        if (length <= 0.05D) {
            return;
        }
        Vec3 step = path.normalize();
        int samples = Mth.clamp((int) (length * 2.0D), 4, 48);
        double spacing = length / samples;
        for (int i = 1; i <= samples; i++) {
            Vec3 pos = start.add(step.scale(spacing * i));
            serverLevel.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1,
                    0.01D, 0.01D, 0.01D, 0.0D);
        }
    }

    private float shotSoundPitch(Level level, float power) {
        return 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + Math.min(power, 1.0F) * 0.5F;
    }

    private double hitScanRange(ItemStack stack, AmmoShot ammoShot) {
        if (isSpecialMagazineGun()) {
            String id = ammoShot == null ? "" : ammoShot.id();
            String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
            if (path.equals("50bmg") || path.equals("hecate_mag")) {
                return 100.0D;
            }
            if (path.equals("p90bullet") || path.equals("p90_mag")) {
                return 64.0D;
            }
        }
        return Math.max(24.0D, 28.0D + projectileSpeed(stack) * 16.0D);
    }

    private Vec3 hitScanDirection(Level level, Player player, ItemStack stack, int shotIndex) {
        Vec3 forward = player.getLookAngle();
        if (!isSpecialMagazineGun()) {
            return forward;
        }
        float entityAccuracy = specialGunEntityAccuracy(player, stack);
        float gunAccuracy = this.profile.name().equals("p90") ? 1.0F : 0.0F;
        double cone = level.random.nextDouble() * (entityAccuracy + gunAccuracy) / 360.0D * Math.PI;
        double roll = level.random.nextDouble() * Math.PI * 2.0D;
        Vec3 up = player.getUpVector(1.0F);
        Vec3 right = forward.cross(up);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        up = right.cross(forward).normalize();
        return forward.scale(Math.cos(cone))
                .add(right.scale(Math.sin(cone) * Math.cos(roll)))
                .add(up.scale(Math.sin(cone) * Math.sin(roll)))
                .normalize();
    }

    private float specialGunEntityAccuracy(Player player, ItemStack stack) {
        Vec3 movement = player.getDeltaMovement();
        double x = movement.x * 20.0D;
        double y = movement.y * 20.0D + 1.568D;
        double z = movement.z * 20.0D;
        boolean ads = stack.hasTag() && stack.getOrCreateTag().getBoolean(SPECIAL_GUN_ADS);
        return (float) ((ads ? 1.0F : 10.0F) + Math.sqrt(x * x + y * y + z * z));
    }

    private void applyMeleeEffects(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyElement(target, AmmoShot.EMPTY, stack);
        if (this.profile.name().equals("rageshield") && attacker.isShiftKeyDown()) {
            EvokerFangs fangs = new EvokerFangs(attacker.level(), target.getX(), target.getY(), target.getZ(),
                    target.getYRot(), 0, attacker);
            attacker.level().addFreshEntity(fangs);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 10));
            target.hurt(attacker.damageSources().magic(), 50.0F);
            if (attacker instanceof Player player) {
                player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
                    aura.useAura(100.0F, false);
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
        if (this.profile.hasType(RWBYMWeaponProfiles.EMBER_CELICA)) {
            applyEmberCelicaMelee(stack, target, attacker);
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
        if (attacker instanceof Player player) {
            applyWeaponModifierKillEffects(stack, player, target);
        }
    }

    private void applyEmberCelicaMelee(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setSecondsOnFire(6);
        target.knockback(1.15D, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        if (!(attacker instanceof Player player)) {
            return;
        }
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
            if (aura.getAmount() < 2.0F) {
                return;
            }
            aura.useAura(2.0F, false);
            aura.delayRecharge(30);
            AABB blastBox = target.getBoundingBox().inflate(2.25D, 0.75D, 2.25D);
            for (LivingEntity nearby : target.level().getEntitiesOfClass(LivingEntity.class, blastBox)) {
                if (nearby == attacker || attacker.isAlliedTo(nearby)) {
                    continue;
                }
                nearby.setSecondsOnFire(4);
                if (nearby != target) {
                    nearby.hurt(attacker.damageSources().playerAttack(player), 6.0F);
                    nearby.knockback(0.65D, attacker.getX() - nearby.getX(), attacker.getZ() - nearby.getZ());
                }
            }
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.55F, 0.85F);
        });
    }

    private void performUseReleaseMelee(Level level, Player player, ItemStack stack) {
        double reach = this.profile.hasType(RWBYMWeaponProfiles.WHIP) || this.profile.name().startsWith("grimm")
                ? 8.0D : 4.5D;
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        Vec3 end = start.add(direction.scale(reach));
        AABB bounds = player.getBoundingBox().expandTowards(direction.scale(reach)).inflate(0.75D);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, start, end, bounds,
                entity -> isValidTarget(player, entity));
        if (result == null || !(result.getEntity() instanceof LivingEntity target)) {
            return;
        }
        float damage = this.profile.hasType(RWBYMWeaponProfiles.WHIP) || this.profile.name().startsWith("grimm")
                ? 14.0F : 18.0F;
        target.knockback(0.4D, player.getX() - target.getX(), player.getZ() - target.getZ());
        target.hurt(level.damageSources().playerAttack(player), damage);
        applyElement(target, AmmoShot.EMPTY, stack);
        if (this.profile.hasType(RWBYMWeaponProfiles.WHIP)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
        }
        applyWeaponModifierKillEffects(stack, player, target);
        stack.hurtAndBreak(this.profile.hasType(RWBYMWeaponProfiles.WHIP) ? 5 : 1, player,
                owner -> owner.broadcastBreakEvent(player.getUsedItemHand()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 0.9F, 1.0F);
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
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 5));
        } else if ("fire".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 5));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 5));
        } else if ("ice".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
        } else if ("water".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3));
        } else if ("wind".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 7));
        } else if ("light".equals(element)) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 5));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 5));
        } else if (this.profile.hasType(RWBYMWeaponProfiles.STAFF)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 5));
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
            aura.useAura(3.0F, false);
            aura.delayRecharge(20);
        });
    }

    private void applyElement(LivingEntity target) {
        applyElement(target, AmmoShot.EMPTY, ItemStack.EMPTY);
    }

    private void applyElement(LivingEntity target, AmmoShot ammoShot) {
        applyElement(target, ammoShot, ItemStack.EMPTY);
    }

    private void applyElement(LivingEntity target, AmmoShot ammoShot, ItemStack stack) {
        String element = elementKey();
        if (element == null) {
            element = "";
        }
        element = element + " " + ammoElementKey(ammoShot);
        element = appendModifierElements(element, stack);
        if (this.profile.hasType(RWBYMWeaponProfiles.ROCKET)) {
            element = element + " rocket";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.BOW) && this.profile.name().contains("cinder")) {
            element = element + " fire";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.EMBER_CELICA)) {
            element = element + " fire ember";
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
            target.setSecondsOnFire(10);
        } else if (element.contains("ice")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
        } else if (element.contains("grav")) {
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 5));
        } else if (element.contains("wind")) {
            target.knockback(1.0D, target.getRandom().nextDouble() - 0.5D, target.getRandom().nextDouble() - 0.5D);
        } else if (element.contains("light") || element.contains("electric")) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        } else if (element.contains("water")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        }
        if (element.contains("absorption")) {
            target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 2));
        }
        if (element.contains("poison")) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        }
        if (element.contains("flarefrost")) {
            target.setSecondsOnFire(4);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
        Float explosionPower = explosionPower(element);
        if (explosionPower != null) {
            target.level().explode(target, target.getX(), target.getY(), target.getZ(), explosionPower,
                    Level.ExplosionInteraction.NONE);
        }
    }

    private String elementKey() {
        String element = this.profile.element() != null ? this.profile.element() : this.profile.elementMelee();
        return element == null ? this.profile.name() : element;
    }

    private String projectileElementKey(AmmoShot ammoShot, ItemStack stack) {
        String element = elementKey();
        element += " " + ammoElementKey(ammoShot);
        element = appendModifierElements(element, stack);
        if (this.profile.hasType(RWBYMWeaponProfiles.ROCKET)) {
            element += " rocket";
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.EMBER_CELICA)) {
            element += " fire ember";
        }
        if (this.profile.name().contains("grenade")) {
            element += " grenade";
        }
        if (this.profile.name().contains("frost")) {
            element += " ice";
        }
        return element;
    }

    private String appendModifierElements(String element, ItemStack stack) {
        if (stack.isEmpty()) {
            return element;
        }
        if (hasEnchant(stack, "poison_shot")) {
            element += " poison";
        }
        if (hasEnchant(stack, "flare_frost_shot")) {
            element += " flarefrost";
        }
        return element;
    }

    public static Float explosionPower(String element) {
        if (element == null || !element.contains("explosion")) {
            return null;
        }
        // Original ExplosionAmmoHit stores exact integer strengths; keep those values shared by hitscan/projectiles.
        if (element.contains("explosion10")) {
            return 10.0F;
        }
        if (element.contains("explosion4")) {
            return 4.0F;
        }
        if (element.contains("explosion3")) {
            return 3.0F;
        }
        if (element.contains("explosion2")) {
            return 2.0F;
        }
        if (element.contains("explosion1")) {
            return 1.0F;
        }
        if (element.contains("explosion0")) {
            return 0.0F;
        }
        return 1.5F;
    }

    private void applyWeaponModifierKillEffects(ItemStack stack, Player player, LivingEntity target) {
        RWBYMWeaponModifierHelper.applyKillModifierEffects(stack, player, target);
    }

    boolean restoreScavengerAmmo(ItemStack stack, Player player) {
        if (isSpecialMagazineGun()) {
            CompoundTag tag = stack.getOrCreateTag();
            int magazineAmmo = tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO);
            if (magazineAmmo < specialGunMagazineSize()) {
                tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, magazineAmmo + 1);
                return true;
            }
            if (!tag.getBoolean(SPECIAL_GUN_CHAMBERED)) {
                tag.putBoolean(SPECIAL_GUN_CHAMBERED, true);
                return true;
            }
            return false;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.INT_MAG)) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 2));
            return true;
        }
        if (!hasProfileAmmo() || this.profileAmmoIds.isEmpty()) {
            return false;
        }
        ResourceLocation ammoId = resourceLocationOrNull(this.profileAmmoIds.get(0));
        if (ammoId == null) {
            return false;
        }
        Item ammoItem = BuiltInRegistries.ITEM.get(ammoId);
        if (ammoItem == Items.AIR) {
            return false;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (candidate.is(ammoItem) && candidate.getCount() < candidate.getMaxStackSize()) {
                candidate.grow(1);
                return true;
            }
        }
        return player.getInventory().add(new ItemStack(ammoItem));
    }

    private void applyNoAmmoSpecial(Player player) {
        if (this.profile.recoilType() == 3 && player.onGround()) {
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(look.x, look.y, look.z);
            player.fallDistance = 0.0F;
            player.hurtMarked = true;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.WALLCLIMB) && player.horizontalCollision) {
            Vec3 look = player.getLookAngle();
            player.push(look.x / 2.0D, Math.max(0.12D, look.y / 2.0D), look.z / 2.0D);
            player.fallDistance = 0.0F;
            player.hurtMarked = true;
        }
    }

    private void playDryFire(Level level, Player player) {
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.GLOCK_DRY.get(),
                    SoundSource.PLAYERS, 0.6F, 1.0F);
        }
    }

    private void applyBoardRide(Player player, ItemStack stack, double forwardPower, double hoverHeight, float impactScale) {
        Vec3 look = player.getLookAngle();
        Vec3 movement = player.getDeltaMovement();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() > 1.0E-4D) {
            forward = forward.normalize();
        }
        double inputScale = player.isSprinting() ? 2.0D : player.isShiftKeyDown() ? 0.5D : 1.0D;
        Vec3 targetHorizontal = forward.scale(forwardPower * inputScale);
        Vec3 currentHorizontal = new Vec3(movement.x, 0.0D, movement.z);
        Vec3 adjustedHorizontal = currentHorizontal.add(targetHorizontal.subtract(currentHorizontal).scale(0.05D));

        BlockPos ground = findGroundBelow(player);
        double distanceAboveGround = player.getY() - (ground.getY() + 1.0D);
        double yMotion = movement.y / 0.98D;
        double lift = hoverHeight - distanceAboveGround;
        double damping = 0.3D;
        if (lift < -1.0D) {
            damping /= -lift;
        }
        if (lift < 0.0D) {
            lift = 0.0D;
            damping = damping / 2.0D * (yMotion < 0.0D ? -yMotion * 5.0D : 0.0D);
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        yMotion += lift * 0.15D - damping * yMotion;

        player.setDeltaMovement(adjustedHorizontal.x, yMotion, adjustedHorizontal.z);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;

        double speed = player.getDeltaMovement().horizontalDistance();
        if (speed < 0.16D) {
            return;
        }
        AABB impactBox = player.getBoundingBox().inflate(1.25D, 0.35D, 1.25D);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, impactBox)) {
            if (target == player || player.isAlliedTo(target)) {
                continue;
            }
            float damage = Math.min(40.0F, Math.max(2.0F, (float) speed * impactScale));
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                target.knockback(0.6D, player.getX() - target.getX(), player.getZ() - target.getZ());
                stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }

    private BlockPos findGroundBelow(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        while (pos.getY() > level.getMinBuildHeight() && level.isEmptyBlock(pos)) {
            pos = pos.below();
        }
        return pos;
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

    private void applyRecoil(Player player, ItemStack stack) {
        int recoil = this.profile.shotRecoil() > 0 ? this.profile.shotRecoil() : this.profile.recoilType() * 4;
        recoil = Math.round(recoil * frameRecoilMultiplier(stack));
        if (recoil <= 0) {
            return;
        }
        kickView(player, recoil);
        if (isCrescentRose()) {
            applyCrescentRoseMovementRecoil(player);
            return;
        }
        Vec3 push = player.getLookAngle().scale(-0.02D * Math.min(recoil, 20));
        if (this.profile.recoilType() == 3) {
            push = push.reverse();
        }
        player.push(push.x, Math.max(0.0D, push.y * 0.2D), push.z);
        player.hurtMarked = true;
    }

    private void kickView(Player player, int recoil) {
        float pitch = -Math.min(recoil, 20);
        float yaw = (player.getRandom().nextFloat() - 0.5F) * Math.min(recoil, 12) * 0.35F;
        player.setXRot(Mth.clamp(player.getXRot() + pitch, -90.0F, 90.0F));
        player.setYRot(player.getYRot() + yaw);
        if (player instanceof ServerPlayer serverPlayer) {
            RWBYMNetwork.sendCameraRecoil(serverPlayer, pitch, yaw);
        }
    }

    private void applyCrescentRoseMovementRecoil(Player player) {
        if (this.profile.recoilType() != 1) {
            return;
        }
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(-look.x, -look.y * 0.5D, -look.z);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
    }

    private boolean isCrescentRose() {
        return this.profile.name().startsWith("crescent");
    }

    private boolean isSpecialMagazineGun() {
        return this.profile.name().equals("p90") || this.profile.name().equals("hecate2");
    }

    public boolean isSpecialMagazineGunItem() {
        return isSpecialMagazineGun();
    }

    public void handleSpecialGunAction(ItemStack stack, ServerPlayer player, InteractionHand hand, Action action,
            boolean down) {
        if (!isSpecialMagazineGun() || player.getItemInHand(hand) != stack) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        initSpecialGunState(tag);
        switch (action) {
            case ADS -> {
                tag.putBoolean(SPECIAL_GUN_ADS, down);
                if (down && this.profile.name().equals("hecate2")) {
                    player.startUsingItem(hand);
                } else if (!down && player.getUseItem() == stack) {
                    player.stopUsingItem();
                }
            }
            case SHOOT -> handleSpecialGunTrigger(stack, player, hand, down);
            case CYCLE_ACTION -> {
                if (down) {
                    performSpecialGunCycle(stack, player, true);
                }
            }
            case REMOVE_ROUND -> {
                if (down) {
                    performSpecialGunCycle(stack, player, false);
                }
            }
            case HAMMER -> {
                if (down) {
                    tag.putBoolean(SPECIAL_GUN_HAMMER_COCKED, true);
                    tag.putInt(SPECIAL_GUN_HAMMER_TICKS, 3);
                    playSpecialGunSound(player.level(), player, SoundEvents.LEVER_CLICK, 0.35F, 1.35F);
                }
            }
            case MAG_RELEASE -> {
                if (down) {
                    performSpecialGunMagazineRelease(stack, player);
                }
            }
            case INSERT_MAG -> {
                if (down) {
                    performSpecialGunMagazineInsert(stack, player);
                }
            }
            case FIRE_SELECT -> {
                if (down) {
                    cycleSpecialGunFireMode(stack, player);
                }
            }
        }
    }

    private boolean isBoltActionSpecialGun() {
        return this.profile.name().equals("hecate2");
    }

    private int specialGunMagazineSize() {
        return this.profile.name().equals("hecate2") ? 7 : 50;
    }

    private String specialGunMagazineItemId() {
        return this.profile.name().equals("hecate2") ? "rwbym:hecate_mag" : "rwbym:p90_mag";
    }

    private String specialGunAmmoItemId() {
        return this.profile.name().equals("hecate2") ? "rwbym:50bmg" : "rwbym:p90bullet";
    }

    private void initSpecialGunState(CompoundTag tag) {
        if (!tag.contains(SPECIAL_GUN_MODE_INDEX)) {
            tag.putInt(SPECIAL_GUN_MODE_INDEX, this.profile.name().equals("p90") ? 1 : 0);
        }
        if (!tag.contains(SPECIAL_GUN_HAMMER_COCKED)) {
            tag.putBoolean(SPECIAL_GUN_HAMMER_COCKED, true);
        }
        tag.putBoolean(SPECIAL_GUN_AUTO_MODE, specialGunMode(tag) == 1);
    }

    private void handleSpecialGunTrigger(ItemStack stack, ServerPlayer player, InteractionHand hand, boolean down) {
        CompoundTag tag = stack.getOrCreateTag();
        initSpecialGunState(tag);
        if (!down) {
            tag.putBoolean(SPECIAL_GUN_TRIGGER_HELD, false);
            tag.putInt(SPECIAL_GUN_BURST_COUNT, 0);
            if (player.getUseItem() == stack) {
                player.stopUsingItem();
            }
            return;
        }
        if (specialGunMode(tag) == 3) {
            playSpecialGunSound(player.level(), player, RWBYMSounds.GLOCK_DRY.get(), 0.45F, 0.85F);
            return;
        }
        if (tag.getBoolean(SPECIAL_GUN_TRIGGER_HELD) && specialGunMode(tag) == 0) {
            return;
        }
        tag.putBoolean(SPECIAL_GUN_TRIGGER_HELD, true);
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // SpecialGunActionPacket bypasses vanilla right-click use, so explicitly mark the hand active for first-person pose sync.
        player.startUsingItem(hand);
        fireSpecialGunOnce(stack, player, hand);
    }

    private void tickSpecialGunTrigger(ItemStack stack, Player player) {
        if (!isSpecialMagazineGun() || !(player instanceof ServerPlayer serverPlayer) || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        initSpecialGunState(tag);
        if (!tag.getBoolean(SPECIAL_GUN_TRIGGER_HELD) || specialGunMode(tag) != 1) {
            return;
        }
        int lastShot = tag.getInt(SPECIAL_GUN_LAST_AUTO_SHOT_TICK);
        int interval = Math.max(2, cooldown(stack) / 2);
        if (serverPlayer.tickCount - lastShot >= interval) {
            fireSpecialGunOnce(stack, serverPlayer, InteractionHand.MAIN_HAND);
        }
    }

    private void fireSpecialGunOnce(ItemStack stack, ServerPlayer player, InteractionHand hand) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean(SPECIAL_GUN_HAMMER_COCKED)) {
            playSpecialGunSound(player.level(), player, RWBYMSounds.GLOCK_DRY.get(), 0.45F, 0.8F);
            return;
        }
        if (isBoltActionSpecialGun() && tag.getBoolean(SPECIAL_GUN_NEEDS_BOLT)) {
            playSpecialGunSound(player.level(), player, RWBYMSounds.GLOCK_DRY.get(), 0.45F, 0.75F);
            return;
        }
        int before = tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO) + (tag.getBoolean(SPECIAL_GUN_CHAMBERED) ? 1 : 0);
        InteractionResultHolder<ItemStack> result = shoot(player.level(), player, hand, stack, 1.0F);
        if (result.getResult().consumesAction()) {
            tag.putInt(SPECIAL_GUN_LAST_AUTO_SHOT_TICK, player.tickCount);
            tag.putBoolean(SPECIAL_GUN_HAMMER_COCKED, false);
            tag.putBoolean(SPECIAL_GUN_FIRED, true);
            if (specialGunMode(tag) == 2) {
                tag.putInt(SPECIAL_GUN_BURST_COUNT, tag.getInt(SPECIAL_GUN_BURST_COUNT) + 1);
                if (tag.getInt(SPECIAL_GUN_BURST_COUNT) >= 3) {
                    tag.putBoolean(SPECIAL_GUN_TRIGGER_HELD, false);
                    tag.putInt(SPECIAL_GUN_BURST_COUNT, 0);
                }
            }
            if (!isBoltActionSpecialGun() && before <= 1) {
                tag.putBoolean(SPECIAL_GUN_AUTO_SLIDE_LOCK, true);
            }
        }
    }

    private void performSpecialGunCycle(ItemStack stack, ServerPlayer player, boolean lockOrBolt) {
        CompoundTag tag = stack.getOrCreateTag();
        initSpecialGunState(tag);
        if (isBoltActionSpecialGun()) {
            boolean raised = tag.getInt(SPECIAL_GUN_BOLT_UP) >= 2;
            if (lockOrBolt) {
                tag.putInt(SPECIAL_GUN_BOLT_UP, raised ? 0 : 2);
                tag.putInt(SPECIAL_GUN_BOLT_TICKS, 8);
                tag.putBoolean(SPECIAL_GUN_HAMMER_COCKED, true);
                playSpecialGunSound(player.level(), player,
                        raised ? RWBYMSounds.RIFLE_BOLT_DOWN.get() : RWBYMSounds.RIFLE_BOLT_UP.get(), 0.65F, 1.0F);
            } else if (raised) {
                tag.putInt(SPECIAL_GUN_BOLT_BACK, tag.getInt(SPECIAL_GUN_BOLT_BACK) >= 2 ? 0 : 2);
                tag.putInt(SPECIAL_GUN_BOLT_TICKS, 10);
                if (tag.getInt(SPECIAL_GUN_BOLT_BACK) >= 2) {
                    ejectChamberedRound(stack, player);
                    playSpecialGunSound(player.level(), player, RWBYMSounds.RIFLE_BOLT_BACK.get(), 0.65F, 1.0F);
                } else {
                    tag.putBoolean(SPECIAL_GUN_NEEDS_BOLT, false);
                    chamberSpecialGunRound(stack);
                    playSpecialGunSound(player.level(), player, RWBYMSounds.RIFLE_BOLT_FORWARD.get(), 0.65F, 1.0F);
                }
            }
            return;
        }
        if (lockOrBolt && tag.getInt(SPECIAL_GUN_SLIDE_TICKS) > 0) {
            tag.putBoolean(SPECIAL_GUN_AUTO_SLIDE_LOCK, !tag.getBoolean(SPECIAL_GUN_AUTO_SLIDE_LOCK));
            return;
        }
        ejectChamberedRound(stack, player);
        tag.putInt(SPECIAL_GUN_SLIDE_TICKS, 10);
        tag.putInt(SPECIAL_GUN_HAMMER_TICKS, 3);
        tag.putBoolean(SPECIAL_GUN_HAMMER_COCKED, true);
        if (!tag.getBoolean(SPECIAL_GUN_AUTO_SLIDE_LOCK)) {
            chamberSpecialGunRound(stack);
        }
        playSpecialGunCycleSound(player.level(), player, stack);
    }

    private void performSpecialGunMagazineRelease(ItemStack stack, ServerPlayer player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (ejectSpecialGunMagazine(stack, player)) {
            tag.putInt(SPECIAL_GUN_RELOAD_TICKS, 8);
            playSpecialGunSound(player.level(), player, RWBYMSounds.GLOCK_MAG_OUT.get(), 0.8F, 1.0F);
        } else {
            playSpecialGunSound(player.level(), player, RWBYMSounds.GLOCK_DRY.get(), 0.45F, 1.0F);
        }
    }

    private void performSpecialGunMagazineInsert(ItemStack stack, ServerPlayer player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tryLoadSpecialGun(stack, player)) {
            tag.putInt(SPECIAL_GUN_RELOAD_TICKS, 12);
            tag.putBoolean(SPECIAL_GUN_HAMMER_COCKED, true);
            playSpecialGunSound(player.level(), player, RWBYMSounds.GLOCK_MAG_IN.get(), 0.8F, 1.0F);
        } else {
            playSpecialGunSound(player.level(), player, RWBYMSounds.GLOCK_DRY.get(), 0.45F, 1.0F);
        }
    }

    private void cycleSpecialGunFireMode(ItemStack stack, ServerPlayer player) {
        CompoundTag tag = stack.getOrCreateTag();
        int maxModes = this.profile.name().equals("p90") ? 2 : 1;
        int next = (tag.getInt(SPECIAL_GUN_MODE_INDEX) + 1) % maxModes;
        tag.putInt(SPECIAL_GUN_MODE_INDEX, next);
        tag.putBoolean(SPECIAL_GUN_AUTO_MODE, specialGunMode(tag) == 1);
        playSpecialGunSound(player.level(), player, SoundEvents.LEVER_CLICK, 0.35F, 1.0F + next * 0.25F);
    }

    private int specialGunMode(CompoundTag tag) {
        if (this.profile.name().equals("p90")) {
            return Math.floorMod(tag.getInt(SPECIAL_GUN_MODE_INDEX), 2);
        }
        return 0;
    }

    private String specialGunModeLabel(CompoundTag tag) {
        return switch (specialGunMode(tag)) {
            case 1 -> "AUTO";
            case 2 -> "BURST";
            case 3 -> "SAFE";
            default -> "SEMI";
        };
    }

    private List<AmmoShot> consumeSpecialGunAmmo(ItemStack stack, Player player, int amount) {
        if (player.getAbilities().instabuild) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(SPECIAL_GUN_CHAMBERED, true);
            tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, Math.max(tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO),
                    specialGunMagazineSize() - 1));
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (isBoltActionSpecialGun() && tag.getBoolean(SPECIAL_GUN_NEEDS_BOLT)) {
            return Collections.emptyList();
        }
        List<AmmoShot> shots = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            if (!tag.getBoolean(SPECIAL_GUN_CHAMBERED)) {
                if (!chamberSpecialGunRound(stack)) {
                    break;
                }
            }
            tag.putBoolean(SPECIAL_GUN_CHAMBERED, false);
            shots.add(ammoShot(specialGunAmmoItemId()));
            if (isBoltActionSpecialGun()) {
                tag.putBoolean(SPECIAL_GUN_NEEDS_BOLT, true);
                break;
            }
            chamberSpecialGunRound(stack);
        }
        return shots;
    }

    private boolean tryLoadSpecialGun(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO) > 0 || tag.getBoolean(SPECIAL_GUN_CHAMBERED)) {
            return false;
        }
        int capacity = specialGunMagazineSize();
        if (player.getAbilities().instabuild) {
            tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, capacity);
            tag.putBoolean(SPECIAL_GUN_HAS_MAGAZINE, true);
            tag.putBoolean(SPECIAL_GUN_NEEDS_BOLT, false);
            chamberSpecialGunRound(stack);
            return true;
        }
        ItemStack magazine = removeOneSpecialGunMagazine(player);
        if (!magazine.isEmpty()) {
            tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, Math.min(capacity, RWBYMMagazineItem.getAmmoCount(magazine)));
            tag.putBoolean(SPECIAL_GUN_HAS_MAGAZINE, true);
            tag.putBoolean(SPECIAL_GUN_NEEDS_BOLT, false);
            chamberSpecialGunRound(stack);
            return true;
        }
        int loaded = consumeItems(player, specialGunAmmoItemId(), capacity);
        if (loaded > 0) {
            tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, loaded);
            tag.putBoolean(SPECIAL_GUN_HAS_MAGAZINE, true);
            tag.putBoolean(SPECIAL_GUN_NEEDS_BOLT, false);
            chamberSpecialGunRound(stack);
            return true;
        }
        return false;
    }

    private boolean canEjectSpecialGunMagazine(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean(SPECIAL_GUN_HAS_MAGAZINE);
    }

    private boolean ejectSpecialGunMagazine(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean(SPECIAL_GUN_HAS_MAGAZINE) || player.getAbilities().instabuild) {
            tag.putBoolean(SPECIAL_GUN_HAS_MAGAZINE, false);
            tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, 0);
            return true;
        }
        Item magazineItem = BuiltInRegistries.ITEM.get(resourceLocationOrNull(specialGunMagazineItemId()));
        if (magazineItem == null) {
            return false;
        }
        ItemStack magazine = new ItemStack(magazineItem);
        RWBYMMagazineItem.setAmmoCount(magazine, tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO));
        player.getInventory().placeItemBackInInventory(magazine);
        tag.putBoolean(SPECIAL_GUN_HAS_MAGAZINE, false);
        tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, 0);
        return true;
    }

    private boolean tryCycleSpecialGunAction(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean hadWork = tag.getBoolean(SPECIAL_GUN_NEEDS_BOLT) || !tag.getBoolean(SPECIAL_GUN_CHAMBERED);
        tag.putBoolean(SPECIAL_GUN_NEEDS_BOLT, false);
        return chamberSpecialGunRound(stack) || hadWork;
    }

    private boolean shouldCycleSpecialGunAction(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean(SPECIAL_GUN_NEEDS_BOLT)
                || (!tag.getBoolean(SPECIAL_GUN_CHAMBERED) && tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO) > 0);
    }

    private boolean chamberSpecialGunRound(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getBoolean(SPECIAL_GUN_CHAMBERED)) {
            return false;
        }
        int magazineAmmo = tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO);
        if (magazineAmmo <= 0) {
            return false;
        }
        tag.putInt(SPECIAL_GUN_MAGAZINE_AMMO, magazineAmmo - 1);
        tag.putBoolean(SPECIAL_GUN_CHAMBERED, true);
        return true;
    }

    private void ejectChamberedRound(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean(SPECIAL_GUN_CHAMBERED)) {
            return;
        }
        tag.putBoolean(SPECIAL_GUN_CHAMBERED, false);
        if (!player.getAbilities().instabuild) {
            Item item = BuiltInRegistries.ITEM.get(resourceLocationOrNull(specialGunAmmoItemId()));
            if (item != null) {
                player.getInventory().placeItemBackInInventory(new ItemStack(item));
            }
        }
    }

    private void playSpecialGunSound(Level level, Player player, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private void markSpecialGunFired(ItemStack stack) {
        if (!isSpecialMagazineGun()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(SPECIAL_GUN_FIRED, true);
        tag.putInt(SPECIAL_GUN_HAMMER_TICKS, 4);
        if (isBoltActionSpecialGun()) {
            tag.putInt(SPECIAL_GUN_BOLT_TICKS, 6);
        } else {
            tag.putInt(SPECIAL_GUN_SLIDE_TICKS, 4);
            tag.putBoolean(SPECIAL_GUN_HAMMER_COCKED, true);
        }
    }

    private void tickSpecialGunAnimations(ItemStack stack) {
        if (!isSpecialMagazineGun() || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        initSpecialGunState(tag);
        decrementTagTimer(tag, SPECIAL_GUN_RELOAD_TICKS);
        decrementTagTimer(tag, SPECIAL_GUN_BOLT_TICKS);
        decrementTagTimer(tag, SPECIAL_GUN_SLIDE_TICKS);
        decrementTagTimer(tag, SPECIAL_GUN_HAMMER_TICKS);
        if (tag.getBoolean(SPECIAL_GUN_FIRED) && tag.getInt(SPECIAL_GUN_HAMMER_TICKS) <= 0) {
            tag.putBoolean(SPECIAL_GUN_FIRED, false);
        }
        tag.putBoolean(SPECIAL_GUN_AUTO_MODE, specialGunMode(tag) == 1);
    }

    private static void decrementTagTimer(CompoundTag tag, String key) {
        int value = tag.getInt(key);
        if (value > 0) {
            tag.putInt(key, value - 1);
        }
    }

    private void playSpecialGunCycleSound(Level level, Player player, ItemStack stack) {
        if (isBoltActionSpecialGun()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.RIFLE_BOLT_BACK.get(),
                    SoundSource.PLAYERS, 0.6F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.RIFLE_BOLT_FORWARD.get(),
                    SoundSource.PLAYERS, 0.6F, 1.0F);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.COLT_SLIDE_BACK.get(),
                    SoundSource.PLAYERS, 0.6F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.COLT_SLIDE_FORWARD.get(),
                    SoundSource.PLAYERS, 0.6F, 1.0F);
        }
    }

    private static boolean consumeOneItem(Player player, String itemId) {
        return consumeItems(player, itemId, 1) == 1;
    }

    private ItemStack removeOneSpecialGunMagazine(Player player) {
        String magazineId = specialGunMagazineItemId();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(candidate.getItem());
            if (id != null && id.toString().equals(magazineId)) {
                ItemStack magazine = candidate.copyWithCount(1);
                candidate.shrink(1);
                return magazine;
            }
        }
        return ItemStack.EMPTY;
    }

    private static int consumeItems(Player player, String itemId, int amount) {
        int consumed = 0;
        for (int i = 0; i < player.getInventory().getContainerSize() && consumed < amount; i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(candidate.getItem());
            if (id != null && id.toString().equals(itemId)) {
                int take = Math.min(amount - consumed, candidate.getCount());
                candidate.shrink(take);
                consumed += take;
            }
        }
        return consumed;
    }

    public static float specialGunPredicate(ItemStack stack, String predicate) {
        if (!(stack.getItem() instanceof RWBYMWeaponItem weapon) || !weapon.isSpecialMagazineGun()) {
            return 0.0F;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return predicate.equals("empty") ? 1.0F : 0.0F;
        }
        return switch (predicate) {
            case "chambered" -> tag.getBoolean(SPECIAL_GUN_CHAMBERED) ? 1.0F : 0.0F;
            case "empty" -> !tag.getBoolean(SPECIAL_GUN_CHAMBERED)
                    && tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO) <= 0 ? 1.0F : 0.0F;
            case "loaded" -> tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO) > 0 ? 1.0F : 0.0F;
            case "mag" -> tag.getBoolean(SPECIAL_GUN_HAS_MAGAZINE) ? 1.0F : 0.0F;
            case "magout" -> tag.getBoolean(SPECIAL_GUN_HAS_MAGAZINE) ? 0.0F : 1.0F;
            case "mag_anim" -> tag.getInt(SPECIAL_GUN_RELOAD_TICKS) / 12.0F;
            case "bullets" -> tag.getInt(SPECIAL_GUN_MAGAZINE_AMMO)
                    + (tag.getBoolean(SPECIAL_GUN_CHAMBERED) ? 1.0F : 0.0F);
            case "bolt", "boltback" -> Math.max(tag.getInt(SPECIAL_GUN_BOLT_BACK),
                    tag.getInt(SPECIAL_GUN_BOLT_TICKS) > 0 ? 2 : 0);
            case "boltopen" -> tag.getInt(SPECIAL_GUN_BOLT_BACK) >= 2 ? 1.0F : 0.0F;
            case "boltup" -> Math.max(tag.getInt(SPECIAL_GUN_BOLT_UP),
                    tag.getInt(SPECIAL_GUN_BOLT_TICKS) > 0 ? 2 : 0);
            case "slide", "charge_handle" -> tag.getBoolean(SPECIAL_GUN_AUTO_SLIDE_LOCK) ? 4.0F
                    : tag.getInt(SPECIAL_GUN_SLIDE_TICKS) > 0 ? 4.0F : 0.0F;
            case "slideback" -> tag.getBoolean(SPECIAL_GUN_AUTO_SLIDE_LOCK)
                    || tag.getInt(SPECIAL_GUN_SLIDE_TICKS) > 0 ? 1.0F : 0.0F;
            case "hammer" -> tag.getBoolean(SPECIAL_GUN_HAMMER_COCKED) ? 1.0F : 0.0F;
            case "fired" -> tag.getBoolean(SPECIAL_GUN_FIRED) || tag.getInt(SPECIAL_GUN_HAMMER_TICKS) > 0 ? 1.0F : 0.0F;
            case "auto" -> tag.getBoolean(SPECIAL_GUN_AUTO_MODE) ? 1.0F : 0.0F;
            case "mode" -> weapon.specialGunMode(tag);
            case "modeindex" -> tag.getInt(SPECIAL_GUN_MODE_INDEX);
            case "ads" -> tag.getBoolean(SPECIAL_GUN_ADS) ? 1.0F : 0.0F;
            case "held" -> tag.getBoolean(SPECIAL_GUN_TRIGGER_HELD) ? 1.0F : 0.0F;
            case "burstcount" -> tag.getInt(SPECIAL_GUN_BURST_COUNT);
            default -> 0.0F;
        };
    }

    private List<AmmoShot> consumeAmmo(Player player, int amount) {
        if (hasInfiniteProfileAmmo()) {
            return Collections.nCopies(amount, ammoShot(this.profileAmmoIds.get(0)));
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate)) {
                AmmoShot ammoShot = ammoShot(candidate);
                if (!isInfiniteAmmo(candidate)) {
                    candidate.shrink(1);
                }
                return Collections.nCopies(amount, ammoShot);
            }
        }
        return Collections.emptyList();
    }

    private boolean hasUsableAmmo(Player player) {
        if (player.getAbilities().instabuild || hasInfiniteProfileAmmo()) {
            return hasProfileAmmo();
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.isEmpty() && isAmmo(candidate)) {
                return true;
            }
        }
        return false;
    }

    private List<AmmoShot> creativeAmmo(int amount) {
        AmmoShot ammoShot = AmmoShot.EMPTY;
        if (hasProfileAmmo()) {
            ammoShot = ammoShot(this.profileAmmoIds.get(0));
        }
        return Collections.nCopies(amount, ammoShot);
    }

    private boolean hasInfiniteProfileAmmo() {
        return hasProfileAmmo() && INFINITE_AMMO_IDS.contains(this.profileAmmoIds.get(0));
    }

    private boolean isInfiniteAmmo(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && INFINITE_AMMO_IDS.contains(id.toString());
    }

    private boolean isAmmo(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            return false;
        }
        if ((stack.is(Items.ARROW) || stack.is(Items.TIPPED_ARROW)) && profileAmmoMatches(id.toString())) {
            return true;
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
        for (String profileAmmo : this.profileAmmoIds) {
            if (profileAmmo.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasProfileAmmo() {
        return !this.profileAmmoIds.isEmpty();
    }

    private static boolean isPlaceholderAmmo(String ammo) {
        if (ammo == null) {
            return true;
        }
        String normalized = ammo.trim().toLowerCase(java.util.Locale.ROOT);
        String path = normalized.contains(":") ? normalized.substring(normalized.indexOf(':') + 1) : normalized;
        return normalized.equals("none")
                || path.equals("none")
                || path.equals("nuller")
                || path.equals("nullest")
                || path.equals("nulls")
                || path.equals("arrows32")
                || normalized.equals("rwbym:nuller,rwbym:nullest")
                || normalized.equals("rwbym:nullest,rwbym:nuller")
                || normalized.equals("rwbym:nuller,rwbym:nulls")
                || normalized.equals("minecraft:arrows32,minecraft:nullest");
    }

    private boolean hasAnyRegisteredProfileAmmo() {
        if (!hasProfileAmmo()) {
            return false;
        }
        for (String id : this.profileAmmoIds) {
            ResourceLocation resourceLocation = resourceLocationOrNull(id);
            if (resourceLocation != null && BuiltInRegistries.ITEM.containsKey(resourceLocation)) {
                return true;
            }
        }
        return false;
    }

    private static ResourceLocation resourceLocationOrNull(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return new ResourceLocation(id);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static List<String> parseProfileAmmo(String ammo) {
        if (ammo == null || ammo.isBlank() || isPlaceholderAmmo(ammo)) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (String id : ammo.split(",")) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty() && !isPlaceholderAmmo(trimmed)) {
                ids.add(trimmed);
            }
        }
        return ids.isEmpty() ? Collections.emptyList() : List.copyOf(ids);
    }

    private AmmoShot ammoShot(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? AmmoShot.EMPTY : ammoShot(id.toString());
    }

    private AmmoShot ammoShot(String id) {
        if (id == null || id.isBlank()) {
            return AmmoShot.EMPTY;
        }
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        float baseDamage = ammoBaseDamage(path);
        boolean pierce = path.contains("hadesmag")
                || path.contains("carminestaffammo")
                || path.contains("pyrrhaspear")
                || path.contains("hardlightmagazines");
        boolean recoverable = isRecoverableAmmo(path);
        String element = legacyAmmoElementKey(path);
        return new AmmoShot(id, element, baseDamage, pierce, recoverable);
    }

    public static String legacyAmmoElementKey(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String element = "";
        if (path.contains("fire") || path.contains("firedust")) {
            element += " fire";
        }
        if (path.contains("ice") || path.contains("icedust")) {
            element += " ice";
        }
        if (path.contains("grav") || path.contains("gravity")) {
            element += " grav";
        }
        if (path.contains("wind")) {
            element += " wind";
        }
        if (path.contains("water")) {
            element += " water";
        }
        if (path.contains("light") || path.contains("electric") || path.contains("thunder")) {
            element += " light";
        }
        String explosion = legacyExplosionElement(path);
        if (!explosion.isEmpty()) {
            element += " " + explosion;
        }
        if (path.contains("thundergod")) {
            element += " flyingthundergod";
        }
        if (path.contains("dustcut") || path.contains("bolt") || path.contains("spl")) {
            element += " cloud";
        }
        if (path.equals("dustcrystalhardlight") || path.equals("dustcrystal")
                || path.equals("waterdustcrystal") || path.equals("waterdust")) {
            element += " absorption";
        }
        return element.trim();
    }

    private static String legacyExplosionElement(String path) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy RWBYAmmoItem attached ExplosionAmmoHit per ammo id, so exact ids cover effects name parsing misses.
        return switch (path) {
            case "lightdustcrystal", "noctulight", "spllight", "emflareammo", "crelectricmag",
                    "hadesmag", "hardlightmagazines", "lightdust", "chatelectricmag", "boltlight" -> "explosion0";
            case "jnrammo", "neptammo", "gwen" -> "explosion1";
            case "gravitydustcut" -> "explosion2";
            case "magnammo", "magnaampammo", "winddustcut", "waterdustcut", "firedustcut",
                    "dustcut", "lightdustcut", "icedustcut" -> "explosion3";
            case "extasisammo" -> "explosion4";
            case "letztammo" -> "explosion10";
            default -> "";
        };
    }

    private static float ammoBaseDamage(String path) {
        return switch (path) {
            case "50bmg" -> 60.0F;
            case "p90bullet", "p90_mag" -> 8.0F;
            case "hecate_mag" -> 60.0F;
            case "letztammo" -> 100.0F;
            case "chastifolammo", "thornammo" -> 35.0F;
            case "fetchammo", "extasisammo" -> 30.0F;
            case "sanreiammo" -> 25.0F;
            case "hadesmag", "carminestaffammo", "pyrrhaspearvammo", "pyrrhaspearammo",
                    "nevermorefeather" -> 20.0F;
            case "arslanammo", "pennyswdammo" -> 18.0F;
            case "crmag", "crgravmag", "crfiremag", "crelectricmag", "carminesaiammo",
                    "hardlightmagazines", "magnammo", "magnaampammo", "jnrammo", "bolt", "boltgrav", "boltfire",
                    "boltlight", "boltice", "boltwind" -> 15.0F;
            case "whisperammo", "noctu", "noctufire", "noctuice", "noctugrav", "noctulight" -> 14.0F;
            case "spl", "splfire", "splice", "splgrav", "spllight" -> 12.0F;
            case "gammag", "gamfiremag", "gamicemag", "gamgravmag", "firedustcrystal",
                    "icedustcrystal", "lightdustcrystal", "gravitydustcrystal", "firedust",
                    "icedust", "lightdust", "gravitydust", "firedust2", "ammmo", "ammmmo", "ammmmmmo",
                    "chatmag", "chatgravmag",
                    "chatfiremag", "chatelectricmag", "neptammo", "thundergodammo" -> 10.0F;
            case "ammov" -> 6.0F;
            case "emammo", "emammmo", "emflareammo" -> 5.0F;
            case "emfireammo", "rzrbolt", "gwen", "sawblade" -> 4.0F;
            case "chastifolincreaseammo" -> 3.0F;
            case "dustcrystalhardlight", "dustcrystal", "waterdustcrystal", "winddustcrystal",
                    "waterdust", "winddust", "gravitydustcut", "winddustcut", "waterdustcut",
                    "firedustcut", "dustcut", "lightdustcut", "icedustcut", "ragorafireball" -> 0.0F;
            default -> -1.0F;
        };
    }

    private static boolean isRecoverableAmmo(String path) {
        return path.equals("fetchammo")
                || path.equals("arslanammo")
                || path.equals("pennyswdammo")
                || path.equals("carminesaiammo")
                || path.equals("carminestaffammo")
                || path.equals("pyrrhaspearvammo")
                || path.equals("pyrrhaspearammo")
                || path.equals("chastifolammo")
                || path.equals("thornammo")
                || path.equals("bolt")
                || path.equals("boltgrav")
                || path.equals("boltfire")
                || path.equals("boltlight")
                || path.equals("boltice")
                || path.equals("boltwind");
    }

    private ItemStack projectileDisplay(ItemStack weapon, AmmoShot ammoShot) {
        if (this.profile.hasType(RWBYMWeaponProfiles.THROWN)) {
            return weapon.copyWithCount(1);
        }
        ResourceLocation ammoId = ammoShot == null ? null : resourceLocationOrNull(ammoShot.id());
        if (ammoId != null && ammoId.getNamespace().equals("minecraft")) {
            Item vanillaProjectile = BuiltInRegistries.ITEM.get(ammoId);
            return vanillaProjectile == Items.AIR ? new ItemStack(Items.ARROW) : new ItemStack(vanillaProjectile);
        }
        String displayPath = projectileDisplayPath(ammoShot);
        ResourceLocation displayId = resourceLocationOrNull(RWBYM.MOD_ID + ":" + displayPath);
        if (displayId != null && BuiltInRegistries.ITEM.containsKey(displayId)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(displayId));
        }
        return new ItemStack(RWBYMItems.SIMPLE_ITEMS.getOrDefault("entitybullet", RWBYMItems.ICON).get());
    }

    private String projectileDisplayPath(AmmoShot ammoShot) {
        ResourceLocation ammoId = ammoShot == null ? null : resourceLocationOrNull(ammoShot.id());
        String path = ammoId == null ? "" : ammoId.getPath();
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original RWBYAmmoItem stored a separate render item; this keeps ammo inventory icons out of flight.
        return switch (path) {
            case "dustcrystalhardlight", "dustcrystal", "waterdustcrystal", "waterdust" -> "entityweisswater";
            case "winddustcrystal", "winddust" -> "entityweisswind";
            case "firedustcrystal", "firedust", "emfireammo" -> "entityweissfire";
            case "icedustcrystal", "icedust" -> "entityweissice";
            case "lightdustcrystal", "lightdust", "emflareammo" -> "entityweisslight";
            case "gravitydustcrystal", "gravitydust" -> "entityweissgravity";
            case "whisperammo" -> "whisperingblossomammo";
            case "noctu", "noctufire", "noctuice", "noctugrav", "noctulight",
                    "spl", "splfire", "splice", "splgrav", "spllight",
                    "gammag", "gamfiremag", "gamicemag", "gamgravmag", "emammo" -> "entitysmallbullet";
            case "fetchammo" -> "fetchboomerang";
            case "arslanammo" -> "arslan";
            case "pennyswdammo" -> "pennyswd";
            case "carminesaiammo" -> "carminesai";
            case "carminestaffammo" -> "carminestaff";
            case "pyrrhaspearvammo" -> "pyrrhaspearv";
            case "pyrrhaspearammo" -> "pyrrhaspear";
            case "chastifolammo" -> "chastifol";
            case "thornammo" -> "thorn";
            case "chastifolincreaseammo" -> "chastifolincrease";
            case "magnammo", "magnaampammo" -> "entitygrenade";
            case "jnrammo" -> "entityrocket";
            case "extasisammo" -> "entityextasisammo";
            case "ammov", "sanreiammo", "letztammo" -> "entitybulletv";
            case "rzrbolt" -> "razorboltknife";
            case "gwen" -> "gwenknife";
            case "thundergodammo" -> "entitythundergod";
            case "sawblade" -> "saw";
            case "gravitydustcut" -> "gravitydustcrystalcut";
            case "winddustcut" -> "winddustcrystalcut";
            case "waterdustcut" -> "waterdustcrystalcut";
            case "firedustcut" -> "firedustcrystalcut";
            case "dustcut", "icedustcut" -> "dustcrystalcut";
            case "lightdustcut" -> "lightdustcrystalcut";
            case "bolt" -> "entitybolt";
            case "boltgrav" -> "entityboltgrav";
            case "boltfire" -> "entityboltfire";
            case "boltlight" -> "entityboltlight";
            case "boltice" -> "entityboltice";
            case "boltwind" -> "entityboltwind";
            case "nevermorefeather" -> "nevermorefeather";
            case "ragorafireball" -> "ragorafireball";
            default -> "entitybullet";
        };
    }

    private String ammoElementKey(AmmoShot ammoShot) {
        return ammoShot == null ? "" : ammoShot.element();
    }

    private float ammoDamageMultiplier(AmmoShot ammoShot) {
        return 1.0F;
    }

    private boolean isRanged() {
        return hasProfileAmmo();
    }

    private boolean shouldFireVisibleProjectile() {
        return RWBYMProjectileEntity.shouldUseProjectile(this.profile) && !isSpecialMagazineGun();
    }

    private boolean isFastProjectileWeapon() {
        return isRanged()
                && !this.profile.hasType(RWBYMWeaponProfiles.BOW)
                && !this.profile.hasType(RWBYMWeaponProfiles.ROCKET)
                && !this.profile.hasType(RWBYMWeaponProfiles.BOOMERANG)
                && !this.profile.hasType(RWBYMWeaponProfiles.THROWN)
                && !this.profile.name().contains("boomerang")
                && !this.profile.name().contains("rocket");
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

    private boolean isKineticBoard() {
        String name = this.profile.name();
        return name.equals("reese") || name.equals("lucidroseboard");
    }

    private boolean shouldStartUseChannel(InteractionHand hand) {
        if (this.profile.shield() && hand == InteractionHand.OFF_HAND) {
            return true;
        }
        if (this.profile.canBlock()) {
            return true;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.UMBRELLA)
                || this.profile.hasType(RWBYMWeaponProfiles.AURAWEAP)
                || this.profile.hasType(RWBYMWeaponProfiles.FLIGHT)
                || this.profile.hasType(RWBYMWeaponProfiles.WALLCLIMB)) {
            return true;
        }
        return false;
    }

    private boolean hasUseReleaseMelee() {
        return isOffhandBlade() || this.profile.hasType(RWBYMWeaponProfiles.WHIP) || this.profile.name().startsWith("grimm");
    }

    private InteractionHand meleeUseHand() {
        return isOffhandBlade() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private boolean isOffhandBlade() {
        String name = this.profile.name();
        return this.profile.hasType(RWBYMWeaponProfiles.OFFHAND)
                || this.profile.hasType(RWBYMWeaponProfiles.WINTER)
                || name.contains("gambol")
                || name.contains("rvn");
    }

    private boolean isPyrrhaGuardWeapon() {
        String name = this.profile.name();
        return name.equals("pyrrharifle") || name.equals("pyrrhaspear");
    }

    private boolean isChannelStrengthSword() {
        String name = this.profile.name();
        return name.equals("elucidator") || name.equals("darkrepulser");
    }

    private boolean isAutomaticWeapon() {
        String name = this.profile.name();
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy minigun/flamethrower weapons use a held-use firing loop, not the vanilla instant-use hand swing.
        return name.equals("p90")
                || name.equals("cocogun")
                || name.equals("cocogunv")
                || name.equals("infinity")
                || name.equals("oobleckflamethrower");
    }

    private boolean isDualWieldWeapon() {
        String name = this.profile.name();
        return name.contains("stormflower")
                || name.contains("ember")
                || name.contains("tyrian")
                || name.contains("fox")
                || name.contains("emerald")
                || name.contains("mariascythe")
                || name.contains("sunnunchuck")
                || name.contains("reese")
                || name.contains("infinity")
                || name.contains("penny");
    }

    private int shotMultiplier(Player player) {
        return isDualWieldWeapon() && player.getMainHandItem().getItem() == this
                && player.getOffhandItem().getItem() == this ? 2 : 1;
    }

    private void damagePairedOffhand(Player player, ItemStack firedStack, InteractionHand hand, int durabilityCost) {
        if (!isDualWieldWeapon() || hand != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() == firedStack.getItem()) {
            offhand.hurtAndBreak(durabilityCost, player, owner -> owner.broadcastBreakEvent(InteractionHand.OFF_HAND));
        }
    }

    private boolean isAuraStorageWeapon() {
        String name = this.profile.name();
        return name.equals("bangle") || name.equals("hbangle") || name.equals("ozpincanetravel");
    }

    private int maxStoredAura() {
        return this.profile.name().equals("ozpincanetravel") ? 8000 : 2000;
    }

    private void toggleAuraStorage(ItemStack stack, Player player, Level level) {
        if (level.isClientSide()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("Aura")) {
            tag.putInt("Aura", 1);
        }
        boolean auraOn = !tag.getBoolean("AuraOn");
        tag.putBoolean("AuraOn", auraOn);
        tag.putBoolean("AuraON", auraOn);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 0.35F, auraOn ? 1.35F : 0.8F);
    }

    private void applyAuraStorage(ItemStack stack, Player player) {
        if (!isAuraStorageWeapon() || !player.isShiftKeyDown() || player.getMainHandItem() != stack) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("Aura")) {
            tag.putInt("Aura", 1);
            tag.putBoolean("AuraOn", false);
            tag.putBoolean("AuraON", false);
        }
        player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
            int stored = tag.getInt("Aura");
            boolean auraOn = tag.getBoolean("AuraOn") || tag.getBoolean("AuraON");
            if (auraOn && stored > 5 && aura.getAmount() < aura.getMaxAura()) {
                aura.addAmount(2.0F);
                tag.putInt("Aura", Math.max(0, stored - 2));
            } else if (!auraOn && aura.getAmount() > 5.0F && stored < maxStoredAura()) {
                aura.useAura(2.0F, false);
                tag.putInt("Aura", Math.min(maxStoredAura(), stored + 2));
            }
        });
    }

    private static boolean isHeld(Player player, ItemStack stack) {
        return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }

    private boolean shouldChargeShot() {
        return this.profile.hasType(RWBYMWeaponProfiles.BOW)
                || this.profile.hasType(RWBYMWeaponProfiles.ROCKET)
                || this.profile.hasType(RWBYMWeaponProfiles.SANREI)
                || this.profile.hasType(RWBYMWeaponProfiles.LETZT)
                || this.profile.name().equals("chatareusgun")
                || this.profile.name().endsWith("bow")
                || this.profile.name().contains("crossbow");
    }

    private int minimumChargeTicks() {
        return 0;
    }

    private float releaseShotPower(int usedTicks) {
        if (this.profile.hasType(RWBYMWeaponProfiles.SANREI)
                || this.profile.hasType(RWBYMWeaponProfiles.LETZT)) {
            float power = usedTicks / 20.0F;
            power = (power * power + power * 2.0F) / 3.0F;
            return Math.min(power, 1.0F);
        }
        return 1.0F;
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

    private int weaponShotDurabilityCost() {
        if (isSpecialMagazineGun() || this.profile.hasType(RWBYMWeaponProfiles.BOW)) {
            return 0;
        }
        int cost = 1;
        if (this.profile.hasType(RWBYMWeaponProfiles.JUNIOR)) {
            cost += 30;
        }
        if (this.profile.hasType(RWBYMWeaponProfiles.INT_MAG)) {
            cost += 4;
        }
        return cost;
    }

    private int cooldown(ItemStack stack) {
        if (isSpecialMagazineGun()) {
            int base = this.profile.hasType(RWBYMWeaponProfiles.BOW) || this.profile.hasType(RWBYMWeaponProfiles.ROCKET)
                    ? 18
                    : this.profile.bulletCount() > 1 ? 12 : 8;
            return Math.max(1, Math.round(base * frameCooldownMultiplier(stack)));
        }
        return this.profile.charges() && !isDualWieldWeapon() ? 5 : 0;
    }

    private float projectileSpeed(ItemStack stack) {
        float speed = this.profile.projectileSpeed() == 0.0F ? 1.0F : this.profile.projectileSpeed();
        return speed * 3.0F * barrelSpeedMultiplier(stack);
    }

    private float projectileSpread(Level level, ItemStack stack, int shotCount) {
        int inaccuracy = shotCount > 2 ? 0 : shotCount;
        return inaccuracy * level.random.nextInt(5) * barrelAccuracy(stack);
    }

    private static int enchantLevel(ItemStack stack, String idPart) {
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

    private static float frameRecoilMultiplier(ItemStack stack) {
        if (hasEnchant(stack, "light_weight_frame")) {
            return 0.5F;
        }
        if (hasEnchant(stack, "attuned_frame")) {
            return 0.7F;
        }
        if (hasEnchant(stack, "precision_frame")) {
            return 0.9F;
        }
        if (hasEnchant(stack, "rapid_fire_frame")) {
            return 1.2F;
        }
        if (hasEnchant(stack, "heavy_weight_frame")) {
            return 1.5F;
        }
        return 1.0F;
    }

    private static float frameCooldownMultiplier(ItemStack stack) {
        if (hasEnchant(stack, "rapid_fire_frame") || hasEnchant(stack, "light_weight_frame")) {
            return 0.75F;
        }
        if (hasEnchant(stack, "heavy_weight_frame")) {
            return 1.25F;
        }
        return 1.0F;
    }

    private static float barrelSpeedMultiplier(ItemStack stack) {
        if (hasEnchant(stack, "chambered_compensator")) {
            return 1.3F;
        }
        if (hasEnchant(stack, "cork_screw") || hasEnchant(stack, "corkscrew")) {
            return 1.2F;
        }
        if (hasEnchant(stack, "extended_barrel")) {
            return 1.8F;
        }
        if (hasEnchant(stack, "fluted")) {
            return 1.2F;
        }
        if (hasEnchant(stack, "full_bore")) {
            return 1.5F;
        }
        if (hasEnchant(stack, "small_bore")) {
            return 1.3F;
        }
        return 1.0F;
    }

    private static float barrelAccuracy(ItemStack stack) {
        if (hasEnchant(stack, "arrow_break")) {
            return 1.2F;
        }
        if (hasEnchant(stack, "chambered_compensator")) {
            return 1.5F;
        }
        if (hasEnchant(stack, "cork_screw") || hasEnchant(stack, "corkscrew")) {
            return 0.4F;
        }
        if (hasEnchant(stack, "extended_barrel")) {
            return 0.5F;
        }
        if (hasEnchant(stack, "fluted") || hasEnchant(stack, "full_bore")
                || hasEnchant(stack, "polygonal")) {
            return 0.9F;
        }
        if (hasEnchant(stack, "home_forged")) {
            return 1.1F;
        }
        if (hasEnchant(stack, "small_bore")) {
            return 0.7F;
        }
        return 1.0F;
    }

    private static boolean isValidTarget(Player player, Entity entity) {
        return entity != player && !entity.isSpectator() && entity.isPickable();
    }

    private static double attackDamage(WeaponProfile profile) {
        double damage = Math.max(1.0D, profile.damage());
        return switch (profile.elementMelee() == null ? "" : profile.elementMelee()) {
            case "gwai2" -> damage * 1.1D;
            case "gwai3" -> damage * 1.25D;
            case "gwai4" -> damage * 1.5D;
            case "gwai5" -> damage * 2.0D;
            default -> damage;
        };
    }

    private static double movementSpeedModifier(WeaponProfile profile) {
        return switch (profile.elementMelee() == null ? "" : profile.elementMelee()) {
            case "gwai2" -> 1.1D;
            case "gwai3" -> 1.15D;
            case "gwai4" -> 1.2D;
            case "gwai5" -> 1.25D;
            case "flash" -> 0.4D;
            case "wind" -> 0.2D;
            default -> 0.0D;
        };
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
