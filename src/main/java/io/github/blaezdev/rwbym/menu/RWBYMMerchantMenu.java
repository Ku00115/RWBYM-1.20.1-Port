package io.github.blaezdev.rwbym.menu;

import io.github.blaezdev.rwbym.registry.RWBYMMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

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
    private int lastIngredientSearchSlot = 3;

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
        performBookTrade(player, offerIndex, tradeAll, true);
    }

    public void performBookTrade(Player player, int offerIndex, boolean tradeAll, boolean clearSlots) {
        if (offerIndex < 0 || offerIndex >= this.getOffers().size()) {
            return;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original GuiVillager right-click moved only the recipe costs, then took the result slot.
        int attempts = tradeAll ? maxBookTradeAttempts(this.getOffers().get(offerIndex)) : 1;
        for (int i = 0; i < attempts; i++) {
            if (!preparePaymentSlotsForOffer(offerIndex, clearSlots, true)) {
                break;
            }
            if (!takeResultAndStoreOrDrop(player)) {
                break;
            }
            clearSlots = false;
            if (!tradeAll) {
                break;
            }
        }
        if (tradeAll) {
            returnPaymentSlotsToInventory();
        }
    }

    private int maxBookTradeAttempts(MerchantOffer offer) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        if (costA.isEmpty()) {
            return 0;
        }
        int availableA = countAvailablePaymentItems(costA);
        int requiredA = costA.getCount();
        if (!costB.isEmpty() && paymentStackMatches(costA, costB)) {
            // Some modern offers can technically use the same item twice; combine the requirement to avoid over-trading.
            requiredA += costB.getCount();
            return availableA / requiredA;
        }
        int attempts = availableA / requiredA;
        if (!costB.isEmpty()) {
            attempts = Math.min(attempts, countAvailablePaymentItems(costB) / costB.getCount());
        }
        return attempts;
    }

    private int countAvailablePaymentItems(ItemStack expected) {
        int count = 0;
        for (int i = 0; i < this.slots.size(); i++) {
            if (i == 2) {
                continue;
            }
            ItemStack stack = this.getSlot(i).getItem();
            if (paymentStackMatches(stack, expected)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public void prepareBookTrade(int offerIndex) {
        prepareBookTrade(offerIndex, true);
    }

    public void prepareBookTrade(int offerIndex, boolean clearSlots) {
        if (offerIndex < 0 || offerIndex >= this.getOffers().size()) {
            return;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy MessageTradingData channel 2 populated the server-side merchant slots on left-click.
        preparePaymentSlotsForOffer(offerIndex, clearSlots, false);
    }

    public void returnPaymentSlotsToInventory() {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The legacy ContainerVillager merged leftover payment-slot items back after shift-right-click trading.
        for (int slotIndex = 0; slotIndex <= 1; slotIndex++) {
            ItemStack stack = this.getSlot(slotIndex).getItem();
            if (!stack.isEmpty() && this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                if (stack.isEmpty()) {
                    this.getSlot(slotIndex).set(ItemStack.EMPTY);
                }
                this.getSlot(slotIndex).setChanged();
            }
        }
    }

    private boolean preparePaymentSlotsForOffer(int offerIndex, boolean clearSlots, boolean quickTrade) {
        MerchantOffer offer = this.getOffers().get(offerIndex);
        this.setSelectionHint(offerIndex);
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        if (arePaymentSlotsSwitched(costA, costB)) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Legacy ContainerVillager accepted reversed payment slots from manual item placement.
            if (!returnPaymentSlotToInventory(0) || !returnPaymentSlotToInventory(1)) {
                return false;
            }
            clearSlots = false;
        }
        if (!normalizePaymentSlot(0, costA, clearSlots)) {
            return false;
        }
        if (!normalizePaymentSlot(1, costB, clearSlots)) {
            return false;
        }
        fillPaymentSlot(0, costA, quickTrade);
        fillPaymentSlot(1, costB, quickTrade);
        this.setSelectionHint(offerIndex);
        return hasRequiredPayment(0, costA) && hasRequiredPayment(1, costB);
    }

    private boolean takeResultAndStoreOrDrop(Player player) {
        Slot resultSlot = this.getSlot(2);
        ItemStack result = resultSlot.getItem();
        if (result.isEmpty()) {
            return false;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy ContainerVillager dropped quick-trade output when the player inventory was full.
        ItemStack taken = resultSlot.remove(result.getCount());
        if (taken.isEmpty()) {
            return false;
        }
        resultSlot.onTake(player, taken);
        ItemStack remainder = taken.copy();
        this.moveItemStackTo(remainder, 3, this.slots.size(), true);
        if (!remainder.isEmpty()) {
            player.drop(remainder, false);
            remainder.setCount(0);
        }
        return true;
    }

    private boolean normalizePaymentSlot(int slotIndex, ItemStack expected, boolean clearSlots) {
        ItemStack current = this.getSlot(slotIndex).getItem();
        if (current.isEmpty()) {
            return true;
        }
        if (expected.isEmpty()) {
            return returnPaymentSlotToInventory(slotIndex);
        }
        if (!clearSlots && paymentStackMatches(current, expected)) {
            return true;
        }
        return returnPaymentSlotToInventory(slotIndex);
    }

    private boolean returnPaymentSlotToInventory(int slotIndex) {
        ItemStack stack = this.getSlot(slotIndex).getItem();
        if (stack.isEmpty()) {
            return true;
        }
        if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
            return false;
        }
        if (stack.isEmpty()) {
            this.getSlot(slotIndex).set(ItemStack.EMPTY);
        }
        this.getSlot(slotIndex).setChanged();
        // A partially returned payment stack must stop recipe preparation, or the stale slot can poison the next trade.
        return stack.isEmpty();
    }

    private void fillPaymentSlot(int targetSlotIndex, ItemStack expected, boolean quickTrade) {
        if (expected.isEmpty()) {
            return;
        }
        Slot targetSlot = this.getSlot(targetSlotIndex);
        ItemStack targetStack = targetSlot.getItem();
        int currentCount = paymentStackMatches(targetStack, expected) ? targetStack.getCount() : 0;
        int needed = Math.min(expected.getCount(), expected.getMaxStackSize()) - currentCount;
        if (needed <= 0) {
            return;
        }
        // Original ContainerVillager remembered where the last ingredient search stopped so repeated clicks keep moving forward.
        for (int scanned = 0; scanned < this.slots.size() - 3 && needed > 0; scanned++) {
            int sourceIndex = 3 + Math.floorMod(this.lastIngredientSearchSlot - 3 + scanned, this.slots.size() - 3);
            Slot sourceSlot = this.getSlot(sourceIndex);
            ItemStack sourceStack = sourceSlot.getItem();
            if (sourceStack.isEmpty() || !paymentStackMatches(sourceStack, expected)) {
                continue;
            }
            targetStack = targetSlot.getItem();
            if (!canMergePaymentStack(targetStack, sourceStack, quickTrade)) {
                continue;
            }
            int moved = Math.min(needed, sourceStack.getCount());
            ItemStack newPaymentStack = sourceStack.copy();
            newPaymentStack.setCount(currentCount + moved);
            sourceStack.shrink(moved);
            targetSlot.set(newPaymentStack);
            sourceSlot.setChanged();
            targetSlot.setChanged();
            currentCount += moved;
            needed -= moved;
            if (needed <= 0) {
                this.lastIngredientSearchSlot = sourceIndex;
            }
        }
    }

    private boolean hasRequiredPayment(int slotIndex, ItemStack expected) {
        ItemStack current = this.getSlot(slotIndex).getItem();
        return expected.isEmpty() ? current.isEmpty()
                : paymentStackMatches(current, expected) && current.getCount() >= expected.getCount();
    }

    private boolean arePaymentSlotsSwitched(ItemStack costA, ItemStack costB) {
        ItemStack slotA = this.getSlot(0).getItem();
        ItemStack slotB = this.getSlot(1).getItem();
        boolean firstLooksLikeSecondCost = !slotA.isEmpty() && paymentStackMatches(slotA, costB);
        boolean secondLooksLikeFirstCost = !slotB.isEmpty() && paymentStackMatches(slotB, costA);
        boolean alreadyAligned = paymentStackMatches(slotA, costA) || paymentStackMatches(slotB, costB);
        return (firstLooksLikeSecondCost || secondLooksLikeFirstCost) && !alreadyAligned;
    }

    private static boolean paymentStackMatches(ItemStack current, ItemStack expected) {
        return !current.isEmpty() && !expected.isEmpty() && ItemStack.isSameItem(current, expected);
    }

    private static boolean canMergePaymentStack(ItemStack current, ItemStack source, boolean quickTrade) {
        if (quickTrade || current.isEmpty()) {
            return true;
        }
        // Original non-skipMove preparation avoided mixing differently tagged stacks in the payment slot.
        return ItemStack.isSameItemSameTags(current, source);
    }
}
