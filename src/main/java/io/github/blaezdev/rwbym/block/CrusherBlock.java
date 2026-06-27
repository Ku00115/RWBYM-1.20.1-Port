package io.github.blaezdev.rwbym.block;

import io.github.blaezdev.rwbym.block.entity.CrusherBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class CrusherBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public CrusherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof CrusherBlockEntity crusher && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, crusher, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(state.getBlock())) {
            Direction facing = state.getValue(FACING);
            Direction opposite = facing.getOpposite();
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Legacy RWBYCrusher flipped away from a solid block immediately in front of it when the rear was open.
            if (isSolidNeighbor(level, pos, facing) && !isSolidNeighbor(level, pos, opposite)) {
                level.setBlock(pos, state.setValue(FACING, opposite), 2);
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrusherBlockEntity crusher) {
                // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
                // Legacy RWBYCrusher spilled its item handler contents before the tile entity disappeared.
                Containers.dropContents(level, pos, crusher);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrusherBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return type == io.github.blaezdev.rwbym.registry.RWBYMBlockEntities.CRUSHER.get()
                ? (tickerLevel, pos, tickerState, blockEntity) ->
                        CrusherBlockEntity.tick(tickerLevel, pos, tickerState, (CrusherBlockEntity) blockEntity)
                : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy RWBYCrusher exposed these usage hints from the block item tooltip.
        tooltip.add(Component.literal("-Chisel Head is used for making cut dust crystals & volatile dust crystals which are stronger.")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("-Crusher Head is used for making dust powder double efficency of the furnace.")
                .withStyle(ChatFormatting.BLUE));
    }

    private static boolean isSolidNeighbor(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        return level.getBlockState(neighborPos).isSolidRender(level, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
}
