package io.github.blaezdev.rwbym.item;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Durable processor head used by the Crusher recipe slot.
 *
 * <p>Original RWBYM defined {@code chisel} and {@code crush} as container items that return
 * a damaged copy after machine or recipe use. This item keeps that stack-sensitive Forge
 * behavior available in the 1.20.1 port.</p>
 */
public class RWBYMProcessingToolItem extends Item {
    public RWBYMProcessingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remaining = stack.copy();
        remaining.setCount(1);
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy RWBYItem's container lambda damaged the processing head and returned empty when it broke.
        return remaining.hurt(1, RandomSource.create(), null) ? ItemStack.EMPTY : remaining;
    }
}
