package io.github.blaezdev.rwbym.block;

import io.github.blaezdev.rwbym.fluid.GrimmFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class GrimmFluidBlock extends LiquidBlock {
    public GrimmFluidBlock(Supplier<? extends FlowingFluid> fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties.randomTicks());
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        GrimmFluid.onEntityInside(level, pos, entity);
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        GrimmFluid.randomTick(level, pos, state.getFluidState(), random);
        super.randomTick(state, level, pos, random);
    }
}
