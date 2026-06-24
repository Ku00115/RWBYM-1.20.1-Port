package io.github.blaezdev.rwbym.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.blaezdev.rwbym.entity.RWBYMProjectileEntity;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.registry.RWBYMSounds;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Port of the legacy RWBYCutGem item. Cut gems act as expendable dust launchers and offhand dust charms:
 * they require the matching cut-dust ammo id, spawn an elemental projectile, then consume the cut gem stack.
 */
public class RWBYMCutGemItem extends Item {
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("cc4c54bc-46d5-11ea-b77f-2e728ce88125");
    private static final UUID DEFENCE_UUID = UUID.fromString("cc4c5714-46d5-11ea-b77f-2e728ce88125");
    private static final UUID VITALITY_UUID = UUID.fromString("cc4c591c-46d5-11ea-b77f-2e728ce88125");
    private static final UUID ATTACK_BOOST_UUID = UUID.fromString("cc4c5b24-46d5-11ea-b77f-2e728ce88125");
    private static final UUID KNOCKBACK_UUID = UUID.fromString("cc4c5d90-46d5-11ea-b77f-2e728ce88125");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("cc4c5ee4-46d5-11ea-b77f-2e728ce88125");

    private final String name;
    private final String ammoId;
    private final boolean gravity;
    private final boolean water;
    private final float projectileDamage;
    private final String projectileElement;
    private final Multimap<Attribute, AttributeModifier> offhandModifiers;

    public RWBYMCutGemItem(String name, Properties properties) {
        super(properties);
        this.name = name;
        this.ammoId = ammoIdFor(name);
        this.gravity = name.contains("gravity");
        this.water = name.contains("water");
        this.projectileDamage = projectileDamageFor(name);
        this.projectileElement = projectileElementFor(name);
        this.offhandModifiers = buildOffhandModifiers(name);
    }

    /**
     * Fires only when the matching legacy cut-dust ammo exists. The original used RWBYAmmoItem#createArrow;
     * the port maps that intent to RWBYMProjectileEntity while preserving the ammo gate and cut-gem consumption.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack ammo = findAmmo(player);
        if (ammo.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide()) {
            RWBYMProjectileEntity projectile = new RWBYMProjectileEntity(level, player, ammo, stack,
                    this.projectileDamage, this.projectileElement, false, true, false);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.9F, 2.0F);
            level.addFreshEntity(projectile);
            // Legacy no-charge cut gems consume the volatile cut crystal itself, not the ammo stack.
            stack.shrink(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), RWBYMSounds.RIFLE_SHOOT.get(),
                    SoundSource.PLAYERS, 0.45F, 1.3F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Restores the two original offhand passive effects: gravity lift/fall reset and water healing/breathing.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player) || player.getOffhandItem() != stack) {
            return;
        }
        if (this.gravity) {
            if (!player.onGround() && player.isShiftKeyDown()) {
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1, false, false));
            }
            if (!player.onGround()) {
                player.setDeltaMovement(player.getDeltaMovement().add(new Vec3(0.0D, 0.05D, 0.0D)));
                player.hasImpulse = true;
                player.fallDistance = 0.0F;
            }
        } else if (this.water) {
            if (player.isInWaterOrBubble()) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 1, false, false));
            }
            player.heal(0.02F);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(RWBYMItems.SIMPLE_ITEMS.get("scrap").get()) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 1200;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.OFFHAND) {
            return this.offhandModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private ItemStack findAmmo(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (isMatchingAmmo(offhand)) {
            return offhand;
        }
        ItemStack mainhand = player.getMainHandItem();
        if (isMatchingAmmo(mainhand)) {
            return mainhand;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (isMatchingAmmo(candidate)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isMatchingAmmo(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.toString().equals(this.ammoId);
    }

    private static String ammoIdFor(String name) {
        if (name.contains("gravity")) {
            return "rwbym:gravitydustcut";
        }
        if (name.contains("wind")) {
            return "rwbym:winddustcut";
        }
        if (name.contains("water")) {
            return "rwbym:waterdustcut";
        }
        if (name.contains("fire")) {
            return "rwbym:firedustcut";
        }
        if (name.contains("light")) {
            return "rwbym:lightdustcut";
        }
        if (name.contains("ice")) {
            return "rwbym:icedustcut";
        }
        return "rwbym:dustcut";
    }

    private static float projectileDamageFor(String name) {
        return name.contains("dustcrystalcut") || name.equals("dustcutgem") ? 0.0F : 6.0F;
    }

    private static String projectileElementFor(String name) {
        if (name.contains("fire")) {
            return "fire explosion3 cloud";
        }
        if (name.contains("ice")) {
            return "ice explosion3 cloud";
        }
        if (name.contains("gravity")) {
            return "grav explosion2 cloud";
        }
        if (name.contains("water")) {
            return "water explosion3 cloud";
        }
        if (name.contains("wind")) {
            return "wind explosion3 cloud";
        }
        if (name.contains("light")) {
            return "light explosion3 cloud";
        }
        return "explosion3 cloud";
    }

    private static Multimap<Attribute, AttributeModifier> buildOffhandModifiers(String name) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        if (name.contains("water")) {
            addModifier(builder, Attributes.ATTACK_DAMAGE, ATTACK_BOOST_UUID, "RWBYM cut gem attack", -0.45D);
        } else if (name.contains("wind")) {
            addModifier(builder, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "RWBYM cut gem movement", 1.7D);
            addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM cut gem vitality", -0.7D);
        } else if (name.contains("fire")) {
            addModifier(builder, Attributes.ATTACK_DAMAGE, ATTACK_BOOST_UUID, "RWBYM cut gem attack", 2.3D);
            addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM cut gem vitality", -0.7D);
        } else if (name.contains("gravity")) {
            addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM cut gem vitality", -0.7D);
        } else if (name.contains("light")) {
            addModifier(builder, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "RWBYM cut gem movement", 0.75D);
            addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM cut gem vitality", -0.4D);
            addModifier(builder, Attributes.ATTACK_SPEED, ATTACK_SPEED_UUID, "RWBYM cut gem attack speed", 2.5D);
        } else if (name.contains("ice")) {
            addModifier(builder, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "RWBYM cut gem movement", -0.5D);
            addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM cut gem vitality", 1.5D);
        } else {
            addModifier(builder, Attributes.MAX_HEALTH, VITALITY_UUID, "RWBYM cut gem vitality", -0.7D);
        }
        return builder.build();
    }

    private static void addModifier(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
            Attribute attribute, UUID uuid, String name, double amount) {
        builder.put(attribute, new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
}
