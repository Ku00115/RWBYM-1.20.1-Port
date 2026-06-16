package io.github.blaezdev.rwbym.block;

import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.block.entity.CrusherBlockEntity;
import io.github.blaezdev.rwbym.registry.RWBYMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;
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
            return InteractionResult.SUCCESS;
        }
        return switch (this.name) {
            case "toolkit" -> repairHeldItem(level, pos, player, hand);
            case "bait" -> spawnGrimm(level, pos, player);
            case "crusher", "crush" -> openCrusher(level, pos, player);
            default -> InteractionResult.PASS;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return "crusher".equals(this.name) || "crush".equals(this.name) ? new CrusherBlockEntity(pos, state) : null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return type == RWBYMBlockEntities.CRUSHER.get()
                ? (tickerLevel, pos, tickerState, blockEntity) ->
                        CrusherBlockEntity.tick(tickerLevel, pos, tickerState, (CrusherBlockEntity) blockEntity)
                : null;
    }

    private InteractionResult repairHeldItem(Level level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !stack.isDamaged() || player.experienceLevel < 5) {
            player.displayClientMessage(Component.translatable("message.rwbym.toolkit.requirement"), true);
            return InteractionResult.CONSUME;
        }
        stack.setDamageValue(0);
        player.giveExperienceLevels(-5);
        level.destroyBlock(pos, false);
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.1F);
        return InteractionResult.CONSUME;
    }

    private InteractionResult spawnGrimm(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || level.getDifficulty().getId() == 0) {
            return InteractionResult.CONSUME;
        }
        EntityType<?> type = randomGrimm(level);
        BlockPos spawnPos = pos.offset(level.random.nextInt(7) - 3, 1, level.random.nextInt(7) - 3);
        type.spawn(serverLevel, spawnPos, MobSpawnType.TRIGGERED);
        level.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 0.9F, 0.65F);
        player.getCooldowns().addCooldown(this.asItem(), 80);
        return InteractionResult.CONSUME;
    }

    private InteractionResult openCrusher(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof CrusherBlockEntity crusher && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, crusher, pos);
        }
        return InteractionResult.CONSUME;
    }

    private EntityType<?> randomGrimm(Level level) {
        @SuppressWarnings("unchecked")
        RegistryObject<? extends EntityType<?>>[] choices = new RegistryObject[] {
                RWBYMEntityTypes.BEOWOLF, RWBYMEntityTypes.URSA, RWBYMEntityTypes.BOARBATUSK,
                RWBYMEntityTypes.CREEP, RWBYMEntityTypes.SABYR, RWBYMEntityTypes.BERINGLE,
                RWBYMEntityTypes.APATHY, RWBYMEntityTypes.DEATHSTALKER, RWBYMEntityTypes.LANCER
        };
        return choices[level.random.nextInt(choices.length)].get();
    }
}
