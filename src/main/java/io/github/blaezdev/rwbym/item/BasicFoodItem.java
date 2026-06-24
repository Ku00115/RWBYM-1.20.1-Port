package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.registry.RWBYMMobEffects;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BasicFoodItem extends Item {
    private final String foodName;

    public BasicFoodItem(String foodName, Properties properties) {
        super(properties.food(foodFor(foodName)));
        this.foodName = foodName;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (isDurableFood(this.foodName)) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            if (living instanceof Player player) {
                FoodProperties food = getFoodProperties();
                player.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
                player.awardStat(Stats.ITEM_USED.get(this));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP,
                        SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
                if (!level.isClientSide()) {
                    applyEffects(player);
                    InteractionHand hand = player.getUsedItemHand();
                    stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
                }
            }
            return stack;
        }
        ItemStack result = super.finishUsingItem(stack, level, living);
        if (!level.isClientSide() && living instanceof Player player) {
            applyEffects(player);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("-").append(Component.translatable(tooltipKey(this.foodName)))
                .withStyle(ChatFormatting.BLUE));
    }

    private void applyEffects(Player player) {
        switch (this.foodName) {
            case "hchoc" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 1200, 0));
            }
            case "coffee" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1));
            }
            case "sunrise" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 2));
            }
            case "plg" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 3));
                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 20, 0));
            }
            case "qrowflask" -> {
                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 0));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 3600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 5, 0));
            }
            case "coconutmilk" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 2));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 1200, 2));
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 3));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 2));
            }
            case "pancakes" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 5));
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 2400, 5));
                player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 1200, 0));
            }
            case "fishramen" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 5));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 5));
                player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 2400, 0));
            }
            case "ramen" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 5));
                player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 2400, 0));
            }
            case "peach" -> player.heal(2.5F);
            case "bourbon" -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2));
            case "brandy" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1));
                player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 2400, 0));
            }
            case "vodka" -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 2));
            case "wine" -> {
                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 0));
            }
            case "sake" -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 3));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200, 2));
            }
            case "torchquick" -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 2));
            }
            default -> {
            }
        }
    }

    private static FoodProperties foodFor(String name) {
        FoodProperties.Builder builder = new FoodProperties.Builder().alwaysEat();
        return switch (name) {
            case "hchoc", "sunrise", "plg" -> builder.nutrition(4).saturationMod(0.4F).build();
            case "pancakes" -> builder.nutrition(6).saturationMod(0.8F).build();
            case "fishramen" -> builder.nutrition(8).saturationMod(0.9F).build();
            case "ramen" -> builder.nutrition(7).saturationMod(0.8F).build();
            case "peach" -> builder.nutrition(3).saturationMod(0.3F).build();
            case "coconutmilk" -> builder.nutrition(4).saturationMod(0.4F).build();
            case "coffee", "qrowflask", "sake", "torchquick", "bourbon", "brandy", "vodka", "wine" ->
                    builder.nutrition(2).saturationMod(0.2F).build();
            default -> builder.nutrition(4).saturationMod(0.4F).build();
        };
    }

    public static boolean isDurableFood(String name) {
        return name.equals("coffee")
                || name.equals("qrowflask")
                || name.equals("sake")
                || name.equals("pancakes")
                || name.equals("vodka");
    }

    public static int legacyStackLimit(String name) {
        if (isDurableFood(name)) {
            return 1;
        }
        if (name.equals("fishramen") || name.equals("ramen") || name.equals("peach")) {
            return 64;
        }
        return 2;
    }

    private static String tooltipKey(String name) {
        return switch (name) {
            case "hchoc" -> "ui.foodhotchocolate";
            case "sunrise" -> "ui.foodstrawberrysunrise";
            case "plg" -> "ui.foodgrapesoda";
            case "qrowflask" -> "ui.foodqrowsflask";
            default -> "ui.food" + name;
        };
    }
}
