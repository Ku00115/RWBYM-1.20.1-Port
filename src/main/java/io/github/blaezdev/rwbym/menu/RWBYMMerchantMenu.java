package io.github.blaezdev.rwbym.menu;

import io.github.blaezdev.rwbym.registry.RWBYMMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

/**
 * RWBYM merchant container bridge.
 *
 * <p>The original mod used a custom villager container so its book-style trade GUI could drive
 * normal merchant slots. Forge 1.20.1 already keeps merchant payment/result behavior in
 * {@link MerchantMenu}, so this class preserves that behavior while exposing RWBYM's own menu type
 * for the custom client screen.</p>
 */
public class RWBYMMerchantMenu extends MerchantMenu {
    private final int merchantEntityId;

    public RWBYMMerchantMenu(int containerId, Inventory playerInventory, int merchantEntityId) {
        super(containerId, playerInventory);
        this.merchantEntityId = merchantEntityId;
    }

    public RWBYMMerchantMenu(int containerId, Inventory playerInventory, Merchant merchant) {
        super(containerId, playerInventory, merchant);
        this.merchantEntityId = merchant instanceof net.minecraft.world.entity.Entity entity ? entity.getId() : -1;
    }

    @Override
    public MenuType<?> getType() {
        return RWBYMMenuTypes.MERCHANT.get();
    }

    public int getMerchantEntityId() {
        return this.merchantEntityId;
    }

    /**
     * Performs the original RWBYM book right-click behavior: select a recipe, move ingredients,
     * then take the result through the real merchant result slot so costs and trade callbacks stay valid.
     */
    public void performBookTrade(Player player, int offerIndex, boolean tradeAll) {
        if (offerIndex < 0 || offerIndex >= this.getOffers().size()) {
            return;
        }
        int attempts = tradeAll ? 64 : 1;
        for (int i = 0; i < attempts; i++) {
            this.setSelectionHint(offerIndex);
            this.tryMoveItems(offerIndex);
            ItemStack traded = this.quickMoveStack(player, 2);
            if (traded.isEmpty()) {
                break;
            }
            if (!tradeAll) {
                break;
            }
        }
    }
}
