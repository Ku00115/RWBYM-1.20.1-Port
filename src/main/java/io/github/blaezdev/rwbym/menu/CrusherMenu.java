package io.github.blaezdev.rwbym.menu;

import io.github.blaezdev.rwbym.block.entity.CrusherBlockEntity;
import io.github.blaezdev.rwbym.registry.RWBYMMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrusherMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public CrusherMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(4), new SimpleContainerData(4));
    }

    public CrusherMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(RWBYMMenuTypes.CRUSHER.get(), id);
        this.container = container;
        this.data = data;
        checkContainerSize(container, 4);
        checkContainerDataCount(data, 4);

        addSlot(new Slot(container, CrusherBlockEntity.INPUT_SLOT, 26, 11));
        addSlot(new Slot(container, CrusherBlockEntity.TOOL_SLOT, 26, 59) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(CrusherBlockEntity.TOOL_SLOT, stack);
            }
        });
        addSlot(new Slot(container, CrusherBlockEntity.FUEL_SLOT, 7, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(CrusherBlockEntity.FUEL_SLOT, stack);
            }
        });
        addSlot(new Slot(container, CrusherBlockEntity.OUTPUT_SLOT, 81, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    public int burnProgress(int pixels) {
        int burn = this.data.get(0);
        int maxBurn = this.data.get(1);
        if (maxBurn == 0) {
            maxBurn = 200;
        }
        return burn * pixels / maxBurn;
    }

    public int cookProgress(int pixels) {
        int cook = this.data.get(2);
        int maxCook = this.data.get(3);
        return maxCook != 0 && cook != 0 ? cook * pixels / maxCook : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index == CrusherBlockEntity.OUTPUT_SLOT) {
                if (!moveItemStackTo(stack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, copy);
            } else if (index >= 4) {
                if (this.container.canPlaceItem(CrusherBlockEntity.FUEL_SLOT, stack)) {
                    if (!moveItemStackTo(stack, CrusherBlockEntity.FUEL_SLOT, CrusherBlockEntity.FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.container.canPlaceItem(CrusherBlockEntity.TOOL_SLOT, stack)) {
                    if (!moveItemStackTo(stack, CrusherBlockEntity.TOOL_SLOT, CrusherBlockEntity.TOOL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, CrusherBlockEntity.INPUT_SLOT, CrusherBlockEntity.INPUT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 4, 40, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // CrusherBlockEntity mirrors the legacy distance-and-tile check; NULL access would skip that world position.
        return this.container.stillValid(player);
    }
}
