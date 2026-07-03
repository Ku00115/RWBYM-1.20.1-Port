package io.github.blaezdev.rwbym.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

/**
 * Restores RWBYM's hardlight fence behavior from the original RWBYBlockFence.
 *
 * <p>The legacy block placed a two-block post and auto-filled one-block gaps between nearby posts
 * with non-post fence panels. This class keeps that state model while using modern Forge block
 * state and damage APIs.</p>
 */
public class HardlightFenceBlock extends FenceBlock {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty ON = BooleanProperty.create("on");
    public static final BooleanProperty POST = BooleanProperty.create("post");

    public HardlightFenceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(TOP, false)
                .setValue(ON, false)
                .setValue(POST, true));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (!context.getLevel().getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return postState(false);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || oldState.is(this) || !state.getValue(POST) || state.getValue(TOP)) {
            return;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The original fence created its upper post and bridge panels when the bottom post was placed.
        if (canReplaceFencePart(level.getBlockState(pos.above()))) {
            level.setBlock(pos.above(), postState(true), Block.UPDATE_ALL);
        }
        connectNearbyPosts(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && state.getValue(POST)) {
            removePairedPost(level, pos, state.getValue(TOP));
            removeAdjacentPanels(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide() && entity instanceof Monster) {
            entity.hurt(level.damageSources().cactus(), 5.0F);
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!state.getValue(POST)) {
            return -1.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (!state.getValue(POST)) {
            return Collections.emptyList();
        }
        return super.getDrops(state, builder);
    }

    private void connectNearbyPosts(Level level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos remotePost = pos.relative(direction, 2);
            if (isPost(level.getBlockState(remotePost), false)) {
                placePanel(level, pos.relative(direction), direction, false);
                placePanel(level, pos.relative(direction).above(), direction, true);
            }
        }
    }

    private void placePanel(Level level, BlockPos pos, Direction direction, boolean top) {
        if (canReplaceFencePart(level.getBlockState(pos))) {
            level.setBlock(pos, panelState(direction, top), Block.UPDATE_ALL);
        }
    }

    private void removePairedPost(Level level, BlockPos pos, boolean top) {
        BlockPos pairedPos = top ? pos.below() : pos.above();
        BlockState pairedState = level.getBlockState(pairedPos);
        if (pairedState.is(this) && pairedState.getValue(POST)) {
            level.removeBlock(pairedPos, false);
        }
    }

    private void removeAdjacentPanels(Level level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos panelPos = pos.relative(direction);
            BlockState panelState = level.getBlockState(panelPos);
            if (panelState.is(this) && !panelState.getValue(POST) && shouldRemovePanelAfterPostBreak(level, panelPos, direction)) {
                level.removeBlock(panelPos, false);
            }
        }
    }

    private boolean shouldRemovePanelAfterPostBreak(Level level, BlockPos panelPos, Direction brokenPostDirection) {
        Direction sideA = brokenPostDirection.getClockWise();
        Direction sideB = brokenPostDirection.getCounterClockWise();
        // Original breakBlock kept crossing panels when both perpendicular neighbor panels still framed the segment.
        return !(level.getBlockState(panelPos.relative(sideA)).is(this)
                && level.getBlockState(panelPos.relative(sideB)).is(this));
    }

    private BlockState postState(boolean top) {
        return this.defaultBlockState()
                .setValue(TOP, top)
                .setValue(ON, false)
                .setValue(POST, true)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(WATERLOGGED, false);
    }

    private BlockState panelState(Direction direction, boolean top) {
        return this.defaultBlockState()
                .setValue(TOP, top)
                .setValue(ON, false)
                .setValue(POST, false)
                .setValue(NORTH, direction == Direction.NORTH || direction == Direction.SOUTH)
                .setValue(SOUTH, direction == Direction.NORTH || direction == Direction.SOUTH)
                .setValue(EAST, direction == Direction.EAST || direction == Direction.WEST)
                .setValue(WEST, direction == Direction.EAST || direction == Direction.WEST)
                .setValue(WATERLOGGED, false);
    }

    private boolean isPost(BlockState state, boolean top) {
        return state.is(this) && state.getValue(POST) && state.getValue(TOP) == top;
    }

    private boolean canReplaceFencePart(BlockState state) {
        // Original RWBYBlockFence used isReplaceable, so plants/snow-like blocks should not block auto panels.
        return state.canBeReplaced() || state.is(this) && !state.getValue(POST);
    }

    @Override
    public boolean connectsTo(BlockState state, boolean sideSolid, Direction direction) {
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (!direction.getAxis().isHorizontal() || !updated.is(this)) {
            return updated;
        }
        boolean connected = legacyConnects(level, pos, direction);
        Property<Boolean> property = propertyFor(direction);
        return property == null ? updated : updated.setValue(property, connected);
    }

    private boolean legacyConnects(BlockGetter level, BlockPos pos, Direction facing) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // RWBYBlockFence connected panels through nearby hardlight posts instead of vanilla solid-side checks.
        Direction opposite = facing.getOpposite();
        BlockPos anchor = pos.relative(opposite);
        boolean currentIsPost = isPost(level.getBlockState(anchor));
        boolean nextIsPost = isPost(level.getBlockState(anchor.relative(facing)));
        boolean twoAwayIsPost = isPost(level.getBlockState(anchor.relative(facing, 2)));
        boolean behindIsPost = isPost(level.getBlockState(anchor.relative(opposite)));
        boolean nextIsPanel = isPanel(level.getBlockState(anchor.relative(facing)));
        return currentIsPost ? nextIsPost || twoAwayIsPost && nextIsPanel : nextIsPost && behindIsPost;
    }

    private boolean isPost(BlockState state) {
        return state.is(this) && state.getValue(POST);
    }

    private boolean isPanel(BlockState state) {
        return state.is(this) && !state.getValue(POST);
    }

    @Nullable
    private static Property<Boolean> propertyFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> null;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TOP, ON, POST);
    }
}
