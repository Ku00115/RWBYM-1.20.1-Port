package io.github.blaezdev.rwbym.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the legacy RWBYAmmoItem. Ammo and Dust stacks are still inventory items, but some Dust types
 * also work as offhand charms, furnace fuel, and projectile metadata consumed by RWBYMWeaponItem.
 */
public class RWBYMAmmoItem extends Item {
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("24806a06-46d6-11ea-b77f-2e728ce88125");
    private static final UUID DEFENCE_UUID = UUID.fromString("24806d26-46d6-11ea-b77f-2e728ce88125");
    private static final UUID VITALITY_UUID = UUID.fromString("24806eb6-46d6-11ea-b77f-2e728ce88125");
    private static final UUID ATTACK_BOOST_UUID = UUID.fromString("24807078-46d6-11ea-b77f-2e728ce88125");
    private static final UUID KNOCKBACK_UUID = UUID.fromString("248071c2-46d6-11ea-b77f-2e728ce88125");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("248072ee-46d6-11ea-b77f-2e728ce88125");

    private final String name;
    private final DustElement element;
    private final double baseDamage;
    private final boolean dustFuel;
    private final Multimap<Attribute, AttributeModifier> offhandModifiers;

    public RWBYMAmmoItem(String name, Properties properties) {
        super(properties);
        this.name = name;
        this.element = DustElement.fromName(name);
        this.baseDamage = baseDamageFor(name, this.element);
        this.dustFuel = isDustFuel(name);
        this.offhandModifiers = buildOffhandModifiers(this.element);
    }

    /**
     * Restores legacy offhand Dust passives: Gravity Dust slows falling/lifts the player and Water Dust heals slowly.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player) || player.getOffhandItem() != stack) {
            return;
        }
        if (this.element == DustElement.GRAVITY && !player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().add(new Vec3(0.0D, 0.05D, 0.0D)));
            player.hasImpulse = true;
            player.fallDistance = 0.0F;
        } else if (this.element == DustElement.WATER && player.tickCount % 2 == 0) {
            // The original healed 0.01F every tick; batching every other tick avoids no-op float rounding on 1.20 health sync.
            player.heal(0.02F);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.OFFHAND && this.element != DustElement.NONE) {
            return this.offhandModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.dustFuel ? 5200 : super.getBurnTime(itemStack, recipeType);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.baseDamage > 0.0D) {
            tooltip.add(Component.literal("Bullet impact damage").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal(String.format("%.1f", this.baseDamage)).withStyle(ChatFormatting.BLUE));
        }
        if (this.element != DustElement.NONE) {
            tooltip.add(Component.literal("Element: " + this.element.label).withStyle(ChatFormatting.BLUE));
        }
    }

    private static Multimap<Attribute, AttributeModifier> buildOffhandModifiers(DustElement element) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        switch (element) {
            case WATER -> addModifier(builder, Attributes.ATTACK_DAMAGE, ATTACK_BOOST_UUID, "RWBYM Dust attack",
                    -0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            case IMPURE -> addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM Dust vitality",
                    -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            case WIND -> {
                addModifier(builder, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "RWBYM Dust movement",
                        1.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM Dust vitality",
                        -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case FIRE -> {
                addModifier(builder, Attributes.ATTACK_DAMAGE, ATTACK_BOOST_UUID, "RWBYM Dust attack",
                        1.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM Dust vitality",
                        -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case GRAVITY -> addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM Dust vitality",
                    -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            case LIGHTNING -> {
                addModifier(builder, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "RWBYM Dust movement",
                        0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM Dust vitality",
                        -0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.ATTACK_SPEED, ATTACK_SPEED_UUID, "RWBYM Dust attack speed",
                        2.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case ICE -> {
                addModifier(builder, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "RWBYM Dust movement",
                        -0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM Dust vitality",
                        0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case HARDLIGHT -> {
                addModifier(builder, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "RWBYM Dust movement",
                        0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.ARMOR, DEFENCE_UUID, "RWBYM Dust defense",
                        0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.ATTACK_DAMAGE, ATTACK_BOOST_UUID, "RWBYM Dust attack",
                        -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addModifier(builder, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_UUID, "RWBYM Dust footing",
                        1.0D, AttributeModifier.Operation.ADDITION);
            }
            default -> {
            }
        }
        return builder.build();
    }

    private static void addModifier(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
            Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        if (amount != 0.0D) {
            builder.put(attribute, new AttributeModifier(uuid, name, amount, operation));
        }
    }

    private static boolean isDustFuel(String name) {
        return name.contains("dust") || name.contains("crystal");
    }

    private static double baseDamageFor(String name, DustElement element) {
        if (name.contains("rocket") || name.contains("jnrammo") || name.contains("grenade")) {
            return 15.0D;
        }
        if (name.contains("extasisammo")) {
            return 30.0D;
        }
        if (name.equals("firedust") || name.equals("icedust") || name.equals("lightdust")
                || name.equals("gravitydust") || name.endsWith("dustcrystal")) {
            return 10.0D;
        }
        if (name.equals("bolt") || name.startsWith("bolt") || name.contains("ammo") || name.contains("bullet")
                || name.contains("shell")) {
            return 14.0D;
        }
        return element == DustElement.NONE ? 0.0D : 0.0D;
    }

    private enum DustElement {
        HARDLIGHT("Hard-Light"),
        WATER("Water"),
        IMPURE("Impure"),
        WIND("Wind"),
        FIRE("Fire"),
        GRAVITY("Gravity"),
        LIGHTNING("Lightning"),
        ICE("Ice"),
        NONE("");

        private final String label;

        DustElement(String label) {
            this.label = label;
        }

        private static DustElement fromName(String name) {
            if (name.contains("hardlight")) {
                return HARDLIGHT;
            }
            if (name.contains("water")) {
                return WATER;
            }
            if (name.equals("dust") || name.equals("dustcrystal") || name.equals("dustrock")) {
                return IMPURE;
            }
            if (name.contains("wind")) {
                return WIND;
            }
            if (name.contains("fire")) {
                return FIRE;
            }
            if (name.contains("gravity") || name.contains("grav")) {
                return GRAVITY;
            }
            if (name.contains("light") || name.contains("electric") || name.contains("flare")) {
                return LIGHTNING;
            }
            if (name.contains("ice")) {
                return ICE;
            }
            return NONE;
        }
    }
}
