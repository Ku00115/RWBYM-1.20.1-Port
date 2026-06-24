package io.github.blaezdev.rwbym.fluid;

import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.List;
import java.util.function.Supplier;

public abstract class GrimmFluid extends ForgeFlowingFluid {
    private static final int GRIMM_FLUID_SPAWN_RATE = 50000;
    private static final List<Supplier<? extends net.minecraft.world.entity.EntityType<? extends Mob>>> SPAWN_POOL = List.of(
            RWBYMEntityTypes.BOARBATUSK,
            RWBYMEntityTypes.BEOWOLF,
            RWBYMEntityTypes.URSA,
            RWBYMEntityTypes.LANCER,
            RWBYMEntityTypes.GEIST,
            RWBYMEntityTypes.APATHY,
            RWBYMEntityTypes.CREEP,
            RWBYMEntityTypes.TINY_DEATHSTALKER,
            RWBYMEntityTypes.NEVERMORE);

    protected GrimmFluid(Properties properties) {
        super(properties);
    }

    public static void onEntityInside(Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity instanceof Monster) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 3, false, false));
        living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2, false, false));
    }

    public static void randomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random) {
        if (!state.isSource() || GRIMM_FLUID_SPAWN_RATE <= 0) {
            return;
        }
        if (random.nextInt(GRIMM_FLUID_SPAWN_RATE) != 0) {
            return;
        }
        Mob grimm = SPAWN_POOL.get(random.nextInt(SPAWN_POOL.size())).get().create(level);
        if (grimm == null) {
            return;
        }
        grimm.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(grimm) || !level.getBlockState(pos.above()).canBeReplaced()) {
            return;
        }
        level.addFreshEntity(grimm);
    }

    public static final class Source extends GrimmFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends GrimmFluid {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.material.Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
