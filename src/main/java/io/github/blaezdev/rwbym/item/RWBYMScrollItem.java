package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Restores the legacy RWBYM scroll item as the entry point for the Scroll GUI.
 */
public class RWBYMScrollItem extends Item {
    public RWBYMScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // The 1.12 item opened a client-only scroll GUI through the old GuiHandler.
            RWBYMNetwork.sendOpenScrollScreen(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
