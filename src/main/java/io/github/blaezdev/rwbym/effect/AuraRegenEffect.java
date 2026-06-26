package io.github.blaezdev.rwbym.effect;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AuraRegenEffect extends MobEffect {
    public AuraRegenEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x66D9FF);
    }

    @Override
    public void applyEffectTick(LivingEntity living, int amplifier) {
        if (living instanceof Player player) {
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> {
                if (aura.getAmount() < aura.getMaxAura() && aura.getDelay() < 45) {
                    // Original PotionAuraRegen only bypassed the tail end of the Aura recharge delay.
                    aura.addAmount(0.35F * Math.max(1, amplifier));
                }
            });
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = 50 >> amplifier;
        return interval > 0 ? duration % interval == 0 : true;
    }
}
