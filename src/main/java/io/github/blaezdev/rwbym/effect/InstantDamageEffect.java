package io.github.blaezdev.rwbym.effect;

import io.github.blaezdev.rwbym.entity.RagoraEntity;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

public class InstantDamageEffect extends InstantenousMobEffect {
    public InstantDamageEffect() {
        super(MobEffectCategory.HARMFUL, 0x1A001A);
    }

    @Override
    public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier,
            double health) {
        if (indirectSource != null && livingEntity == indirectSource) {
            return;
        }
        if (indirectSource instanceof RagoraEntity ragora && livingEntity == ragora.getOwner()) {
            // Original PotionDamage skipped Ragora's owner so its Dragon Breath clouds stay summon-friendly.
            return;
        }
        DamageSource damageSource = source != null
                ? livingEntity.damageSources().indirectMagic(source, indirectSource != null ? indirectSource : source)
                : livingEntity.damageSources().magic();
        livingEntity.hurt(damageSource, Math.max(1.0F, amplifier));
    }
}
