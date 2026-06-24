package io.github.blaezdev.rwbym.capability.aura;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuraProvider implements ICapabilitySerializable<CompoundTag> {
    private final IAura aura = new Aura();
    private final LazyOptional<IAura> optional = LazyOptional.of(() -> this.aura);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        return capability == RWBYMCapabilities.AURA ? this.optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.aura.serialize();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.aura.deserialize(tag);
    }

    public void invalidate() {
        this.optional.invalidate();
    }
}
