package io.github.blaezdev.rwbym.item;

import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Simple item wrapper for legacy RWBYM materials that can be used as furnace fuel.
 */
public class RWBYMFuelItem extends Item {
    private final int burnTime;

    public RWBYMFuelItem(int burnTime, Properties properties) {
        super(properties);
        this.burnTime = burnTime;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTime;
    }
}
