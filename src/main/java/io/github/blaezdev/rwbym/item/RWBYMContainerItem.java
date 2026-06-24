package io.github.blaezdev.rwbym.item;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.MenuProvider;

/**
 * AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source.
 * Restores RWBYM's portable wallet/pouch/container items with per-stack NBT storage.
 */
public class RWBYMContainerItem extends Item {
    private static final String ITEMS_TAG = "Items";

    private final int slots;
    private final Set<String> acceptedItems;

    public RWBYMContainerItem(int slots, String acceptedItems, Properties properties) {
        super(properties.stacksTo(1));
        this.slots = slots;
        this.acceptedItems = Stream.of(acceptedItems.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player,
                    new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return stack.getHoverName();
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                            StoredContainer container = new StoredContainer(stack, slots, acceptedItems);
                            return slots > 9
                                    ? new ChestMenu(MenuType.GENERIC_9x6, containerId, inventory, container, 6)
                                    : new ChestMenu(MenuType.GENERIC_9x1, containerId, inventory, container, 1);
                        }
                    });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static final class StoredContainer extends SimpleContainer {
        private final ItemStack owner;
        private final Set<String> acceptedItems;

        private StoredContainer(ItemStack owner, int slots, Set<String> acceptedItems) {
            super(slots);
            this.owner = owner;
            this.acceptedItems = acceptedItems;
            NonNullList<ItemStack> contents = NonNullList.withSize(slots, ItemStack.EMPTY);
            CompoundTag tag = owner.getOrCreateTag();
            net.minecraft.world.ContainerHelper.loadAllItems(tag.getCompound(ITEMS_TAG), contents);
            for (int i = 0; i < contents.size(); i++) {
                this.setItem(i, contents.get(i));
            }
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            // Original RWBYItemStackHandler filtered by registry id strings supplied to RWBYContainerItem.
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return id != null && this.acceptedItems.contains(id.toString());
        }

        @Override
        public void setChanged() {
            super.setChanged();
            NonNullList<ItemStack> contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            for (int i = 0; i < contents.size(); i++) {
                contents.set(i, this.getItem(i));
            }
            CompoundTag itemsTag = new CompoundTag();
            net.minecraft.world.ContainerHelper.saveAllItems(itemsTag, contents);
            this.owner.getOrCreateTag().put(ITEMS_TAG, itemsTag);
        }
    }
}
