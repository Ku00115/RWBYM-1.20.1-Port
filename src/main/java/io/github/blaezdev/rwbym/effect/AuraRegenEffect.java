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
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(1.0F + amplifier));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = Math.max(10, 40 >> amplifier);
        return duration % interval == 0;
    }
}
