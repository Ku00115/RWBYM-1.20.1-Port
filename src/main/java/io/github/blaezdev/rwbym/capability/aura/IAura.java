package io.github.blaezdev.rwbym.capability.aura;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IAura {
    void tick(Player player);

    float useAura(float usage, boolean overflow);

    void delayRecharge(int ticks);

    float getPercentage();

    void addToMax(float amount);

    int getExpToLevel();

    float getMaxAura();

    void setMaxAura(float amount);

    float getAmount();

    int getDelay();

    void setAmount(float amount);

    void addAmount(float amount);

    void copyFrom(IAura other);

    CompoundTag serialize();

    void deserialize(CompoundTag tag);
}
