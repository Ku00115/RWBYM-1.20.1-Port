package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.entity.BlakeSummonEntity;
import io.github.blaezdev.rwbym.registry.RWBYMEnchantments;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMWeaponModifierHelper {
    private static final MobEffectInstance[] LUCKY_HIT_EFFECTS = new MobEffectInstance[] {
            new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 1, true, false),
            new MobEffectInstance(MobEffects.DIG_SPEED, 600, 1, true, false),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 1, true, false),
            new MobEffectInstance(MobEffects.JUMP, 600, 1, true, false),
            new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 1, true, false),
            new MobEffectInstance(MobEffects.ABSORPTION, 600, 1, true, false),
            new MobEffectInstance(MobEffects.HEALTH_BOOST, 600, 1, true, false),
            new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1, true, false),
            new MobEffectInstance(MobEffects.REGENERATION, 600, 1, true, false),
            new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1, true, false),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1, true, false)
    };
    private static final List<String> SHOT_MODIFIERS = List.of(
            "double_shot",
            "knock_shot",
            "poison_shot",
            "flare_frost_shot");
    private static final List<String> KILL_MODIFIERS = List.of(
            "aura_siphon",
            "scavenger",
            "lucky_hit",
            "trickster");
    private static final List<String> FRAME_MODIFIERS = List.of(
            "attuned_frame",
            "precision_frame",
            "heavy_weight_frame",
            "light_weight_frame",
            "rapid_fire_frame");
    private static final List<String> BARREL_MODIFIERS = List.of(
            "arrow_break_barrel",
            "chambered_compensator_barrel",
            "cork_screw_rifling",
            "extended_barrel",
            "fluted_barrel",
            "full_bore_barrel",
            "home_forged_rifling",
            "polygonal_rifling",
            "small_bore_barrel");

    public static ItemStack createGeneratedWeaponStack(ItemStack source, RandomSource random) {
        if (!supportsDefaultModifiers(source)) {
            return source.copy();
        }
        ItemStack generated = new ItemStack(source.getItem());
        initializeLegacyWeaponData(generated, source.getTag());
        applyDefaultModifiersIfMissing(generated, random);
        return generated;
    }

    public static void applyDefaultModifiersIfMissing(ItemStack stack, RandomSource random) {
        if (!supportsDefaultModifiers(stack)) {
            return;
        }
        initializeLegacyWeaponData(stack, stack.getTag());
        if (!EnchantmentHelper.getEnchantments(stack).isEmpty()) {
            return;
        }
        enchantRandomModifier(stack, BARREL_MODIFIERS, random);
        enchantRandomModifier(stack, FRAME_MODIFIERS, random);
        enchantRandomModifier(stack, SHOT_MODIFIERS, random);
        enchantRandomModifier(stack, KILL_MODIFIERS, random);
    }

    public static boolean supportsDefaultModifiers(ItemStack stack) {
        return stack.getItem() instanceof RWBYMWeaponItem || stack.getItem() instanceof BasicGunItem;
    }

    public static void applyKillModifierEffects(ItemStack stack, Player player, LivingEntity target) {
        if (!target.isDeadOrDying()) {
            return;
        }
        if (hasEnchant(stack, "aura_siphon")) {
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(10.0F));
        }
        if (hasEnchant(stack, "trickster") && player.getRandom().nextInt(16) < 4) {
            BlakeSummonEntity blake = RWBYMEntityTypes.BLAKE.get().create(target.level());
            if (blake != null) {
                blake.setOwner(player);
                blake.moveTo(target.getX(), target.getY(), target.getZ(), 0.0F, 0.0F);
                target.level().addFreshEntity(blake);
            }
        }
        if (hasEnchant(stack, "lucky_hit") && player.getRandom().nextInt(32) < 4) {
            player.addEffect(LUCKY_HIT_EFFECTS[player.getRandom().nextInt(LUCKY_HIT_EFFECTS.length)]);
        }
        if (hasEnchant(stack, "scavenger") && player.getRandom().nextInt(32) < 4) {
            if (stack.getItem() instanceof RWBYMWeaponItem weaponItem) {
                weaponItem.restoreScavengerAmmo(stack, player);
            } else if (stack.getItem() instanceof BasicGunItem) {
                BasicGunItem.restoreScavengerAmmo(player);
            }
        }
    }

    private static void initializeLegacyWeaponData(ItemStack stack, CompoundTag sourceTag) {
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals("ozpincane")) {
            return;
        }
        CompoundTag tag = sourceTag == null ? new CompoundTag() : sourceTag.copy();
        if (!tag.contains("Aura")) {
            tag.putInt("Aura", 1);
        }
        boolean auraOn = tag.getBoolean("AuraOn") || tag.getBoolean("AuraON");
        tag.putBoolean("AuraOn", auraOn);
        tag.putBoolean("AuraON", auraOn);
        stack.setTag(tag);
    }

    private static void enchantRandomModifier(ItemStack stack, List<String> modifierIds, RandomSource random) {
        RegistryObject<Enchantment> enchantment = RWBYMEnchantments.WEAPON_MODIFIERS
                .get(modifierIds.get(random.nextInt(modifierIds.size())));
        if (enchantment != null) {
            stack.enchant(enchantment.get(), 1);
        }
    }

    private static boolean hasEnchant(ItemStack stack, String idPart) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            var id = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey());
            if (id != null && id.getPath().contains(idPart)) {
                return true;
            }
        }
        return false;
    }

    private RWBYMWeaponModifierHelper() {
    }
}
