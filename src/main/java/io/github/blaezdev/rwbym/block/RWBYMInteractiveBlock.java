package io.github.blaezdev.rwbym.block;

import io.github.blaezdev.rwbym.block.entity.CrusherBlockEntity;
import io.github.blaezdev.rwbym.block.entity.GrimmBaitBlockEntity;
import io.github.blaezdev.rwbym.registry.RWBYMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class RWBYMInteractiveBlock extends Block implements EntityBlock {
    private final String name;

    public RWBYMInteractiveBlock(String name, Properties properties) {
        super(properties);
        this.name = name;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide()) {
            if ("bait".equals(this.name) && level.getDifficulty().getId() == 0) {
                return InteractionResult.PASS;
            }
            if ("toolkit".equals(this.name)) {
                ItemStack stack = player.getMainHandItem();
                return !stack.isEmpty() && stack.isDamaged() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }
        return switch (this.name) {
            case "toolkit" -> repairHeldItem(level, pos, player);
            case "bait" -> activateBait(level, pos, player);
            case "crusher" -> openCrusher(level, pos, player);
            default -> InteractionResult.PASS;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if ("crusher".equals(this.name)) {
            return new CrusherBlockEntity(pos, state);
        }
        if ("bait".equals(this.name)) {
            return new GrimmBaitBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type == RWBYMBlockEntities.CRUSHER.get()) {
            return (tickerLevel, pos, tickerState, blockEntity) ->
                    CrusherBlockEntity.tick(tickerLevel, pos, tickerState, (CrusherBlockEntity) blockEntity);
        }
        if (type == RWBYMBlockEntities.GRIMM_BAIT.get()) {
            return (tickerLevel, pos, tickerState, blockEntity) ->
                    GrimmBaitBlockEntity.tick(tickerLevel, pos, tickerState, (GrimmBaitBlockEntity) blockEntity);
        }
        return null;
    }

    private InteractionResult repairHeldItem(Level level, BlockPos pos, Player player) {
        // Original RWBYToolkit repaired the selected hotbar item rather than the offhand interaction stack.
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !stack.isDamaged()) {
            return InteractionResult.PASS;
        }
        stack.setDamageValue(0);
        // Original RWBYToolkit always charged five levels after a successful repair.
        player.giveExperienceLevels(-5);
        level.destroyBlock(pos, false);
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.1F);
        return InteractionResult.CONSUME;
    }

    private InteractionResult activateBait(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel) || level.getDifficulty().getId() == 0) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(pos) instanceof GrimmBaitBlockEntity bait && player instanceof ServerPlayer serverPlayer) {
            bait.activate(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    private InteractionResult openCrusher(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof CrusherBlockEntity crusher && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, crusher, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return "bait".equals(this.name) || super.useShapeForLightOcclusion(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
