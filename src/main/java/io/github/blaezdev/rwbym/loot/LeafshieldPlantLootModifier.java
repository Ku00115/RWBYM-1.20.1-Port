package io.github.blaezdev.rwbym.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class LeafshieldPlantLootModifier extends LootModifier {
    public static final Codec<LeafshieldPlantLootModifier> CODEC = RecordCodecBuilder.create(
            instance -> codecStart(instance).apply(instance, LeafshieldPlantLootModifier::new));

    public LeafshieldPlantLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (!(entity instanceof Player player)
                || !(state.getBlock() instanceof BushBlock)
                || (!player.getMainHandItem().is(RWBYMItems.SIMPLE_ITEMS.get("leafshield").get())
                        && !player.getOffhandItem().is(RWBYMItems.SIMPLE_ITEMS.get("leafshield").get()))) {
            return generatedLoot;
        }
        generatedLoot.addAll(new ObjectArrayList<>(generatedLoot));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
