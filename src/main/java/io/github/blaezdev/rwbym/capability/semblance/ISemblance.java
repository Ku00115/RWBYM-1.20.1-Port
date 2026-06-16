package io.github.blaezdev.rwbym.capability.semblance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface ISemblance {
    void tick(Player player);

    String getName();

    void setName(String name);

    boolean isActive();

    void setActive(boolean active);

    int getLevel();

    void setLevel(int level);

    void copyFrom(ISemblance other);

    CompoundTag serialize();

    void deserialize(CompoundTag tag);
}
