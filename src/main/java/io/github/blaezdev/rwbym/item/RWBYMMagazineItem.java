package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.RWBYM;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class RWBYMMagazineItem extends Item {
    public static final String AMMO_COUNT = "AmmoCount";
    private final String ammoId;
    private final int capacity;

    public RWBYMMagazineItem(String ammoId, int capacity, Properties properties) {
        super(properties.stacksTo(1));
        this.ammoId = ammoId;
        this.capacity = capacity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                unloadOneRound(stack, player);
            } else {
                loadOneRound(stack, player);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Ammo: " + getAmmoCount(stack) + " / " + this.capacity)
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public int getCapacity() {
        return this.capacity;
    }

    public String getAmmoId() {
        return this.ammoId;
    }

    public static int getAmmoCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(AMMO_COUNT)) {
            return stack.getItem() instanceof RWBYMMagazineItem magazine ? magazine.capacity : 0;
        }
        return tag.getInt(AMMO_COUNT);
    }

    public static void setAmmoCount(ItemStack stack, int count) {
        if (stack.getItem() instanceof RWBYMMagazineItem magazine) {
            stack.getOrCreateTag().putInt(AMMO_COUNT, Math.max(0, Math.min(magazine.capacity, count)));
        }
    }

    private void loadOneRound(ItemStack stack, Player player) {
        int current = getAmmoCount(stack);
        if (current >= this.capacity || !consumeOneAmmo(player)) {
            return;
        }
        setAmmoCount(stack, current + 1);
    }

    private void unloadOneRound(ItemStack stack, Player player) {
        int current = getAmmoCount(stack);
        if (current <= 0) {
            return;
        }
        Item ammo = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.ammoId));
        if (ammo != null) {
            player.getInventory().placeItemBackInInventory(new ItemStack(ammo));
        }
        setAmmoCount(stack, current - 1);
    }

    private boolean consumeOneAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(candidate.getItem());
            if (id != null && id.getNamespace().equals(RWBYM.MOD_ID) && id.toString().equals(this.ammoId)) {
                candidate.shrink(1);
                return true;
            }
        }
        return false;
    }
}
