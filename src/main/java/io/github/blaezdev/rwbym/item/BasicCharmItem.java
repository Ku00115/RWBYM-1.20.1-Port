package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BasicCharmItem extends Item {
    private final String charmName;

    public BasicCharmItem(String charmName, Properties properties) {
        super(properties);
        this.charmName = charmName;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player && player.tickCount % 40 == 0) {
            applyCharm(player);
        }
    }

    private void applyCharm(Player player) {
        switch (this.charmName) {
            case "attackcharm" -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false));
            case "auracharm" -> player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(2.0F));
            case "criticalcharm", "edgecharm" ->
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, true, false));
            case "feathercharm" -> player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, true, false));
            case "firedancercharm" ->
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0, true, false));
            case "fleetingcharm" ->
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0, true, false));
            case "healthcharm" -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
            case "knockoutcharm" ->
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
            default -> {
            }
        }
    }

    public long getPerks() {
        return switch (this.charmName) {
            case "attackcharm" -> RWBYMArmorItem.ATTACKBOOST2;
            case "auracharm" -> RWBYMArmorItem.AURAREGEN;
            case "criticalcharm" -> RWBYMArmorItem.CRITICALSTRIKE1;
            case "edgecharm" -> RWBYMArmorItem.GLADIATOR1;
            case "fairyking" -> RWBYMArmorItem.JAVELIN2;
            case "feathercharm" -> RWBYMArmorItem.JUMPBOOST2;
            case "firedancercharm" -> RWBYMArmorItem.FIRESTARTER;
            case "fleetingcharm" -> RWBYMArmorItem.MOVEMENTSPEED1;
            case "healthcharm" -> RWBYMArmorItem.VITALITY2;
            case "kingsgambit" -> RWBYMArmorItem.KINGSGAMBIT;
            case "kingsgambitpawn" -> RWBYMArmorItem.KINGSPAWN;
            case "knockoutcharm" -> RWBYMArmorItem.K01;
            case "maidencharm" -> RWBYMArmorItem.MAIDEN;
            case "puncturecharm" -> RWBYMArmorItem.PUNCTURE1;
            case "reachcharm" -> RWBYMArmorItem.REACH1;
            case "rushcharm" -> RWBYMArmorItem.RUSH1;
            case "tankcharm" -> RWBYMArmorItem.DEFENSE2;
            default -> 0L;
        };
    }
}
