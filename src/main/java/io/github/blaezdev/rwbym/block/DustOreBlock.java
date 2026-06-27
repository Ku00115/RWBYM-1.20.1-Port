package io.github.blaezdev.rwbym.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Dust ore block behavior restored from the original RWBYM ore class.
 *
 * <p>Original dust ores dropped dust rocks and, with unsafe mining enabled by default, could
 * explode when mined or powered. Loot quantities stay data-driven in the block loot tables while
 * this class keeps the redstone/mining hazard attached to the ore blocks.</p>
 */
public class DustOreBlock extends Block {
    private static final int DEFAULT_DUST_ORE_EXPLOSION_CHANCE = 20;
    private static final float DUST_ORE_EXPLOSION_POWER = 4.0F;

    public DustOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            maybeExplode(level, pos);
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide()) {
            maybeExplode(level, pos);
        }
    }

    private static void maybeExplode(Level level, BlockPos pos) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy RWBYConfig defaulted unsafe mining on with a 1-in-20 dust ore explosion chance.
        if (level.random.nextInt(DEFAULT_DUST_ORE_EXPLOSION_CHANCE) == 0) {
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), DUST_ORE_EXPLOSION_POWER,
                    Level.ExplosionInteraction.BLOCK);
        }
    }
}
