package io.github.blaezdev.rwbym.item;

import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import java.util.List;
import javax.annotation.Nullable;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RWBYMWearableItem extends Item implements Equipable {
    private static final java.util.UUID LEGACY_HOOD_ARMOR_UUID =
            java.util.UUID.fromString("883e415e-46d5-11ea-b77f-2e728ce88125");

    private final String itemName;
    private final EquipmentSlot slot;
    private final long perks;
    private final int legacyHoodArmor;
    private final String morphTarget;
    private final ResourceLocation morphTargetId;

    public RWBYMWearableItem(String itemName, EquipmentSlot slot, Properties properties) {
        super(properties);
        this.itemName = itemName;
        this.slot = slot;
        this.perks = RWBYMArmorItem.perksForWearable(itemName);
        this.legacyHoodArmor = legacyHoodArmor(itemName);
        this.morphTarget = morphTargetFor(itemName);
        this.morphTargetId = this.morphTarget == null ? null : new ResourceLocation(this.morphTarget);
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
        if (slot == this.slot) {
            Multimap<Attribute, AttributeModifier> modifiers = this.perks != 0L
                    ? RWBYMArmorItem.buildPerkAttributeModifiers(slot, this.perks,
                            super.getDefaultAttributeModifiers(slot))
                    : super.getDefaultAttributeModifiers(slot);
            if (slot == EquipmentSlot.HEAD && this.legacyHoodArmor > 0) {
                ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.putAll(modifiers);
                // Original RWBYHood supplied vanilla armor value in addition to RWBYM perk modifiers.
                builder.put(Attributes.ARMOR, new AttributeModifier(LEGACY_HOOD_ARMOR_UUID,
                        "RWBYM hood armor", this.legacyHoodArmor, AttributeModifier.Operation.ADDITION));
                return builder.build();
            }
            return modifiers;
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
        if (player.isShiftKeyDown() && this.morphTargetId != null) {
            if (!level.isClientSide()) {
                Item target = BuiltInRegistries.ITEM.get(this.morphTargetId);
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

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        var scrap = RWBYMItems.SIMPLE_ITEMS.get("scrap");
        return scrap != null && scrap.isPresent() && repair.is(scrap.get())
                || super.isValidRepairItem(stack, repair);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.itemName.equals("relicofknowledge")) {
            tooltip.add(Component.literal("Boosted XP Gain").withStyle(ChatFormatting.BLUE));
        }
    }

    private static String morphTargetFor(String itemName) {
        return switch (itemName) {
            case "rubyhood" -> "rwbym:ruby2_head";
            case "summerhood" -> "rwbym:summer2_head";
            case "taylorhood" -> "rwbym:taylor_head";
            default -> null;
        };
    }

    public static boolean isLegacyHoodName(String itemName) {
        return switch (itemName) {
            case "antimagic_mask", "henchmenhat", "henchmenhatglasses", "mariaeyes", "mariamask",
                    "ozpinglasses", "rubyhood", "rvnmask", "summerhood", "taylorhood", "whtefng" -> true;
            default -> false;
        };
    }

    private static int legacyHoodArmor(String itemName) {
        if (!isLegacyHoodName(itemName)) {
            return 0;
        }
        return itemName.contains("summer") ? 7 : 6;
    }
}
