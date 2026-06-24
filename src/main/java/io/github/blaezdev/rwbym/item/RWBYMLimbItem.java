package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class RWBYMLimbItem extends Item {
    public static final String DATA_KEY = RWBYM.MOD_ID + "_appearance";

    private final String name;
    private final String slot;
    private final String registryName;

    public RWBYMLimbItem(String name, String slot, Properties properties) {
        super(properties);
        this.name = name;
        this.slot = slot;
        this.registryName = new ResourceLocation(RWBYM.MOD_ID, name).toString();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        applyAppearance(entity, this.slot, this.name.startsWith("clear") ? "" : this.registryName);
        if (!level.isClientSide()) {
            RWBYMNetwork.syncAppearance(entity);
            if (!(entity instanceof Player player) || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public static void applyAppearance(LivingEntity entity, String slot, String itemId) {
        CompoundTag data = entity.getPersistentData();
        CompoundTag appearance = data.getCompound(DATA_KEY);
        appearance.putString(slot, itemId);
        data.put(DATA_KEY, appearance);
    }

    public static String getAppearance(LivingEntity entity, String slot) {
        return entity.getPersistentData().getCompound(DATA_KEY).getString(slot);
    }
}
