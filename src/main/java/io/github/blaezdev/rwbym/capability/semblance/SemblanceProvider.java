package io.github.blaezdev.rwbym.capability.semblance;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SemblanceProvider implements ICapabilitySerializable<CompoundTag> {
    private final ISemblance instance = new Semblance();
    private final LazyOptional<ISemblance> optional = LazyOptional.of(() -> this.instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        return capability == RWBYMCapabilities.SEMBLANCE ? this.optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.instance.serialize();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.instance.deserialize(tag);
    }
}
