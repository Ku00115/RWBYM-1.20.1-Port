package io.github.blaezdev.rwbym.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RWBYMGliderItem extends Item {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public RWBYMGliderItem(Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                15.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                -1.0D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 6000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    public static boolean isUsable(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || living.getUseItem() != stack || !living.isFallFlying()) {
            return;
        }
        Vec3 look = living.getLookAngle();
        ItemStack offhand = living.getOffhandItem();
        if (offhand.getItem() == RWBYMItems.SIMPLE_ITEMS.get("winddustcrystal").get()) {
            // Wind dust boosts only during real fall-flying, matching the original RWBYGliderItem Elytra bridge.
            living.setDeltaMovement(living.getDeltaMovement().add(
                    look.x * 0.1D + (look.x * 0.5D - living.getDeltaMovement().x) * 0.5D,
                    look.y * 0.1D + (look.y * 0.5D - living.getDeltaMovement().y) * 0.5D,
                    look.z * 0.1D + (look.z * 0.5D - living.getDeltaMovement().z) * 0.5D));
            if (!level.isClientSide() && entity.tickCount % 90 == 0 && living instanceof Player player
                    && !player.getAbilities().instabuild) {
                offhand.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(InteractionHand.OFF_HAND));
            }
        }
    }
}
