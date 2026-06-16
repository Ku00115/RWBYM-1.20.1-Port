package io.github.blaezdev.rwbym.item;

import com.google.common.collect.Multimap;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class RWBYMWearableItem extends Item implements Equipable {
    private final EquipmentSlot slot;
    private final long perks;
    private final String morphTarget;

    public RWBYMWearableItem(String itemName, EquipmentSlot slot, Properties properties) {
        super(properties);
        this.slot = slot;
        this.perks = RWBYMArmorItem.perksForWearable(itemName);
        this.morphTarget = morphTargetFor(itemName);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && this.perks != 0L && entity instanceof Player player
                && player.getItemBySlot(this.slot) == stack && player.tickCount % 40 == 0) {
            RWBYMArmorItem.applyPassiveEffects(player, this.perks);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == this.slot && this.perks != 0L) {
            return RWBYMArmorItem.buildPerkAttributeModifiers(slot, this.perks,
                    super.getDefaultAttributeModifiers(slot));
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return this.slot;
    }

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return this.slot;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return armorType == this.slot;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && this.morphTarget != null) {
            if (!level.isClientSide()) {
                Item target = BuiltInRegistries.ITEM.get(new ResourceLocation(this.morphTarget));
                if (target != Items.AIR && target != stack.getItem()) {
                    ItemStack morphed = new ItemStack(target, stack.getCount());
                    morphed.setTag(stack.getTag() == null ? null : stack.getTag().copy());
                    morphed.setDamageValue(Math.min(stack.getDamageValue(), morphed.getMaxDamage()));
                    player.setItemInHand(hand, morphed);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER,
                            SoundSource.PLAYERS, 0.65F, 1.15F);
                }
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    public long getPerks() {
        return this.perks;
    }

    private static String morphTargetFor(String itemName) {
        return switch (itemName) {
            case "rubyhood" -> "rwbym:ruby2_head";
            case "summerhood" -> "rwbym:summer2_head";
            case "taylorhood" -> "rwbym:taylor_head";
            default -> null;
        };
    }
}
