package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.fluid.GrimmFluid;
import net.minecraft.world.level.material.FlowingFluid;
import io.github.blaezdev.rwbym.fluid.GrimmFluidType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, RWBYM.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, RWBYM.MOD_ID);

    public static final RegistryObject<FluidType> GRIMM_TYPE = FLUID_TYPES.register("grimm_type",
            () -> new GrimmFluidType());

    public static final RegistryObject<FlowingFluid> GRIMM = FLUIDS.register("grimm",
            () -> new GrimmFluid.Source(grimmProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_GRIMM = FLUIDS.register("flowing_grimm",
            () -> new GrimmFluid.Flowing(grimmProperties()));

    private static ForgeFlowingFluid.Properties grimmProperties() {
        return new ForgeFlowingFluid.Properties(GRIMM_TYPE, GRIMM, FLOWING_GRIMM)
                .bucket(RWBYMItems.GRIMM_BUCKET)
                .block(RWBYMBlocks.GRIMM_FLUID_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(20)
                .explosionResistance(100.0F);
    }

    private RWBYMFluids() {
    }
}
