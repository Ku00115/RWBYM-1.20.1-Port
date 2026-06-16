package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.registry.RWBYMMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BasicFoodItem extends Item {
    private final String foodName;

    public BasicFoodItem(String foodName, Properties properties) {
        super(properties.food(foodFor(foodName)));
        this.foodName = foodName;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, level, living);
        if (!level.isClientSide() && living instanceof Player player) {
            applyEffects(player);
        }
        return result;
    }

    private void applyEffects(Player player) {
        switch (this.foodName) {
            case "coffee" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1));
            }
            case "coconutmilk" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
            }
            case "pancakes" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 2));
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 2400, 1));
                player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 1200, 0));
            }
            case "fishramen" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 80, 2));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0));
                player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 2400, 0));
            }
            case "bourbon" -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 1));
            case "brandy" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 0));
                player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 2400, 0));
            }
            case "vodka" -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1));
            case "wine" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 0));
            }
            case "sake" -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200, 0));
            }
            case "torchquick" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1));
            }
            default -> {
            }
        }
    }

    private static FoodProperties foodFor(String name) {
        FoodProperties.Builder builder = new FoodProperties.Builder().alwaysEat();
        return switch (name) {
            case "pancakes" -> builder.nutrition(6).saturationMod(0.8F).build();
            case "fishramen" -> builder.nutrition(8).saturationMod(0.9F).build();
            case "coconutmilk" -> builder.nutrition(4).saturationMod(0.4F).build();
            case "coffee", "sake", "torchquick", "bourbon", "brandy", "vodka", "wine" ->
                    builder.nutrition(2).saturationMod(0.2F).build();
            default -> builder.nutrition(4).saturationMod(0.4F).build();
        };
    }
}
