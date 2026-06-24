package io.github.blaezdev.rwbym.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source.
 * Restores RWBYM's legacy melee reach helper used by Grimm, Atlas Knight, and Weiss summons.
 */
public class RangedMeleeAttackGoal extends MeleeAttackGoal {
    private final PathfinderMob mob;
    private final float extraRange;

    public RangedMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen,
            float extraRange) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.mob = mob;
        this.extraRange = extraRange;
    }

    @Override
    protected double getAttackReachSqr(LivingEntity target) {
        // Match the 1.12 helper formula so large custom models keep their intended hit distance.
        double reach = this.mob.getBbWidth() + target.getBbWidth() + this.extraRange;
        return reach * reach;
    }
}
